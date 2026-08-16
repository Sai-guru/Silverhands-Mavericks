package io.bootify.silverhands.config.security;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;


/**
 * Handshake interceptor that requires an authenticated HTTP session before
 * allowing the WebSocket upgrade, and copies the Authentication into the
 * WebSocket session attributes so it can be attached to the STOMP session.
 */
@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    public static final String AUTHENTICATION_ATTRIBUTE = "authentication";

    @Override
    public boolean beforeHandshake(final ServerHttpRequest request,
            final ServerHttpResponse response, final WebSocketHandler wsHandler,
            final Map<String, Object> attributes) throws Exception {

        // Spring Security stores the authenticated principal in the HTTP session
        // (SPRING_SECURITY_CONTEXT) for session-based OAuth2 login. The security
        // context is also available via SecurityContextHolder during the handshake.
        final Authentication authentication = getAuthentication(request);

        if (authentication == null || !authentication.isAuthenticated()) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        attributes.put(AUTHENTICATION_ATTRIBUTE, authentication);
        return true;
    }

    @Override
    public void afterHandshake(final ServerHttpRequest request,
            final ServerHttpResponse response, final WebSocketHandler wsHandler,
            final Exception exception) {
        // no-op
    }

    private Authentication getAuthentication(final ServerHttpRequest request) {
        final SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext.getAuthentication() != null
                && securityContext.getAuthentication().isAuthenticated()) {
            return securityContext.getAuthentication();
        }
        if (request instanceof ServletServerHttpRequest servletRequest) {
            final jakarta.servlet.http.HttpSession session =
                    servletRequest.getServletRequest().getSession(false);
            if (session != null) {
                final Object context = session.getAttribute("SPRING_SECURITY_CONTEXT");
                if (context instanceof SecurityContext secContext) {
                    return secContext.getAuthentication();
                }
            }
        }
        return null;
    }

}
