package ke.co.eza;

import ke.co.eza.models.WebhookEvent;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Webhook {
    public static final int DEFAULT_TOLERANCE_SECONDS = 300;

    private static final Pattern PATTERN_T = Pattern.compile("t=(\\d+)");
    private static final Pattern PATTERN_V1 = Pattern.compile("v1=([0-9a-fA-F]+)");

    private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern TYPE_PATTERN = Pattern.compile("\"type\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern CREATED_AT_PATTERN = Pattern.compile("\"created_at\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern LIVEMODE_PATTERN = Pattern.compile("\"livemode\"\\s*:\\s*(true|false)");

    private Webhook() {}

    public static WebhookEvent verify(String rawBody, String signatureHeader, String secret) {
        return verify(rawBody, signatureHeader, secret, DEFAULT_TOLERANCE_SECONDS, Instant.now());
    }

    public static WebhookEvent verify(String rawBody, String signatureHeader, String secret, int toleranceSeconds, Instant now) {
        if (rawBody == null || rawBody.isEmpty()) {
            throw new SignatureException("raw body cannot be empty");
        }
        if (signatureHeader == null || signatureHeader.isEmpty()) {
            throw new SignatureException("signature header cannot be empty");
        }
        if (secret == null || secret.isEmpty()) {
            throw new SignatureException("secret cannot be empty");
        }

        Long timestamp = null;
        String signature = null;

        Matcher matcherT = PATTERN_T.matcher(signatureHeader);
        if (matcherT.find()) {
            timestamp = Long.parseLong(matcherT.group(1));
        }

        Matcher matcherV1 = PATTERN_V1.matcher(signatureHeader);
        if (matcherV1.find()) {
            signature = matcherV1.group(1).toLowerCase();
        }

        if (timestamp == null || signature == null) {
            throw new SignatureException("malformed signature header: missing timestamp or v1 signature");
        }

        Instant currentTime = now != null ? now : Instant.now();
        long diff = Math.abs(currentTime.getEpochSecond() - timestamp);
        if (diff > toleranceSeconds) {
            throw new SignatureException("timestamp outside tolerance zone (" + toleranceSeconds + "s)");
        }

        String payloadToSign = timestamp + "." + rawBody;
        String computedSignature = computeHmacSha256(payloadToSign, secret);

        if (!MessageDigest.isEqual(computedSignature.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8))) {
            throw new SignatureException("signature mismatch");
        }

        WebhookEvent event = new WebhookEvent();
        event.setRawData(rawBody);

        Matcher m = ID_PATTERN.matcher(rawBody);
        if (m.find()) event.setId(m.group(1));

        m = TYPE_PATTERN.matcher(rawBody);
        if (m.find()) event.setType(m.group(1));

        m = CREATED_AT_PATTERN.matcher(rawBody);
        if (m.find()) event.setCreatedAt(m.group(1));

        m = LIVEMODE_PATTERN.matcher(rawBody);
        if (m.find()) event.setLivemode(Boolean.parseBoolean(m.group(1)));

        return event;
    }

    private static String computeHmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(rawHmac.length * 2);
            for (byte b : rawHmac) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC-SHA256", e);
        }
    }
}
