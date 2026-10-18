package ke.co.eza;

import ke.co.eza.models.EmailResponse;
import ke.co.eza.models.SendEmailParams;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Emails {
    private final EzaClient client;

    public Emails(EzaClient client) {
        this.client = client;
    }

    public EmailResponse send(SendEmailParams params) {
        return send(params, null);
    }

    public EmailResponse send(SendEmailParams params, String idempotencyKey) {
        if (params == null) throw new IllegalArgumentException("params cannot be null");
        if (params.getFrom() == null || params.getFrom().isEmpty()) throw new IllegalArgumentException("from is required");
        if (params.getTo() == null || params.getTo().isEmpty()) throw new IllegalArgumentException("to is required");
        if (params.getSubject() == null || params.getSubject().isEmpty()) throw new IllegalArgumentException("subject is required");
        if ((params.getText() == null || params.getText().isEmpty()) && (params.getHtml() == null || params.getHtml().isEmpty())) {
            throw new IllegalArgumentException("Either text or html content must be provided");
        }

        StringBuilder json = new StringBuilder("{");
        json.append("\"from\":").append(quote(params.getFrom()));
        json.append(",\"to\":[");
        for (int i = 0; i < params.getTo().size(); i++) {
            if (i > 0) json.append(",");
            json.append(quote(params.getTo().get(i)));
        }
        json.append("],\"subject\":").append(quote(params.getSubject()));

        if (params.getText() != null) json.append(",\"text\":").append(quote(params.getText()));
        if (params.getHtml() != null) json.append(",\"html\":").append(quote(params.getHtml()));
        if (params.getReplyTo() != null) json.append(",\"reply_to\":").append(quote(params.getReplyTo()));

        if (params.getCc() != null && !params.getCc().isEmpty()) {
            json.append(",\"cc\":[");
            for (int i = 0; i < params.getCc().size(); i++) {
                if (i > 0) json.append(",");
                json.append(quote(params.getCc().get(i)));
            }
            json.append("]");
        }

        if (params.getBcc() != null && !params.getBcc().isEmpty()) {
            json.append(",\"bcc\":[");
            for (int i = 0; i < params.getBcc().size(); i++) {
                if (i > 0) json.append(",");
                json.append(quote(params.getBcc().get(i)));
            }
            json.append("]");
        }

        if (params.getAttachments() != null && !params.getAttachments().isEmpty()) {
            json.append(",\"attachments\":[");
            for (int i = 0; i < params.getAttachments().size(); i++) {
                if (i > 0) json.append(",");
                SendEmailParams.Attachment att = params.getAttachments().get(i);
                json.append("{\"filename\":").append(quote(att.getFilename()))
                    .append(",\"content\":").append(quote(att.getContent()))
                    .append("}");
            }
            json.append("]");
        }

        json.append("}");

        String responseBody = client.request(
            "POST",
            client.getEmailUrl() + "/emails",
            EzaClient.ServiceType.EMAIL,
            json.toString(),
            idempotencyKey
        );

        Pattern p = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(responseBody);
        String id = m.find() ? m.group(1) : "";
        return new EmailResponse(id);
    }

    public String get(String id) {
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("id is required");

        String escaped = URLEncoder.encode(id, StandardCharsets.UTF_8);
        return client.request(
            "GET",
            client.getEmailUrl() + "/emails/" + escaped,
            EzaClient.ServiceType.EMAIL,
            null,
            null
        );
    }

    private static String quote(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }
}
