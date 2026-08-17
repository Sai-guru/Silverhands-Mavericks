// import { useCallback, useEffect, useState, type FormEvent } from 'react'
// import { useNavigate } from 'react-router-dom'
// import { useTranslation } from 'react-i18next'
// import { api, getPage } from '../api/http'
// import type { AppRole, JsonRecord } from '../api/types'
// import { useAuth } from '../auth/AuthContext'
// import type { FieldConfig, ResourceConfig } from '../resources'

// function asString(value: unknown): string {
//   if (value == null) {
//     return ''
//   }
//   if (Array.isArray(value)) {
//     return value.join(', ')
//   }
//   return String(value)
// }

// function emptyForm(resource: ResourceConfig): Record<string, string> {
//   const form: Record<string, string> = {}
//   for (const field of resource.fields) {
//     form[field.name] = ''
//   }
//   return form
// }

// function recordToForm(resource: ResourceConfig, record: JsonRecord): Record<string, string> {
//   const form: Record<string, string> = {}
//   for (const field of resource.fields) {
//     form[field.name] = asString(record[field.name])
//   }
//   return form
// }

// function serializeField(field: FieldConfig, raw: string): unknown {
//   const trimmed = raw.trim()
//   if (field.type === 'number') {
//     return trimmed.length === 0 ? null : Number(trimmed)
//   }
//   if (trimmed.length === 0) {
//     return field.required ? trimmed : null
//   }
//   return raw
// }

// function toPayload(resource: ResourceConfig, form: Record<string, string>): JsonRecord {
//   const payload: JsonRecord = {}
//   for (const field of resource.fields) {
//     payload[field.name] = serializeField(field, form[field.name] ?? '')
//   }
//   return payload
// }

// function listUrl(resource: ResourceConfig, search: Record<string, string>): string {
//   const params = new URLSearchParams(resource.listParams)
//   for (const [key, value] of Object.entries(search)) {
//     if (value.trim().length > 0) {
//       params.set(key, value.trim())
//     }
//   }
//   const query = params.toString()
//   return query.length > 0 ? `${resource.apiPath}?${query}` : resource.apiPath
// }

// // The provider's user id used to start a conversation (POST /api/conversations {otherUserId}).
// function providerUserIdFromRow(row: JsonRecord): string | null {
//   const candidate =
//     row.providerUserId ?? row.providerId ?? (row.provider as JsonRecord | null)?.id ?? row.provider ?? row.id
//   const value = candidate == null ? '' : String(candidate)
//   return value.length > 0 ? value : null
// }

// interface ResourceCrudProps {
//   resource: ResourceConfig
// }

// export function ResourceCrud({ resource }: ResourceCrudProps) {
//     const { t } = useTranslation()  //our translation hook
//   const navigate = useNavigate()
//   const { user } = useAuth()
//   const canWrite = user != null && resource.writeRoles.includes(user.role as AppRole)
//   const [rows, setRows] = useState<JsonRecord[]>([])
//   const [total, setTotal] = useState(0)
//   const [editingId, setEditingId] = useState<string | null>(null)
//   const [form, setForm] = useState<Record<string, string>>(() => emptyForm(resource))
//   const [search, setSearch] = useState<Record<string, string>>({})
//   const [status, setStatus] = useState<string | null>(null)
//   const [error, setError] = useState<string | null>(null)
//   const [busy, setBusy] = useState(false)

//   const load = useCallback(async () => {
//     setBusy(true)
//     setError(null)
//     try {
//       const page = await getPage<JsonRecord>(listUrl(resource, search))
//       setRows(page.content)
//       setTotal(page.totalElements)
//     } catch (err) {
//       setError(err instanceof Error ? err.message : 'Failed to load')
//     } finally {
//       setBusy(false)
//     }
//   }, [resource, search])

//   useEffect(() => {
//     setEditingId(null)
//     setForm(emptyForm(resource))
//     setSearch({})
//     void load()
//     // eslint-disable-next-line react-hooks/exhaustive-deps
//   }, [resource])

