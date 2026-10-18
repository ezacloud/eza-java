package ke.co.eza.models;

public class WebhookEvent {
    private String id;
    private String type;
    private String createdAt;
    private boolean livemode;
    private String rawData;

    public WebhookEvent() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isLivemode() { return livemode; }
    public void setLivemode(boolean livemode) { this.livemode = livemode; }

    public String getRawData() { return rawData; }
    public void setRawData(String rawData) { this.rawData = rawData; }
}
