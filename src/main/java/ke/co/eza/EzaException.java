package ke.co.eza;

import java.util.List;

public class EzaException extends RuntimeException {
    private final int status;
    private final String code;
    private final String requestId;
    private final List<FieldError> fields;

    public static class FieldError {
        private final String field;
        private final String code;
        private final String message;

        public FieldError(String field, String code, String message) {
            this.field = field;
            this.code = code;
            this.message = message;
        }

        public String getField() { return field; }
        public String getCode() { return code; }
        public String getMessage() { return message; }
    }

    public EzaException(String message, int status, String code, String requestId, List<FieldError> fields) {
        super(formatMessage(message, status, code, requestId));
        this.status = status;
        this.code = code;
        this.requestId = requestId;
        this.fields = fields;
    }

    public int getStatus() { return status; }
    public String getCode() { return code; }
    public String getRequestId() { return requestId; }
    public List<FieldError> getFields() { return fields; }

    private static String formatMessage(String message, int status, String code, String requestId) {
        StringBuilder sb = new StringBuilder();
        if (code != null && !code.isEmpty()) {
            sb.append("eza [").append(code).append("]: ");
        } else {
            sb.append("eza: ");
        }
        sb.append(message != null ? message : "HTTP " + status);
        if (status > 0) {
            sb.append(" (status ").append(status).append(")");
        }
        if (requestId != null && !requestId.isEmpty()) {
            sb.append(" (request_id: ").append(requestId).append(")");
        }
        return sb.toString();
    }
}
