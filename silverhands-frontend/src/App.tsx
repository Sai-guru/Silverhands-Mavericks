import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import { RequireRole } from './auth/RequireRole'
import { Layout } from './components/Layout'
import { AiChatPage } from './pages/AiChatPage'
import { AuthCallbackPage } from './pages/AuthCallbackPage'
import { ChatPage } from './pages/ChatPage'
import { HomePage } from './pages/HomePage'
import { LoginPage } from './pages/LoginPage'
import { ResourcePage } from './pages/ResourcePage'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<LoginPage />} />
          <Route path="/auth/callback" element={<AuthCallbackPage />} />

          <Route element={<RequireRole role="CUSTOMER" />}>
            <Route path="/customer" element={<Layout role="CUSTOMER" />}>
              <Route index element={<HomePage role="CUSTOMER" />} />
              <Route path="ai" element={<AiChatPage />} />
              <Route path="chat" element={<ChatPage />} />
              <Route path=":resourcePath" element={<ResourcePage role="CUSTOMER" />} />
            </Route>
          </Route>

          <Route element={<RequireRole role="PROVIDER" />}>
            <Route path="/provider" element={<Layout role="PROVIDER" />}>
              <Route index element={<HomePage role="PROVIDER" />} />
              <Route path="ai" element={<AiChatPage />} />
              <Route path="chat" element={<ChatPage />} />
              <Route path=":resourcePath" element={<ResourcePage role="PROVIDER" />} />
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