//   async function onSubmit(event: FormEvent) {
//     event.preventDefault()
//     if (!canWrite) {
//       return
//     }
//     setBusy(true)
//     setError(null)
//     setStatus(null)
//     try {
//       const payload = toPayload(resource, form)
//       if (editingId) {
//         await api(`${resource.apiPath}/${editingId}`, {
//           method: 'PUT',
//           body: JSON.stringify(payload),
//         })
//         setStatus('Updated')
//       } else {
//         await api(resource.apiPath, {
//           method: 'POST',
//           body: JSON.stringify(payload),
//         })
//         setStatus('Created')
//       }
//       setEditingId(null)
//       setForm(emptyForm(resource))
//       await load()
//     } catch (err) {
//       setError(err instanceof Error ? err.message : 'Save failed')
//     } finally {
//       setBusy(false)
//     }
//   }

//   async function onDelete(id: string) {
//     if (!canWrite) {
//       return
//     }
//     setBusy(true)
//     setError(null)
//     try {
//       await api(`${resource.apiPath}/${id}`, { method: 'DELETE' })
//       setStatus('Deleted')
//       if (editingId === id) {
//         setEditingId(null)
//         setForm(emptyForm(resource))
//       }
//       await load()
//     } catch (err) {
//       setError(err instanceof Error ? err.message : 'Delete failed')
//     } finally {
//       setBusy(false)
//     }
//   }

//   const columns = resource.fields.slice(0, 5)
//   const showActionsColumn = canWrite || user?.role === 'CUSTOMER'

//   return (
//     <section className="panel">
//       <header className="panel-head">
//         <div>
//           <h1>{t(resource.title)}</h1>
//           <p className="muted">
//             {resource.apiPath}
//             {resource.listParams ? `?${new URLSearchParams(resource.listParams).toString()}` : ''} ·{' '}
//             {total} records
//             {canWrite ? '' : ' · read only'}
//           </p>
//         </div>
//         {/* <button type="button" className="ghost" onClick={() => void load()} disabled={busy}>
//           Refresh
//         </button> */}
//         <button type="button" className="ghost" onClick={() => void load()} disabled={busy}>
//   {t('common.refresh')}
// </button>
//       </header>

//       {resource.searchable ? (
//         <form
//           className="stack"
//           onSubmit={(event) => {
//             event.preventDefault()
//             void load()
//           }}
//         >
//           <div className="grid">
//             <label>
//               Search
//               <input
//                 value={search.search ?? ''}
//                 placeholder="cooking, plumber…"
//                 onChange={(event) =>
//                   setSearch((current) => ({ ...current, search: event.target.value }))
//                 }
//               />
//             </label>
//             {resource.fields.some((f) => f.name === 'area') ? (
//               <label>
//                 Area
//                 <input
//                   value={search.area ?? ''}
//                   placeholder="Bandra"
//                   onChange={(event) =>
//                     setSearch((current) => ({ ...current, area: event.target.value }))
//                   }
//                 />
//               </label>
//             ) : null}
//             {resource.fields.some((f) => f.name === 'category') ? (
//               <label>
//                 Category
//                 <input
//                   value={search.category ?? ''}
//                   placeholder="Cleaning"
//                   onChange={(event) =>
//                     setSearch((current) => ({ ...current, category: event.target.value }))
//                   }
//                 />
//               </label>
//             ) : null}
//             {resource.key === 'browseProviders' ? (
//               <label>
//                 Provider name
//                 <input
//                   value={search.name ?? ''}
//                   placeholder="Lakshmi"
//                   onChange={(event) =>
//                     setSearch((current) => ({ ...current, name: event.target.value }))
//                   }
//                 />
//               </label>
//             ) : null}
//           </div>
//           <div className="row-actions">
//             <button type="submit" disabled={busy}>
//               {t('common.applyFilters')}
//             </button>
//             <button
//               type="button"
//               className="ghost"
//               onClick={() => {
//                 setSearch({})
//                 setTimeout(() => void load(), 0)
//               }}
//             >
//               Clear
//             </button>
//           </div>
//         </form>
//       ) : null}

//       {error ? <p className="banner error">{error}</p> : null}
//       {status ? <p className="banner ok">{status}</p> : null}

