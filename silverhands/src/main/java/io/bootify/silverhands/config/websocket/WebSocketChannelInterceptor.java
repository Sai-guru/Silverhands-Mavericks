package io.bootify.silverhands.config.websocket;

import static io.bootify.silverhands.config.security.AuthHandshakeInterceptor.AUTHENTICATION_ATTRIBUTE;
import java.util.Map;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;


/**
 * Channel interceptor that attaches the authenticated user (captured during the
 * WebSocket handshake) to the STOMP session, so @MessageMapping handlers can
 * receive the Principal and derive the sender server-side.
 */
@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(final Message<?> message, final MessageChannel channel) {
        final StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            final Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            final Authentication authentication = sessionAttributes == null ? null
                    : (Authentication) sessionAttributes.get(
                            AUTHENTICATION_ATTRIBUTE);

            if (authentication == null || !authentication.isAuthenticated()) {
                throw new MessageDeliveryException(
                        "WebSocket connection rejected: user not authenticated");
            }
            accessor.setUser(authentication);
        }

        return message;
    }

}
