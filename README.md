# Eza Cloud Java SDK (`ke.co.eza:eza-sdk`)

Official Java client library for [Eza Cloud](https://eza.co.ke): PayEdge M-Pesa STK push, transactional emails, and cryptographically signed webhook verification.

Zero external dependencies. Pure standard library (`java.net.http`, `javax.crypto`). Compatible with Java 17+ and any JVM language (Java, Kotlin, Scala, Clojure).

---

## Installation

### Maven
```xml
<dependency>
    <groupId>ke.co.eza</groupId>
    <artifactId>eza-sdk</artifactId>
    <version>0.2.0</version>
</dependency>
```

### Gradle
```groovy
implementation 'ke.co.eza:eza-sdk:0.2.0'
```

---

## Usage

```java
import ke.co.eza.EzaClient;
import ke.co.eza.Webhook;
import ke.co.eza.models.StkPushParams;
import ke.co.eza.models.Transaction;
import ke.co.eza.models.SendEmailParams;
import ke.co.eza.models.EmailResponse;
import ke.co.eza.models.WebhookEvent;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Auto-configures from EZA_API_KEY, EZA_EMAIL_KEY on Eza Cloud pods
        EzaClient eza = EzaClient.builder()
            .apiKey("eza_live_...")
            .build();

        // 1. Initiate PayEdge M-Pesa STK Push
        Transaction tx = eza.payedge().stkPush(
            new StkPushParams("254712345678", 1500, "INV1024")
        );
        System.out.println("Checkout ID: " + tx.getCheckoutRequestId());

        // 2. Send transactional email
        SendEmailParams mailParams = new SendEmailParams();
        mailParams.setFrom("Shop <orders@mail.shop.co.ke>");
        mailParams.setTo(List.of("wanjiku@gmail.com"));
        mailParams.setSubject("Your receipt");
        mailParams.setHtml("<p>Thank you for your order!</p>");

        EmailResponse email = eza.emails().send(mailParams);
        System.out.println("Dispatched email: " + email.getId());

        // 3. Verify signed webhook
        WebhookEvent event = Webhook.verify(
            rawBody,
            requestHeader("Eza-Signature"),
            System.getenv("EZA_WEBHOOK_SECRET")
        );
        System.out.println("Verified event: " + event.getType());
    }
}
```