//       <div className="table-wrap">
//         <table>
//           <thead>
//             <tr>
//               <th>id</th>
//               {columns.map((column) => (
//                 <th key={column.name}>{column.label}</th>
//               ))}
//               {showActionsColumn ? <th>Actions</th> : null}
//             </tr>
//           </thead>
//           <tbody>
//             {rows.length === 0 ? (
//               <tr>
//                 <td colSpan={columns.length + (showActionsColumn ? 2 : 1)} className="muted">
//                   {busy ? 'Loading…' : 'No rows yet'}
//                 </td>
//               </tr>
//             ) : (
//               rows.map((row) => {
//                 const id = asString(row.id)
//                 const otherUserId = providerUserIdFromRow(row)
//                 return (
//                   <tr key={id}>
//                     <td className="mono">{id.slice(0, 8)}</td>
//                     {columns.map((column) => (
//                       <td key={column.name}>{asString(row[column.name])}</td>
//                     ))}
//                     {showActionsColumn ? (
//                       <td className="row-actions">
//                         {user?.role === 'CUSTOMER' && otherUserId ? (
//                           <button
//                             type="button"
//                             className="ghost"
//                             onClick={() => navigate(`/customer/chat?userId=${otherUserId}`)}
//                           >
//                             💬 Chat
//                           </button>
//                         ) : null}
//                         {canWrite ? (
//                           <>
//                             <button
//                               type="button"
//                               className="ghost"
//                               onClick={() => {
//                                 setEditingId(id)
//                                 setForm(recordToForm(resource, row))
//                               }}
//                             >
//                               Edit
//                             </button>
//                             <button type="button" className="ghost danger" onClick={() => void onDelete(id)}>
//                               Delete
//                             </button>
//                           </>
//                         ) : null}
//                       </td>
//                     ) : null}
//                   </tr>
//                 )
//               })
//             )}
//           </tbody>
//         </table>
//       </div>

//       {canWrite ? (
//         <form className="stack" onSubmit={(event) => void onSubmit(event)}>
//           <h2>{editingId ? 'Edit record' : 'Create record'}</h2>
//           <div className="grid">
//             {resource.fields.map((field) => (
//               <label key={field.name} className={field.type === 'textarea' ? 'span-2' : undefined}>
//                 {field.label}
//                 {field.type === 'textarea' ? (
//                   <textarea
//                     value={form[field.name] ?? ''}
//                     required={field.required}
//                     onChange={(event) =>
//                       setForm((current) => ({ ...current, [field.name]: event.target.value }))
//                     }
//                   />
//                 ) : (
//                   <input
//                     type={field.type === 'number' ? 'number' : 'text'}
//                     step={field.type === 'number' ? 'any' : undefined}
//                     value={form[field.name] ?? ''}
//                     required={field.required}
//                     onChange={(event) =>
//                       setForm((current) => ({ ...current, [field.name]: event.target.value }))
//                     }
//                   />
//                 )}
//               </label>
//             ))}
//           </div>
//           <div className="row-actions">
//             <button type="submit" disabled={busy}>
//               {editingId ? 'Save changes' : 'Create'}
//             </button>
//             {editingId ? (
//               <button
//                 type="button"
//                 className="ghost"
//                 onClick={() => {
//                   setEditingId(null)
//                   setForm(emptyForm(resource))
//                 }}
//               >
//                 Cancel
//               </button>
//             ) : null}
//           </div>
//         </form>
//       ) : null}
//     </section>
//   )
// }

import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { api, getPage } from '../api/http'
import type { AppRole, JsonRecord } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import type { FieldConfig, ResourceConfig } from '../resources'

function asString(value: unknown): string {
  if (value == null) {
    return ''
  }
  if (Array.isArray(value)) {
    return value.join(', ')
  }
  return String(value)
}

function emptyForm(resource: ResourceConfig): Record<string, string> {
  const form: Record<string, string> = {}
  for (const field of resource.fields) {
    form[field.name] = ''
  }
  return form
}

function recordToForm(resource: ResourceConfig, record: JsonRecord): Record<string, string> {
  const form: Record<string, string> = {}
  for (const field of resource.fields) {
    form[field.name] = asString(record[field.name])
  }
  return form
}

function serializeField(field: FieldConfig, raw: string): unknown {
  const trimmed = raw.trim()
  if (field.type === 'number') {
    return trimmed.length === 0 ? null : Number(trimmed)
  }
  if (trimmed.length === 0) {
    return field.required ? trimmed : null
  }
  return raw
}

function toPayload(resource: ResourceConfig, form: Record<string, string>): JsonRecord {
  const payload: JsonRecord = {}
  for (const field of resource.fields) {
    payload[field.name] = serializeField(field, form[field.name] ?? '')
  }
  return payload
}

