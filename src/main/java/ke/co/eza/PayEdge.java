package ke.co.eza;

import ke.co.eza.models.StkPushParams;
import ke.co.eza.models.Transaction;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PayEdge {
    private final EzaClient client;

    public PayEdge(EzaClient client) {
        this.client = client;
    }

    public Transaction stkPush(StkPushParams params) {
        if (params == null) throw new IllegalArgumentException("params cannot be null");
        if (params.getAmountKes() < 1) throw new IllegalArgumentException("amountKes must be at least 1 KES");
        if (params.getPhone() == null || params.getPhone().isEmpty()) throw new IllegalArgumentException("phone is required");
        if (params.getAccountReference() == null || params.getAccountReference().isEmpty()) throw new IllegalArgumentException("accountReference is required");

        StringBuilder json = new StringBuilder("{");
        json.append("\"phone\":").append(quote(params.getPhone()));
        json.append(",\"amount_kes\":").append(params.getAmountKes());
        json.append(",\"account_reference\":").append(quote(params.getAccountReference()));
        if (params.getDescription() != null) {
            json.append(",\"description\":").append(quote(params.getDescription()));
        }
        if (params.getConfigId() != null) {
            json.append(",\"config_id\":").append(quote(params.getConfigId()));
        }
        if (params.getIdempotencyKey() != null) {
            json.append(",\"idempotency_key\":").append(quote(params.getIdempotencyKey()));
        }
        json.append("}");

        String responseBody = client.request(
            "POST",
            client.getPayEdgeUrl() + "/v1/stk-push",
            EzaClient.ServiceType.PAYEDGE,
            json.toString(),
            params.getIdempotencyKey()
        );

        return parseTransaction(responseBody);
    }

    public Transaction getTransaction(String reference) {
        if (reference == null || reference.isEmpty()) throw new IllegalArgumentException("reference is required");

        String escaped = URLEncoder.encode(reference, StandardCharsets.UTF_8);
        String responseBody = client.request(
            "GET",
            client.getPayEdgeUrl() + "/v1/transactions/" + escaped,
            EzaClient.ServiceType.PAYEDGE,
            null,
            null
        );

        return parseTransaction(responseBody);
    }

    private static Transaction parseTransaction(String json) {
        Transaction tx = new Transaction();
        tx.setId(extractString(json, "id"));
        tx.setConfigId(extractString(json, "config_id"));
        tx.setCheckoutRequestId(extractString(json, "checkout_request_id"));
        tx.setStatus(extractString(json, "status"));
        tx.setPhone(extractString(json, "phone"));
        tx.setAccountReference(extractString(json, "account_reference"));
        tx.setMpesaReceipt(extractString(json, "mpesa_receipt"));
        tx.setResultDescription(extractString(json, "result_description"));
        tx.setCreatedAt(extractString(json, "created_at"));
        tx.setSettledAt(extractString(json, "settled_at"));

        Long amount = extractLong(json, "amount_kes");
        if (amount != null) tx.setAmountKes(amount);

        Boolean livemode = extractBoolean(json, "livemode");
        if (livemode != null) tx.setLivemode(livemode);

        return tx;
    }

    private static String quote(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }

    private static String extractString(String json, String field) {
        Pattern p = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private static Long extractLong(String json, String field) {
        Pattern p = Pattern.compile("\"" + field + "\"\\s*:\\s*(\\d+)");
        Matcher m = p.matcher(json);
        return m.find() ? Long.parseLong(m.group(1)) : null;
    }

    private static Boolean extractBoolean(String json, String field) {
        Pattern p = Pattern.compile("\"" + field + "\"\\s*:\\s*(true|false)");
        Matcher m = p.matcher(json);
        return m.find() ? Boolean.parseBoolean(m.group(1)) : null;
    }
}
