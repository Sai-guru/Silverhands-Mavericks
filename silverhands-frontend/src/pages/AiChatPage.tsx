import { useState, type FormEvent } from 'react'
import { api } from '../api/http'
import type { AiChatResponse } from '../api/types'

export function AiChatPage() {
  const [message, setMessage] = useState('')
  const [inputLanguage, setInputLanguage] = useState('en')
  const [outputLanguage, setOutputLanguage] = useState('en')
  const [inputType, setInputType] = useState('text')
  const [result, setResult] = useState<AiChatResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setBusy(true)
    setError(null)
    try {
      const response = await api<AiChatResponse>('/api/ai/chat', {
        method: 'POST',
        body: JSON.stringify({ message, inputLanguage, outputLanguage, inputType }),
      })
      setResult(response)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Chat failed')
    } finally {
      setBusy(false)
    }
  }

  function handleClear() {
    setMessage('')
    setResult(null)
    setError(null)
  }

  return (
    <section className="panel">
      <h1>AI chat</h1>
      <p className="muted">POST /api/ai/chat</p>

      {error ? <p className="banner error">{error}</p> : null}

      <form className="stack" onSubmit={(event) => void onSubmit(event)}>
        <label>
          Message
          <textarea
            value={message}
            required
            onChange={(event) => setMessage(event.target.value)}
          />
        </label>

        <div className="grid">
          {/* we dont need this for our ai chat */}
          {/* <label>
            Input language
            <input value={inputLanguage} onChange={(event) => setInputLanguage(event.target.value)} />
          </label>
          <label>
            Output language
            <input value={outputLanguage} onChange={(event) => setOutputLanguage(event.target.value)} />
          </label>
          <label>
            Input type
            <input value={inputType} onChange={(event) => setInputType(event.target.value)} />
          </label> */}
        </div>

        <button type="submit" disabled={busy}>
          Send
        </button>

        <button type="button" onClick={handleClear} disabled={busy}>
          Clear
        </button>
      </form>

      {result ? (
        <div className="stack">
          <h2>Reply</h2>
          <p>{result.reply}</p>
          {result.recommendedServices &&
          result.recommendedServices.length > 0 ? (
            <ul>
              {result.recommendedServices.map((service) => (
                <li key={service.serviceId ?? service.id}>
                  {service.name ?? service.serviceName} · {service.area ?? ''}{' '}
                  {service.phoneNumber ?? ''}
                </li>
              ))}
            </ul>
          ) : null}
        </div>
      ) : null}
    </section>
  )
}