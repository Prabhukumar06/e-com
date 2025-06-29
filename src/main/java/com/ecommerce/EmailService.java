package com.ecommerce;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.text.NumberFormat;
import java.util.Locale;

public class EmailService {
    private static final String ADMIN_EMAIL = System.getenv().getOrDefault("ADMIN_EMAIL", "prabhukp1777@gmail.com");
    private static final String COMPANY_NAME = System.getenv().getOrDefault("COMPANY_NAME", "E-Commerce Store");
    private static final String SMTP_HOST = System.getenv().getOrDefault("SMTP_HOST", "smtp.gmail.com");
    private static final String SMTP_PORT = System.getenv().getOrDefault("SMTP_PORT", "587");
    private static final String SMTP_USER = System.getenv().getOrDefault("SMTP_USER", "prabhukp1777@gmail.com");
    private static final String SMTP_PASSWORD = System.getenv().getOrDefault("SMTP_PASSWORD", "tliy pnas dtya zzsu");

    public static void sendOrderConfirmation(String customerName, String customerEmail, String customerAddress, List<Map<String, Object>> cart) {
        String subject = "New Order Received - Order #" + generateOrderId();
        String content = buildAdminEmailContent(customerName, customerEmail, customerAddress, cart);
        sendEmail(ADMIN_EMAIL, subject, content);
    }

    public static void sendCustomerConfirmation(String customerName, String customerEmail, List<Map<String, Object>> cart) {
        String subject = "Order Confirmation - " + COMPANY_NAME;
        String content = buildCustomerEmailContent(customerName, cart);
        sendEmail(customerEmail, subject, content);
    }

    private static String buildAdminEmailContent(String customerName, String customerEmail, String customerAddress, List<Map<String, Object>> cart) {
        StringBuilder email = new StringBuilder();
        email.append("<div style='font-family: Arial, sans-serif;'>");
        email.append("<h2>New Order Details</h2>");
        email.append("<div style='background-color: #f5f5f5; padding: 15px; margin: 10px 0;'>");
        email.append("<h3>Customer Information</h3>");
        email.append("<p><strong>Name:</strong> ").append(customerName).append("</p>");
        email.append("<p><strong>Email:</strong> ").append(customerEmail).append("</p>");
        email.append("<p><strong>Shipping Address:</strong> ").append(customerAddress).append("</p>");
        email.append("</div>");
        
        appendOrderDetails(email, cart);
        email.append("</div>");
        
        return email.toString();
    }

    private static String buildCustomerEmailContent(String customerName, List<Map<String, Object>> cart) {
        StringBuilder email = new StringBuilder();
        email.append("<div style='font-family: Arial, sans-serif;'>");
        email.append("<h2>Thank you for your order, ").append(customerName).append("!</h2>");
        email.append("<p>We're pleased to confirm that we've received your order.</p>");
        
        appendOrderDetails(email, cart);
        
        email.append("<div style='margin-top: 20px;'>");
        email.append("<p>If you have any questions about your order, please contact us at ").append(ADMIN_EMAIL).append("</p>");
        email.append("<p>Thank you for shopping with ").append(COMPANY_NAME).append("!</p>");
        email.append("</div></div>");
        
        return email.toString();
    }

    

    private static void appendOrderDetails(StringBuilder email, List<Map<String, Object>> cart) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        double total = 0.0;

        email.append("<div style='margin: 20px 0;'>");
        email.append("<h3>Order Details</h3>");
        email.append("<table style='width: 100%; border-collapse: collapse;'>");
        email.append("<tr style='background-color: #f5f5f5;'>");
        email.append("<th style='padding: 10px; text-align: left;'>Product</th>");
        email.append("<th style='padding: 10px; text-align: right;'>Quantity</th>");
        email.append("<th style='padding: 10px; text-align: right;'>Price</th>");
        email.append("<th style='padding: 10px; text-align: right;'>Subtotal</th></tr>");

        for (Map<String, Object> item : cart) {
            Product product = (Product) item.get("product");
            int quantity = ((Number) item.get("quantity")).intValue();
            double subtotal = product.getPrice() * quantity;
            total += subtotal;

            email.append("<tr style='border-bottom: 1px solid #ddd;'>");
            email.append("<td style='padding: 10px;'>").append(product.getName()).append("</td>");
            email.append("<td style='padding: 10px; text-align: right;'>").append(quantity).append("</td>");
            email.append("<td style='padding: 10px; text-align: right;'>").append(currencyFormatter.format(product.getPrice())).append("</td>");
            email.append("<td style='padding: 10px; text-align: right;'>").append(currencyFormatter.format(subtotal)).append("</td></tr>");
        }

        email.append("<tr style='font-weight: bold;'>");
        email.append("<td colspan='3' style='padding: 10px; text-align: right;'>Total:</td>");
        email.append("<td style='padding: 10px; text-align: right;'>").append(currencyFormatter.format(total)).append("</td></tr>");
        email.append("</table></div>");
    }

    private static void sendEmail(String to, String subject, String content) {
        if (!areSmtpSettingsValid(SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASSWORD)) {
            logEmailError("SMTP settings are not properly configured");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        try {
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SMTP_USER, SMTP_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(content, "text/html");

            Transport.send(message);
            System.out.println("Email sent successfully to: " + to);

        } catch (MessagingException e) {
            logEmailError("Failed to send email to " + to + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean areSmtpSettingsValid(String... settings) {
        for (String setting : settings) {
            if (setting == null || setting.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static void logEmailError(String error) {
        System.err.println("Email Service Error: " + error);
        // In a production environment, you might want to log this to a file or monitoring service
    }

    private static String generateOrderId() {
        return String.format("%d-%d", System.currentTimeMillis(), (int) (Math.random() * 1000));
    }
}
