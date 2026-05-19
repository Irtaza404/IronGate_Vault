package com.irongate.security;

import java.security.SecureRandom;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * Sends 6-digit OTPs via Gmail SMTP (STARTTLS on port 587).
 * Requires a valid Gmail App Password (NOT your regular Gmail password).
 *
 * How to get an App Password:
 *   1. Go to myaccount.google.com → Security
 *   2. Enable 2-Step Verification (must be ON first)
 *   3. Search "App passwords" → create one for Mail / Windows
 *   4. Paste the 16-char code below — no spaces
 */
public class OTPService {

    // ── Configure these two fields ──────────────────────────────
    private static final String SMTP_USER = "your email";
    private static final String SMTP_PASS = "app password"; // 16-char Gmail App Password
    // ────────────────────────────────────────────────────────────

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int    SMTP_PORT = 587;

    private static final SecureRandom RNG = new SecureRandom();

    /** Generate a random 6-digit OTP string. */
    public static String generateOTP() {
        int code = 100_000 + RNG.nextInt(900_000);
        return String.valueOf(code);
    }

    /**
     * Send a beautifully formatted HTML OTP email.
     * @param toEmail  Recipient email address
     * @param otp      The 6-digit code to deliver
     */
    public static void sendOTP(String toEmail, String otp) throws Exception {

        Properties props = new Properties();
        props.put("mail.smtp.auth",              "true");
        props.put("mail.smtp.starttls.enable",   "true");
        props.put("mail.smtp.starttls.required", "true");   // force TLS upgrade
        props.put("mail.smtp.host",              SMTP_HOST);
        props.put("mail.smtp.port",              String.valueOf(SMTP_PORT));
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout",           "10000");
        props.put("mail.smtp.ssl.protocols",     "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.ssl.trust",         SMTP_HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
            }
        });

        // Multipart/alternative: plain-text + HTML
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(SMTP_USER, "IronGate Vault"));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        msg.setSubject("IronGate Vault — Your One-Time Password");

        // Plain-text fallback
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(
            "IronGate Vault — Secure File Management\n\n" +
            "Your One-Time Password (OTP) is: " + otp + "\n\n" +
            "Valid for 5 minutes. Do NOT share this code with anyone.\n" +
            "If you did not attempt to log in, ignore this email.\n\n" +
            "— IronGate Vault Security System\n" +
            "Muhammad Irtaza | 2025-ARID-0151 | BSCS-3B | BIIT",
            "UTF-8"
        );

        // HTML part
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(buildHtmlEmail(otp), "text/html; charset=UTF-8");

        Multipart mp = new MimeMultipart("alternative");
        mp.addBodyPart(textPart);
        mp.addBodyPart(htmlPart);  // email clients prefer the last matching part
        msg.setContent(mp);

        Transport.send(msg);
    }

    // ── HTML email builder ───────────────────────────────────────

    private static String buildHtmlEmail(String otp) {

        // Build individual styled digit boxes
        StringBuilder digits = new StringBuilder();
        for (char c : otp.toCharArray()) {
            digits.append(
                "<td align='center' style='padding:0 5px;'>" +
                "<div style='" +
                "width:48px;height:60px;line-height:60px;" +
                "background:linear-gradient(135deg,#667eea,#764ba2);" +
                "color:#ffffff;" +
                "font-size:30px;font-weight:900;" +
                "font-family:Courier New,monospace;" +
                "border-radius:12px;" +
                "text-align:center;" +
                "box-shadow:0 6px 18px rgba(102,126,234,0.5);" +
                "'>" + c + "</div></td>"
            );
        }

        return "<!DOCTYPE html>" +
        "<html lang='en'><head><meta charset='UTF-8'>" +
        "<meta name='viewport' content='width=device-width,initial-scale=1'></head>" +
        "<body style='margin:0;padding:0;background:#0d0d1a;'>" +

        // ── Outer table ──────────────────────────────────────────
        "<table width='100%' cellpadding='0' cellspacing='0' bgcolor='#0d0d1a'>" +
        "<tr><td align='center' style='padding:48px 16px;'>" +

        // ── Card ─────────────────────────────────────────────────
        "<table width='520' cellpadding='0' cellspacing='0' style='" +
        "background:#16162a;border-radius:24px;" +
        "box-shadow:0 24px 80px rgba(0,0,0,0.7);overflow:hidden;" +
        "border:1px solid #2a2a50;'>" +

        // ── Gradient header ──────────────────────────────────────
        "<tr><td style='" +
        "background:linear-gradient(135deg,#667eea 0%,#a855f7 50%,#f093fb 100%);" +
        "padding:40px 40px 32px 40px;text-align:center;'>" +

        "<div style='font-size:56px;margin-bottom:12px;'>&#128274;</div>" +  // 🔒

        "<h1 style='margin:0 0 4px 0;color:#ffffff;font-size:26px;" +
        "font-weight:900;letter-spacing:3px;font-family:Arial,sans-serif;" +
        "text-shadow:0 2px 10px rgba(0,0,0,0.25);'>IRONGATE VAULT</h1>" +

        "<p style='margin:0;color:rgba(255,255,255,0.8);font-size:11px;" +
        "letter-spacing:4px;text-transform:uppercase;font-family:Arial,sans-serif;'>" +
        "Secure File Management System</p>" +

        // Decorative dots
        "<div style='margin-top:20px;'>" +
        "<span style='display:inline-block;width:6px;height:6px;border-radius:50%;" +
        "background:rgba(255,255,255,0.4);margin:0 3px;'></span>" +
        "<span style='display:inline-block;width:6px;height:6px;border-radius:50%;" +
        "background:rgba(255,255,255,0.8);margin:0 3px;'></span>" +
        "<span style='display:inline-block;width:6px;height:6px;border-radius:50%;" +
        "background:rgba(255,255,255,0.4);margin:0 3px;'></span>" +
        "</div>" +

        "</td></tr>" +

        // ── Body ─────────────────────────────────────────────────
        "<tr><td style='padding:40px 40px 28px 40px;'>" +

        "<p style='margin:0 0 6px 0;color:#8080b0;font-size:11px;" +
        "text-transform:uppercase;letter-spacing:3px;" +
        "text-align:center;font-family:Arial,sans-serif;'>Authentication Code</p>" +

        "<p style='margin:0 0 28px 0;color:#c0c0e0;font-size:14px;" +
        "line-height:1.7;text-align:center;font-family:Arial,sans-serif;'>" +
        "Enter this code in IronGate Vault to complete your login.<br>" +
        "<span style='color:#f093fb;font-size:12px;'>" +
        "&#8987; Expires in <strong>5 minutes</strong></span></p>" +

        // OTP digit row
        "<table cellpadding='0' cellspacing='0' align='center' style='margin:0 auto 32px auto;'>" +
        "<tr>" + digits + "</tr></table>" +

        // Timer bar
        "<div style='background:#1e1e3a;border-radius:8px;padding:2px;" +
        "margin-bottom:28px;overflow:hidden;'>" +
        "<div style='height:4px;background:linear-gradient(to right,#667eea,#f093fb);" +
        "border-radius:8px;width:100%;'></div></div>" +

        // Security notice box
        "<table width='100%' cellpadding='0' cellspacing='0' style='" +
        "background:#0d0d20;border-radius:14px;" +
        "border:1px solid #2a2a50;margin-bottom:4px;'>" +
        "<tr><td style='padding:20px 24px;'>" +

        "<p style='margin:0 0 12px 0;color:#a855f7;font-size:12px;" +
        "font-weight:bold;letter-spacing:2px;text-transform:uppercase;" +
        "font-family:Arial,sans-serif;'>&#128737; Security Notice</p>" +

        "<p style='margin:0;color:#6060a0;font-size:12px;" +
        "line-height:2;font-family:Arial,sans-serif;'>" +
        "&#10004;&nbsp; Never share this code with anyone, including support staff.<br>" +
        "&#10004;&nbsp; IronGate Vault will never ask for your password via email.<br>" +
        "&#10004;&nbsp; This OTP is single-use and expires in 5 minutes.<br>" +
        "&#10004;&nbsp; If you didn&#39;t request this, your account is safe — ignore it." +
        "</p></td></tr></table>" +

        "</td></tr>" +

        // ── Footer ───────────────────────────────────────────────
        "<tr><td style='background:#0d0d20;padding:24px 40px;" +
        "border-top:1px solid #2a2a50;text-align:center;'>" +

        "<p style='margin:0 0 6px 0;color:#404070;font-size:11px;" +
        "font-family:Arial,sans-serif;'>" +
        "Sent by <span style='color:#667eea;font-weight:bold;'>IronGate Vault</span>" +
        " &nbsp;&#183;&nbsp; AES-256-GCM &nbsp;&#183;&nbsp; PBKDF2 Password Hashing</p>" +

        "<p style='margin:0 0 16px 0;color:#30305a;font-size:10px;" +
        "font-family:Arial,sans-serif;'>" +
        "Muhammad Irtaza &nbsp;&#183;&nbsp; 2025-ARID-0151 " +
        "&nbsp;&#183;&nbsp; BSCS-3B &nbsp;&#183;&nbsp; BIIT " +
        "&nbsp;&#183;&nbsp; Software Engineering</p>" +

        // Rainbow bottom bar
        "<div style='height:5px;border-radius:3px;" +
        "background:linear-gradient(to right,#667eea,#a855f7,#f093fb,#667eea);'>" +
        "</div>" +

        "</td></tr>" +
        "</table>" +   // end card
        "</td></tr></table>" +   // end outer
        "</body></html>";
    }
}
