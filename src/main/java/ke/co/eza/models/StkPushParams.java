package ke.co.eza.models;

public class StkPushParams {
    private String phone;
    private long amountKes;
    private String accountReference;
    private String description;
    private String configId;
    private String idempotencyKey;

    public StkPushParams() {}

    public StkPushParams(String phone, long amountKes, String accountReference) {
        this.phone = phone;
        this.amountKes = amountKes;
        this.accountReference = accountReference;
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public long getAmountKes() { return amountKes; }
    public void setAmountKes(long amountKes) { this.amountKes = amountKes; }

    public String getAccountReference() { return accountReference; }
    public void setAccountReference(String accountReference) { this.accountReference = accountReference; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getConfigId() { return configId; }
    public void setConfigId(String configId) { this.configId = configId; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
