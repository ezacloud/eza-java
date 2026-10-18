package ke.co.eza.models;

import java.util.ArrayList;
import java.util.List;

public class SendEmailParams {
    private String from;
    private List<String> to = new ArrayList<>();
    private String subject;
    private String text;
    private String html;
    private List<String> cc;
    private List<String> bcc;
    private String replyTo;
    private List<Attachment> attachments;

    public static class Attachment {
        private String filename;
        private String content; // base64 encoded

        public Attachment() {}
        public Attachment(String filename, String content) {
            this.filename = filename;
            this.content = content;
        }

        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public SendEmailParams() {}

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public List<String> getTo() { return to; }
    public void setTo(List<String> to) { this.to = to; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getHtml() { return html; }
    public void setHtml(String html) { this.html = html; }

    public List<String> getCc() { return cc; }
    public void setCc(List<String> cc) { this.cc = cc; }

    public List<String> getBcc() { return bcc; }
    public void setBcc(List<String> bcc) { this.bcc = bcc; }

    public String getReplyTo() { return replyTo; }
    public void setReplyTo(String replyTo) { this.replyTo = replyTo; }

    public List<Attachment> getAttachments() { return attachments; }
    public void setAttachments(List<Attachment> attachments) { this.attachments = attachments; }
}
