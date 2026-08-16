import { useCallback, useEffect, useRef, useState, type FormEvent } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Client, type IMessage } from '@stomp/stompjs'
import { api, getPage } from '../api/http'
import { API_URL } from '../api/config'
import type { JsonRecord } from '../api/types'
import { useAuth } from '../auth/AuthContext'

interface ConversationItem {
  id: string
  label: string
  createdAt: string | null
}

interface MessageItem {
  id: string
  content: string
  senderId: string | null
  createdAt: string | null
}

function wsUrl(): string {
  return `${API_URL.replace(/^http/, 'ws')}/ws/chat`
}

function toConversation(r: JsonRecord): ConversationItem {
  const other =
    (r.otherUser as JsonRecord | null) ??
    (r.otherParticipant as JsonRecord | null) ??
    (r.customerUser as JsonRecord | null) ??
    (r.providerUser as JsonRecord | null)
  const label =
    (other != null && typeof other === 'object' ? asText(other.name) : asText(r.otherUserName)) ||
    asText(r.otherUserName) ||
    `Conversation ${String(r.id ?? '').slice(0, 8)}`
  return {
    id: String(r.id ?? ''),
    label,
    createdAt: r.createdAt != null ? String(r.createdAt) : null,
  }
}

function asText(value: unknown): string {
  return value == null ? '' : String(value)
}

function toMessage(r: JsonRecord): MessageItem {
  return {
    id: String(r.id ?? `${r.createdAt ?? ''}-${r.content ?? ''}`),
    content: asText(r.content ?? r.message ?? r.text),
    senderId: r.senderId != null ? String(r.senderId) : r.senderUser != null ? String(r.senderUser) : null,
    createdAt: r.createdAt != null ? String(r.createdAt) : r.sentAt != null ? String(r.sentAt) : null,
  }
}

function formatTime(iso: string | null): string {
  if (!iso) return ''
  try {
    return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  } catch {
    return ''
  }
}

