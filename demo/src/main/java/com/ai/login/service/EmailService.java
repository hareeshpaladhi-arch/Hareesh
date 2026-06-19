package com.ai.login.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.ai.login.DAO.OtpRepository;
import com.ai.login.DAO.loginUserDAO;
import com.ai.login.DTO.OtpEntity;
import com.ai.login.DTO.OtpResponse;
import com.ai.login.DTO.User;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private OtpRepository otpRepository;
	
	@Autowired
	private loginUserDAO userDao;

	public void sendEmail(String userName, String subject, String mailType, Map<String, Object> data) {

		try {

			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
			Optional<User> user=null;
			if (userName != null && !"".equalsIgnoreCase(userName)) {
			     user = userDao.findByUsername(userName);
			}
            String userEmail=user.get().getEmail();
			helper.setTo(userEmail);
			helper.setSubject(subject);

			String htmlBody = "";

			switch (mailType.toLowerCase()) {

			case "otp":
				String otp = String.format("%06d", new SecureRandom().nextInt(1000000));

				OtpEntity otpEntity = new OtpEntity();
				otpEntity.setEmail(userEmail);
				otpEntity.setOtp(otp);
				otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(10));
				otpEntity.setStatus("ACTIVE");

				otpRepository.save(otpEntity);

				htmlBody = getOtpTemplate(String.valueOf(data.get("userName")), otp);
				break;

			case "workflow":
				htmlBody = getWorkflowTemplate(data);
				break;

			case "requestcreated":
				htmlBody = getRequestCreatedTemplate(data);
				break;

			case "approval":
				htmlBody = getApprovalTemplate(data);
				break;

			case "rejection":
				htmlBody = getRejectionTemplate(data);
				break;

			case "passwordreset":
				htmlBody = getPasswordResetTemplate(data);
				break;

			case "registration":
				htmlBody = getRegistrationTemplate(data);
				break;

			default:
				htmlBody = String.valueOf(data.get("body"));
			}

			helper.setText(htmlBody, true);

			mailSender.send(mimeMessage);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	private String buildEmailTemplate(
	        String title,
	        String message,
	        String detailsHtml,
	        String buttonText,
	        String buttonUrl) {

	    return "<html>"
	            + "<body style='font-family:Arial,sans-serif;background:#f4f6f9;padding:20px;'>"
	            + "<table width='700' align='center' style='background:#ffffff;'>"

	            + "<tr>"
	            + "<td style='background:#0f4c81;color:white;padding:20px;'>"
	            + "<h2>" + title + "</h2>"
	            + "</td>"
	            + "</tr>"

	            + "<tr>"
	            + "<td style='padding:25px;'>"

	            + message
	            + detailsHtml

	            + "<br><br>"

	            + (buttonUrl != null
	                    ? "<a href='" + buttonUrl + "' "
	                    + "style='background:#0f4c81;color:white;"
	                    + "padding:10px 20px;text-decoration:none;'>"
	                    + buttonText
	                    + "</a>"
	                    : "")

	            + "<br><br>"
	            + "<p>Regards,<br>"
	            + "<b>MDM/MDG Workflow System</b></p>"

	            + "</td>"
	            + "</tr>"

	            + "<tr>"
	            + "<td style='background:#f8f9fa;padding:10px;"
	            + "font-size:12px;text-align:center;'>"
	            + "This is an automated email. Please do not reply."
	            + "</td>"
	            + "</tr>"

	            + "</table>"
	            + "</body>"
	            + "</html>";
	}

	private String getWorkflowTemplate(Map<String,Object> data) {

    String details =
            "<table border='1' cellpadding='8' width='100%'>"
            + "<tr><td>Task Name</td><td>" + data.get("taskName") + "</td></tr>"
            + "<tr><td>Assigned To</td><td>" + data.get("assignedTo") + "</td></tr>"
            + "<tr><td>Reporting To</td><td>" + data.get("reportingTo") + "</td></tr>"
            + "<tr><td>Total Records</td><td>" + data.get("recordCount") + "</td></tr>"
            + "<tr><td>Priority</td><td>" + data.get("priority") + "</td></tr>"
            + "</table>";

    return buildEmailTemplate(
            "Workflow Task Assignment",
            "<p>The selected records have been assigned for processing.</p>",
            details,
            "Open Application",
            String.valueOf(data.get("applicationUrl"))
    );
}
	private String getRejectionTemplate(Map<String,Object> data) {

	    String details =
	            "<table border='1' cellpadding='8' width='100%'>"
	            + "<tr><td>Request No</td><td>" + data.get("requestNo") + "</td></tr>"
	            + "<tr><td>Rejected By</td><td>" + data.get("rejectedBy") + "</td></tr>"
	            + "<tr><td>Reason</td><td>" + data.get("reason") + "</td></tr>"
	            + "</table>";

	    return buildEmailTemplate(
	            "Request Rejected",
	            "<p>Your request has been rejected.</p>",
	            details,
	            "Modify Request",
	            String.valueOf(data.get("applicationUrl"))
	    );
	}	
	private String getRequestCreatedTemplate(Map<String,Object> data) {

	    String details =
	            "<table border='1' cellpadding='8' width='100%'>"
	            + "<tr><td>Request No</td><td>" + data.get("requestNo") + "</td></tr>"
	            + "<tr><td>Domain</td><td>" + data.get("domain") + "</td></tr>"
	            + "<tr><td>Request Type</td><td>" + data.get("requestType") + "</td></tr>"
	            + "<tr><td>Status</td><td>Submitted</td></tr>"
	            + "</table>";

	    return buildEmailTemplate(
	            "Request Created",
	            "<p>Your request has been created successfully.</p>",
	            details,
	            "View Request",
	            String.valueOf(data.get("applicationUrl"))
	    );
	}
	
	private String getRegistrationTemplate(Map<String,Object> data) {

	    String details =
	            "<table border='1' cellpadding='8' width='100%'>"
	            + "<tr><td>User Name</td><td>" + data.get("username") + "</td></tr>"
	            + "<tr><td>Role</td><td>" + data.get("role") + "</td></tr>"
	            + "</table>";

	    return buildEmailTemplate(
	            "Welcome to MDM Platform",
	            "<p>Your account has been created successfully.</p>",
	            details,
	            "Login",
	            String.valueOf(data.get("applicationUrl"))
	    );
	}
	private String getApprovalTemplate(Map<String,Object> data) {

	    String details =
	            "<table border='1' cellpadding='8' width='100%'>"
	            + "<tr><td>Request No</td><td>" + data.get("requestNo") + "</td></tr>"
	            + "<tr><td>Approved By</td><td>" + data.get("approvedBy") + "</td></tr>"
	            + "<tr><td>Comments</td><td>" + data.get("comments") + "</td></tr>"
	            + "</table>";

	    return buildEmailTemplate(
	            "Request Approved",
	            "<p>Your request has been approved.</p>",
	            details,
	            "View Request",
	            String.valueOf(data.get("applicationUrl"))
	    );
	}
	
	private String getPasswordResetTemplate(Map<String,Object> data) {

	    return buildEmailTemplate(
	            "Password Reset",
	            "<p>A password reset has been requested for your account.</p>"
	            + "<p>The reset link will expire in 30 minutes.</p>",
	            "",
	            "Reset Password",
	            String.valueOf(data.get("resetUrl"))
	    );
	}
	public String getOtpTemplate(String userName, String otp) {

		return "<!DOCTYPE html>" + "<html>" + "<head>" + "<meta charset='UTF-8'>" + "</head>"
				+ "<body style='font-family: Arial, sans-serif; background:#f4f4f4; padding:20px;'>"
				+ "<table width='100%' cellpadding='0' cellspacing='0'>" + "<tr>" + "<td align='center'>"
				+ "<table width='600' cellpadding='20' cellspacing='0' "
				+ "style='background:#ffffff; border-radius:8px;'>"

				+ "<tr>" + "<td align='center'>" + "<h2 style='color:#333;'>Email Verification</h2>" + "</td>" + "</tr>"

				+ "<tr>" + "<td>" + "<p>Dear " + userName + ",</p>"
				+ "<p>Your One-Time Password (OTP) for verification is:</p>"

				+ "<div style='text-align:center; margin:20px 0;'>" + "<span style='font-size:30px; font-weight:bold;"
				+ "letter-spacing:5px; color:#007bff;'>" + otp + "</span>" + "</div>"

				+ "<p>This OTP is valid for <b>10 minutes</b>.</p>"
				+ "<p>If you did not request this OTP, please ignore this email.</p>"

				+ "<br>" + "<p>Regards,<br>" + "<b>Your Company Name</b></p>" + "</td>" + "</tr>"

				+ "</table>" + "</td>" + "</tr>" + "</table>" + "</body>" + "</html>";
	}

	public OtpResponse verifyOtp(String email, String enteredOtp) {

	    Optional<OtpEntity> otpOpt =
	            otpRepository.findTopByEmailOrderByIdDesc(email);

	    if (!otpOpt.isPresent()) {
	        return new OtpResponse(
	                false,
	                "OTP_NOT_FOUND",
	                "No OTP found. Please generate a new OTP."
	        );
	    }

	    OtpEntity otpEntity = otpOpt.get();

	    if ("USED".equalsIgnoreCase(otpEntity.getStatus())) {
	        return new OtpResponse(
	                false,
	                "OTP_ALREADY_USED",
	                "OTP has already been used."
	        );
	    }

	    if ("EXPIRED".equalsIgnoreCase(otpEntity.getStatus())) {
	        return new OtpResponse(
	                false,
	                "OTP_EXPIRED",
	                "OTP has expired. Please request a new OTP."
	        );
	    }

	    if (LocalDateTime.now().isAfter(otpEntity.getExpiryTime())) {

	        otpEntity.setStatus("EXPIRED");
	        otpRepository.save(otpEntity);

	        return new OtpResponse(
	                false,
	                "OTP_EXPIRED",
	                "OTP has expired. Please request a new OTP."
	        );
	    }

	    if (!otpEntity.getOtp().equals(enteredOtp)) {

	        return new OtpResponse(
	                false,
	                "INVALID_OTP",
	                "Invalid OTP entered."
	        );
	    }

	    otpEntity.setStatus("USED");
	    otpRepository.save(otpEntity);

	    return new OtpResponse(
	            true,
	            "OTP_VERIFIED",
	            "OTP verified successfully."
	    );
	}
}