function listUrl(resource: ResourceConfig, search: Record<string, string>): string {
  const params = new URLSearchParams(resource.listParams)
  for (const [key, value] of Object.entries(search)) {
    if (value.trim().length > 0) {
      params.set(key, value.trim())
    }
  }
  const query = params.toString()
  return query.length > 0 ? `${resource.apiPath}?${query}` : resource.apiPath
}

function providerUserIdFromRow(row: JsonRecord): string | null {
  const candidate =
    row.providerUserId ?? row.providerId ?? (row.provider as JsonRecord | null)?.id ?? row.provider ?? row.id
  const value = candidate == null ? '' : String(candidate)
  return value.length > 0 ? value : null
}

interface ResourceCrudProps {
  resource: ResourceConfig
}

export function ResourceCrud({ resource }: ResourceCrudProps) {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { user } = useAuth()
  const canWrite = user != null && resource.writeRoles.includes(user.role as AppRole)
  const [rows, setRows] = useState<JsonRecord[]>([])
  const [total, setTotal] = useState(0)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [form, setForm] = useState<Record<string, string>>(() => emptyForm(resource))
  const [search, setSearch] = useState<Record<string, string>>({})
  const [status, setStatus] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const load = useCallback(async () => {
    setBusy(true)
    setError(null)
    try {
      const page = await getPage<JsonRecord>(listUrl(resource, search))
      setRows(page.content)
      setTotal(page.totalElements)
    } catch (err) {
      setError(err instanceof Error ? err.message : t('common.failedToLoad'))
    } finally {
      setBusy(false)
    }
  }, [resource, search, t])

  useEffect(() => {
    setEditingId(null)
    setForm(emptyForm(resource))
    setSearch({})
    void load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [resource])

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    if (!canWrite) {
      return
    }
    setBusy(true)
    setError(null)
    setStatus(null)
    try {
      const payload = toPayload(resource, form)
      if (editingId) {
        await api(`${resource.apiPath}/${editingId}`, {
          method: 'PUT',
          body: JSON.stringify(payload),
        })
        setStatus(t('common.updated'))
      } else {
        await api(resource.apiPath, {
          method: 'POST',
          body: JSON.stringify(payload),
        })
        setStatus(t('common.created'))
      }
      setEditingId(null)
      setForm(emptyForm(resource))
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : t('common.saveFailed'))
    } finally {
      setBusy(false)
    }
  }

  async function onDelete(id: string) {
    if (!canWrite) {
      return
    }
    setBusy(true)
    setError(null)
    try {
      await api(`${resource.apiPath}/${id}`, { method: 'DELETE' })
      setStatus(t('common.deleted'))
      if (editingId === id) {
        setEditingId(null)
        setForm(emptyForm(resource))
      }
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : t('common.deleteFailed'))
    } finally {
      setBusy(false)
    }
  }

  const columns = resource.fields.slice(0, 5)
  const showActionsColumn = canWrite || user?.role === 'CUSTOMER'

  return (
    <section className="panel">
      <header className="panel-head">
        <div>
          <h1>{t(resource.title)}</h1>
          <p className="muted">
            {resource.apiPath}
            {resource.listParams ? `?${new URLSearchParams(resource.listParams).toString()}` : ''} ·{' '}
            {total} records
            {canWrite ? '' : ' · read only'}
          </p>
        </div>
        <button type="button" className="ghost" onClick={() => void load()} disabled={busy}>
          {t('common.refresh')}
        </button>
      </header>

      {resource.searchable ? (
        <form
          className="stack"
          onSubmit={(event) => {
            event.preventDefault()
            void load()
          }}
        >
          <div className="grid">
            <label>
              {t('common.search')}
              <input
                value={search.search ?? ''}
                placeholder="cooking, plumber…"
                onChange={(event) =>
                  setSearch((current) => ({ ...current, search: event.target.value }))
                }
              />
            </label>
            {resource.fields.some((f) => f.name === 'area') ? (
              <label>
                {t('filters.area')}
                <input
                  value={search.area ?? ''}
                  placeholder="Bandra"
                  onChange={(event) =>
                    setSearch((current) => ({ ...current, area: event.target.value }))
                  }
                />
              </label>
            ) : null}
            {resource.fields.some((f) => f.name === 'category') ? (
              <label>
                {t('filters.category')}
                <input
                  value={search.category ?? ''}
                  placeholder="Cleaning"
                  onChange={(event) =>
                    setSearch((current) => ({ ...current, category: event.target.value }))
                  }
                />
              </label>
            ) : null}
            {resource.key === 'browseProviders' ? (
              <label>
                {t('filters.providerName')}
                <input
                  value={search.name ?? ''}
                  placeholder="Lakshmi"
                  onChange={(event) =>
                    setSearch((current) => ({ ...current, name: event.target.value }))
                  }
                />
              </label>
            ) : null}
          </div>
          <div className="row-actions">
            <button type="submit" disabled={busy}>
              {t('common.applyFilters')}
            </button>
            <button
              type="button"
              className="ghost"
              onClick={() => {
                setSearch({})
                setTimeout(() => void load(), 0)
              }}
            >
              {t('common.clear')}
            </button>
          </div>
        </form>
      ) : null}

      {error ? <p className="banner error">{error}</p> : null}
      {status ? <p className="banner ok">{status}</p> : null}

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>id</th>
              {columns.map((column) => (
                <th key={column.name}>{t(column.label)}</th>
              ))}
              {showActionsColumn ? <th>{t('common.actions')}</th> : null}
            </tr>
          </thead>
          <tbody>
            {rows.length === 0 ? (
              <tr>
                <td colSpan={columns.length + (showActionsColumn ? 2 : 1)} className="muted">
                  {busy ? t('common.loading') : t('common.noRows')}
                </td>
              </tr>
            ) : (
              rows.map((row) => {
                const id = asString(row.id)
                const otherUserId = providerUserIdFromRow(row)
                return (
                  <tr key={id}>
                    <td className="mono">{id.slice(0, 8)}</td>
                    {columns.map((column) => (
                      <td key={column.name}>{asString(row[column.name])}</td>
                    ))}
                    {showActionsColumn ? (
                      <td className="row-actions">
                        {user?.role === 'CUSTOMER' && otherUserId ? (
                          <button
                            type="button"
                            className="ghost"
                            onClick={() => navigate(`/customer/chat?userId=${otherUserId}`)}
                          >
                            💬 {t('common.chat')}
                          </button>
                        ) : null}
                        {canWrite ? (
                          <>
                            <button
                              type="button"
                              className="ghost"
                              onClick={() => {
                                setEditingId(id)
                                setForm(recordToForm(resource, row))
                              }}
                            >
                              {t('common.edit')}
                            </button>
                            <button type="button" className="ghost danger" onClick={() => void onDelete(id)}>
                              {t('common.delete')}
                            </button>
                          </>
                        ) : null}
                      </td>
                    ) : null}
                  </tr>
                )
              })
            )}
          </tbody>
        </table>
      </div>

      {canWrite ? (
        <form className="stack" onSubmit={(event) => void onSubmit(event)}>
          <h2>
            {editingId ? t('common.editRecord') : t('common.createRecord')}
          </h2>
          <div className="grid">
            {resource.fields.map((field) => (
              <label key={field.name} className={field.type === 'textarea' ? 'span-2' : undefined}>
                {t(field.label)}
                {field.type === 'textarea' ? (
                  <textarea
                    value={form[field.name] ?? ''}
                    required={field.required}
                    onChange={(event) =>
                      setForm((current) => ({ ...current, [field.name]: event.target.value }))
                    }
                  />
                ) : (
                  <input
                    type={field.type === 'number' ? 'number' : 'text'}
                    step={field.type === 'number' ? 'any' : undefined}
                    value={form[field.name] ?? ''}
                    required={field.required}
                    onChange={(event) =>
                      setForm((current) => ({ ...current, [field.name]: event.target.value }))
                    }
                  />
                )}
              </label>
            ))}
          </div>
          <div className="row-actions">
            <button type="submit" disabled={busy}>
              {editingId ? t('common.saveChanges') : t('common.create')}
            </button>
            {editingId ? (
              <button
                type="button"
                className="ghost"
                onClick={() => {
                  setEditingId(null)
                  setForm(emptyForm(resource))
                }}
              >
                {t('common.cancel')}
              </button>
            ) : null}
          </div>
        </form>
      ) : null}
    </section>
  )
}