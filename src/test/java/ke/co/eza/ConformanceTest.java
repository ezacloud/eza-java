package ke.co.eza;

import ke.co.eza.models.WebhookEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class ConformanceTest {

    @Test
    public void testWebhookValidVector() {
        String secret = "whsec_test_0123456789abcdef";
        long timestamp = 1790538762L;
        String header = "t=1790538762,v1=5ed06d16182620e9aee7fd9d3b02a369038ccbdd656de90a45b004c9fc51c52b";
        String body = "{\"id\":\"evt_01\",\"type\":\"payment.completed\",\"livemode\":false,\"data\":{\"amount_kes\":1500}}";

        Instant now = Instant.ofEpochSecond(timestamp + 30);
        WebhookEvent event = Webhook.verify(body, header, secret, 300, now);

        assertEquals("evt_01", event.getId());
        assertEquals("payment.completed", event.getType());
        assertFalse(event.isLivemode());
    }

    @Test
    public void testWebhookInvalidCases() {
        String secret = "whsec_test_0123456789abcdef";
        String header = "t=1790538762,v1=5ed06d16182620e9aee7fd9d3b02a369038ccbdd656de90a45b004c9fc51c52b";
        String body = "{\"id\":\"evt_01\",\"type\":\"payment.completed\",\"livemode\":false,\"data\":{\"amount_kes\":1500}}";

        // Tampered body
        assertThrows(SignatureException.class, () -> {
            Webhook.verify("{\"tampered\":true}", header, secret, 300, Instant.ofEpochSecond(1790538792L));
        });

        // Wrong secret
        assertThrows(SignatureException.class, () -> {
            Webhook.verify(body, header, "whsec_test_other", 300, Instant.ofEpochSecond(1790538792L));
        });

        // Expired timestamp
        assertThrows(SignatureException.class, () -> {
            Webhook.verify(body, header, secret, 300, Instant.ofEpochSecond(1790539362L));
        });

        // Malformed header
        assertThrows(SignatureException.class, () -> {
            Webhook.verify(body, "v1=abc", secret, 300, Instant.ofEpochSecond(1790538792L));
        });
    }
}
