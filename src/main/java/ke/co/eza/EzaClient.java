package ke.co.eza;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EzaClient {
    public static final String DEFAULT_EMAIL_URL = "https://email.eza.co.ke";
    public static final String DEFAULT_PAYEDGE_URL = "https://payedge.eza.co.ke";

    public enum ServiceType {
        PAYEDGE,
        EMAIL
    }

    private final String apiKey;
    private final String emailKey;
    private final String emailUrl;
    private final String payedgeUrl;
    private final int maxRetries;
    private final HttpClient httpClient;

    private final PayEdge payedge;
    private final Emails emails;

    public static class Builder {
        private String apiKey;
        private String emailKey;
        private String emailUrl;
        private String payedgeUrl;
        private int maxRetries = 2;
        private HttpClient httpClient;

        public Builder apiKey(String apiKey) { this.apiKey = apiKey; return this; }
        public Builder emailKey(String emailKey) { this.emailKey = emailKey; return this; }
        public Builder emailUrl(String emailUrl) { this.emailUrl = emailUrl; return this; }
        public Builder payedgeUrl(String payedgeUrl) { this.payedgeUrl = payedgeUrl; return this; }
        public Builder maxRetries(int maxRetries) { this.maxRetries = maxRetries; return this; }
        public Builder httpClient(HttpClient httpClient) { this.httpClient = httpClient; return this; }

        public EzaClient build() {
            return new EzaClient(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public EzaClient() {
        this(new Builder());
    }

    public EzaClient(String apiKey) {
        this(new Builder().apiKey(apiKey));
    }

    private EzaClient(Builder builder) {
        this.apiKey = builder.apiKey != null ? builder.apiKey : System.getenv("EZA_API_KEY");
        String envEmailKey = System.getenv("EZA_EMAIL_KEY");
        this.emailKey = builder.emailKey != null ? builder.emailKey : (envEmailKey != null ? envEmailKey : this.apiKey);

        String envEmailUrl = System.getenv("EZA_EMAIL_URL");
        this.emailUrl = trimSlash(builder.emailUrl != null ? builder.emailUrl : (envEmailUrl != null ? envEmailUrl : DEFAULT_EMAIL_URL));

        String envPayEdgeUrl = System.getenv("EZA_PAYEDGE_URL");
        this.payedgeUrl = trimSlash(builder.payedgeUrl != null ? builder.payedgeUrl : (envPayEdgeUrl != null ? envPayEdgeUrl : DEFAULT_PAYEDGE_URL));

        this.maxRetries = builder.maxRetries;
        this.httpClient = builder.httpClient != null ? builder.httpClient : HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

        this.payedge = new PayEdge(this);
        this.emails = new Emails(this);
    }

    public PayEdge payedge() { return payedge; }
    public Emails emails() { return emails; }
    public String getEmailUrl() { return emailUrl; }
    public String getPayEdgeUrl() { return payedgeUrl; }

    public String request(String method, String url, ServiceType service, String jsonBody, String idempotencyKey) {
        String authKey = service == ServiceType.EMAIL ? emailKey : apiKey;
        if (authKey == null || authKey.isEmpty()) {
            String serviceName = service == ServiceType.EMAIL ? "Email" : "PayEdge";
            String envVar = service == ServiceType.EMAIL ? "EZA_EMAIL_KEY or EZA_API_KEY" : "EZA_API_KEY";
            throw new IllegalStateException("No API key configured for " + serviceName + ". Set via builder or " + envVar);
        }

        if ("POST".equalsIgnoreCase(method) && (idempotencyKey == null || idempotencyKey.isEmpty())) {
            idempotencyKey = UUID.randomUUID().toString();
        }

        int attempts = 0;
        while (true) {
            attempts++;
            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + authKey)
                .header("Accept", "application/json")
                .header("User-Agent", "eza-java/0.2.0")
                .timeout(Duration.ofSeconds(30));

            if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
                reqBuilder.header("Idempotency-Key", idempotencyKey);
            }

            if ("POST".equalsIgnoreCase(method)) {
                reqBuilder.header("Content-Type", "application/json");
                reqBuilder.POST(HttpRequest.BodyPublishers.ofString(jsonBody != null ? jsonBody : "{}"));
            } else if ("DELETE".equalsIgnoreCase(method)) {
                reqBuilder.DELETE();
            } else {
                reqBuilder.GET();
            }

            HttpResponse<String> response;
            try {
                response = httpClient.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                if (attempts <= maxRetries) {
                    backoff(attempts);
                    continue;
                }
                throw new RuntimeException("Network request failed: " + e.getMessage(), e);
            }

            int status = response.statusCode();

            // 429 Rate Limit (honor Retry-After)
            if (status == 429 && attempts <= maxRetries) {
                Optional<String> retryAfter = response.headers().firstValue("Retry-After");
                double delaySec = 0.2 * Math.pow(2, attempts - 1);
                if (retryAfter.isPresent()) {
                    try {
                        delaySec = Math.max(Double.parseDouble(retryAfter.get()), 0.05);
                    } catch (NumberFormatException ignored) {}
                }
                sleepMs((long) (delaySec * 1000));
                continue;
            }

            // 503 / 504 Service Unavailable / Gateway Timeout
            if ((status == 503 || status == 504) && attempts <= maxRetries) {
                backoff(attempts);
                continue;
            }

            String body = response.body();
            if (status >= 200 && status < 300) {
                return body;
            }

            // Parse error details
            String requestId = response.headers().firstValue("eza-request-id").orElse(null);
            String code = extractPattern(body, "\"code\"\\s*:\\s*\"([^\"]*)\"");
            if (code == null) code = extractPattern(body, "\"name\"\\s*:\\s*\"([^\"]*)\"");

            String message = extractPattern(body, "\"message\"\\s*:\\s*\"([^\"]*)\"");
            if (message == null) message = extractPattern(body, "\"detail\"\\s*:\\s*\"([^\"]*)\"");

            throw new EzaException(message != null ? message : body, status, code, requestId, new ArrayList<>());
        }
    }

    private static void backoff(int attempt) {
        long delay = (long) (100 * Math.pow(2, attempt - 1));
        sleepMs(delay);
    }

    private static void sleepMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String extractPattern(String text, String regex) {
        Matcher m = Pattern.compile(regex).matcher(text);
        return m.find() ? m.group(1) : null;
    }

    private static String trimSlash(String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}
