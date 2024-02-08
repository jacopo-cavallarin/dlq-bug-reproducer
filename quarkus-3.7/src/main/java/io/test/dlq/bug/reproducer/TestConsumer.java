package io.test.dlq.bug.reproducer;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;

@ApplicationScoped
public class TestConsumer {
    public record TestPayload(boolean shouldAck) {}

    private final Logger log = getLogger(getClass().getName());

    @Incoming("test-consumer")
    public CompletionStage<Void> consume(Message<TestPayload> message) {
        var payload = message.getPayload();

        if (payload.shouldAck()) {
            log.info(() -> "acking message %s".formatted(message));
            return message.ack();
        } else {
            log.severe(() -> "nacking message %s".formatted(message));
            return message.nack(new IllegalArgumentException("shouldAck was false"));
        }
    }
}
