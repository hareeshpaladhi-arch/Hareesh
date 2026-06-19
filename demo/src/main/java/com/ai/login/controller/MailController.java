package com.ai.login.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ai.login.DTO.MailRequest;
import com.ai.login.DTO.OtpResponse;
import com.ai.login.service.EmailService;

;

@RestController
@RequestMapping("/mail")
public class MailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendMail(
            @RequestBody MailRequest request) {

        emailService.sendEmail(
                request.getUserName(),
                request.getSubject(),
                request.getMailType(),
                request.getData()
        );

        return ResponseEntity.ok("Mail Sent Successfully");
    }
    
    @PostMapping("/verifyOtp")
    public ResponseEntity<OtpResponse> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp) {

        OtpResponse result = emailService.verifyOtp(email, otp);

        return ResponseEntity.ok(result);
    }
}
