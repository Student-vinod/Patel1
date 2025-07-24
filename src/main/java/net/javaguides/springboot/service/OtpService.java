package net.javaguides.springboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService {

    private static class OtpData {
        private String otp;
        private LocalDateTime createdAt;

        public OtpData(String otp) {
            this.otp = otp;
            this.createdAt = LocalDateTime.now();
        }

        public String getOtp() {
            return otp;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
    }

    private final Map<String, OtpData> otpStorage = new HashMap<>();

    @Autowired
    private JavaMailSender mailSender;

    public String generateOtp(String email) {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000); // 6-digit OTP
        otpStorage.put(email, new OtpData(otp));
        sendOtpEmail(email, otp);
        return otp;
    }

    private void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("OTP for Updating Your Email ID / Mobile Number");

        String otpMessage =
                "Dear Vinod Goud,\n\n" +
                        "We have received your request to update your e-mail ID / mobile number linked to your account.\n" +
                        "To verify and complete this update, please enter the following One-Time Password (OTP): " + otp + "\n\n" +
                        "Note: This OTP is valid for 5 minutes only.\n" +
                        "Please do not share this OTP with anyone.\n" +
                        "If you did not request this change, kindly disregard this message or report it to our support team immediately.\n\n" +
                        "Thanking you.\n" +
                        "Revenue Department\n" +
                        "Vadodara Municipal Corporation\n" +
                        "Khanderao Market\n" +
                        "Vadodara, Gujarat, India\n" +
                        "Website: https://www.vmc.gov.in";

        message.setText(otpMessage);
        mailSender.send(message);
    }

    public String validateOtp(String email, String otp) {
        OtpData data = otpStorage.get(email);

        if (data == null) {
            return "OTP not found or already expired.";
        }

        Duration duration = Duration.between(data.getCreatedAt(), LocalDateTime.now());
        if (duration.toMinutes() > 1) {
            otpStorage.remove(email);
            return "OTP expired.";
        }

        if (!data.getOtp().equals(otp)) {
            return "Invalid OTP.";
        }

        otpStorage.remove(email); // Remove OTP after successful validation
        return "OTP verified successfully.";
    }
}