export function ChatPage() {
  const { user } = useAuth()
  const [searchParams] = useSearchParams()
  const queryConvId = searchParams.get('conversationId')
  const queryUserId = searchParams.get('userId') ?? searchParams.get('providerId')

  const [conversations, setConversations] = useState<ConversationItem[]>([])
  const [selectedId, setSelectedId] = useState<string | null>(queryConvId)
  const [messages, setMessages] = useState<MessageItem[]>([])
  const [draft, setDraft] = useState('')
  const [loadingConvs, setLoadingConvs] = useState(true)
  const [loadingMsgs, setLoadingMsgs] = useState(false)
  const [connStatus, setConnStatus] = useState<'connecting' | 'connected' | 'disconnected'>('disconnected')
  const [error, setError] = useState<string | null>(null)
  const bottomRef = useRef<HTMLDivElement>(null)
  const stompRef = useRef<Client | null>(null)

  // ---------- load conversations ----------
  const loadConversations = useCallback(async () => {
    setLoadingConvs(true)
    try {
      const page = await getPage<JsonRecord>('/api/conversations')
      const list = page.content.map(toConversation)
      setConversations(list)

      if (queryConvId) {
        setSelectedId(queryConvId)
      } else if (queryUserId) {
        // POST /api/conversations finds the existing pair conversation or creates it
        const created = await api<unknown>('/api/conversations', {
          method: 'POST',
          body: JSON.stringify({ otherUserId: queryUserId }),
        })
        const record = (Array.isArray(created) ? created[0] : created) as JsonRecord
        const id = record?.id != null ? String(record.id) : String(created ?? '')
        const refreshed = await getPage<JsonRecord>('/api/conversations')
        setConversations(refreshed.content.map(toConversation))
        setSelectedId(id)
      } else if (!selectedId && list.length > 0) {
        setSelectedId(list[0].id)
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load conversations')
    } finally {
      setLoadingConvs(false)
    }
  }, [queryConvId, queryUserId, selectedId])

  useEffect(() => {
    void loadConversations()
  }, [loadConversations])

  // ---------- STOMP connection ----------
  useEffect(() => {
    const client = new Client({
      brokerURL: wsUrl(),
      reconnectDelay: 3000,
      onConnect: () => setConnStatus('connected'),
      onWebSocketClose: () => setConnStatus('disconnected'),
      onStompError: () => setConnStatus('disconnected'),
    })
    stompRef.current = client
    setConnStatus('connecting')
    void client.activate()

    return () => {
      void client.deactivate()
    }
  }, [])

  // ---------- subscribe to selected conversation topic ----------
  useEffect(() => {
    const client = stompRef.current
    if (!selectedId || !client || connStatus !== 'connected') return

    const subscription = client.subscribe(`/topic/conversations/${selectedId}`, (frame: IMessage) => {
      try {
        const body = JSON.parse(frame.body) as JsonRecord
        setMessages((prev) => {
          const msg = toMessage(body)
          if (prev.some((m) => m.id === msg.id)) return prev
          return [...prev, msg]
        })
      } catch {
        // ignore malformed frames
      }
    })

    // ---------- history ----------
    void (async () => {
      setLoadingMsgs(true)
      setMessages([])
      try {
        const page = await getPage<JsonRecord>(`/api/conversations/${selectedId}/messages`)
        setMessages(page.content.map(toMessage))
      } catch {
        setMessages([])
      } finally {
        setLoadingMsgs(false)
      }
    })()

    return () => {
      void subscription.unsubscribe()
    }
  }, [selectedId, connStatus])

  // auto-scroll to bottom when messages change
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  // ---------- send message via STOMP ----------
  function sendMessage(event: FormEvent) {
    event.preventDefault()
    const text = draft.trim()
    const client = stompRef.current
    if (!text || !selectedId || !client?.connected) return

    client.publish({
      destination: '/app/chat.send',
      body: JSON.stringify({ conversationId: selectedId, message: text }),
    })
    setDraft('')
  }

  const selectedConv = conversations.find((c) => c.id === selectedId)
  const myId = user?.id ?? ''

  return (
    <section className="chat-shell">
      {/* ── conversation list ── */}
      <aside className="chat-sidebar">
        <div className="chat-sidebar-head">
          <h2>Conversations</h2>
          <button type="button" className="ghost small-btn" onClick={() => void loadConversations()}>
            ↻
          </button>
        </div>

        <div className={`ws-badge ws-badge--${connStatus}`}>
          {connStatus === 'connected' ? '● Live' : connStatus === 'connecting' ? '◌ Connecting…' : '○ Offline'}
        </div>

        {error ? <p className="chat-error">{error}</p> : null}

        {loadingConvs ? (
          <p className="chat-empty">Loading…</p>
        ) : conversations.length === 0 ? (
          <p className="chat-empty">No conversations yet. Click 💬 Chat on a provider to start one.</p>
        ) : (
          <ul className="conv-list">
            {conversations.map((c) => (
              <li key={c.id}>
                <button
                  type="button"
                  className={`conv-item ${c.id === selectedId ? 'conv-item--active' : ''}`}
                  onClick={() => setSelectedId(c.id)}
                >
                  <span className="conv-id">{c.label}</span>
                  <span className="conv-meta">{c.createdAt ? formatTime(c.createdAt) : ''}</span>
                </button>
              </li>
            ))}
          </ul>
        )}
      </aside>

      {/* ── chat pane ── */}
      <div className="chat-pane">
        {!selectedId ? (
          <div className="chat-placeholder">
            <p>← Select a conversation to start chatting</p>
          </div>
        ) : (
          <>
            {/* header */}
            <div className="chat-pane-head">
              <div>
                <strong>{selectedConv?.label ?? `Conversation #${selectedId.slice(0, 8)}`}</strong>
              </div>
              <span className={`ws-dot ws-dot--${connStatus}`} title={connStatus} />
            </div>

            {/* messages */}
            <div className="chat-messages">
              {loadingMsgs ? (
                <p className="chat-empty">Loading messages…</p>
              ) : messages.length === 0 ? (
                <p className="chat-empty">No messages yet. Say something!</p>
              ) : (
                messages.map((msg) => {
                  const isMe = msg.senderId === myId
                  return (
                    <div key={msg.id} className={`bubble-wrap ${isMe ? 'bubble-wrap--me' : 'bubble-wrap--them'}`}>
                      <div className={`bubble ${isMe ? 'bubble--me' : 'bubble--them'}`}>
                        <p className="bubble-text">{msg.content}</p>
                        <span className="bubble-time">{formatTime(msg.createdAt)}</span>
                      </div>
                    </div>
                  )
                })
              )}
              <div ref={bottomRef} />
            </div>

            {/* input */}
            <form className="chat-input-bar" onSubmit={sendMessage}>
              <input
                type="text"
                className="chat-input"
                placeholder={connStatus === 'connected' ? 'Type a message…' : 'Connecting to live chat…'}
                value={draft}
                onChange={(e) => setDraft(e.target.value)}
                disabled={connStatus !== 'connected'}
              />
              <button
                type="submit"
                className="chat-send-btn"
                disabled={!draft.trim() || connStatus !== 'connected'}
              >
                Send
              </button>
            </form>
          </>
        )}
      </div>
    </section>
  )
}
