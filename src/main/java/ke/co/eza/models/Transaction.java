package ke.co.eza.models;

public class Transaction {
    private String id;
    private String configId;
    private String checkoutRequestId;
    private String status;
    private long amountKes;
    private String phone;
    private String accountReference;
    private String mpesaReceipt;
    private String resultDescription;
    private boolean livemode;
    private String createdAt;
    private String settledAt;

    public Transaction() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConfigId() { return configId; }
    public void setConfigId(String configId) { this.configId = configId; }

    public String getCheckoutRequestId() { return checkoutRequestId; }
    public void setCheckoutRequestId(String checkoutRequestId) { this.checkoutRequestId = checkoutRequestId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getAmountKes() { return amountKes; }
    public void setAmountKes(long amountKes) { this.amountKes = amountKes; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAccountReference() { return accountReference; }
    public void setAccountReference(String accountReference) { this.accountReference = accountReference; }

    public String getMpesaReceipt() { return mpesaReceipt; }
    public void setMpesaReceipt(String mpesaReceipt) { this.mpesaReceipt = mpesaReceipt; }

    public String getResultDescription() { return resultDescription; }
    public void setResultDescription(String resultDescription) { this.resultDescription = resultDescription; }

    public boolean isLivemode() { return livemode; }
    public void setLivemode(boolean livemode) { this.livemode = livemode; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getSettledAt() { return settledAt; }
    public void setSettledAt(String settledAt) { this.settledAt = settledAt; }
}
