package com.ai.login.controller;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ai.login.DTO.ApiResponse;
import com.ai.login.DTO.BatchJobResult;
import com.ai.login.DTO.LoginRequest;
import com.ai.login.DTO.User;
import com.ai.login.config.BatchCounterListener;
import com.ai.login.service.FileUploadService;
import com.ai.login.service.ProgressService;
import com.ai.login.service.loginUserService;
import com.ai.login.util.JwtUtil;

import io.jsonwebtoken.lang.Arrays;

import com.ai.login.DTO.BatchTemplate;

@RestController
@RequestMapping("/auth")
public class LoginUserController {

	@Autowired
	private loginUserService userService;

	@Autowired
	private AuthenticationManager authManager;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job job;

	@Autowired
	private ProgressService progressService;

	@Autowired
	private FileUploadService fileUploadService;

	@Autowired
	private Job importJob;

	@Autowired
	private BatchCounterListener counterListener;

	// ✅ LOGIN
	@PostMapping("/checkUserLogin")
	public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
		

		try {
			authManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

			String token = jwtUtil.generateToken(request.getUsername(),request.getEmail());

			String cookie = "token=" + token + "; Path=/" + "; Max-Age=3600" + "; HttpOnly" + "; SameSite=Lax";

			response.setHeader("Set-Cookie", cookie);

			return ResponseEntity.ok(Map.of("message", "Login successful"));

		} catch (BadCredentialsException e) {
			return ResponseEntity.status(401).body(new ApiResponse("Invalid username or password", false));
		}
	}

	// ✅ REGISTER
	@PostMapping("/register")
	public ResponseEntity<ApiResponse> register(@RequestBody User user) {

		ApiResponse response = userService.register(user);

		return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
	}

	// ✅ LOGOUT
	@PostMapping("/logout")
	public ResponseEntity<?> logout(HttpServletResponse response) {

		String cookie = "token=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax";
		response.setHeader("Set-Cookie", cookie);

		return ResponseEntity.ok().build();
	}

	// ✅ FILE UPLOAD (FIXED)
	@PostMapping("/upload")
	public ResponseEntity<BatchJobResult> upload(@RequestParam("file") MultipartFile file) {
		try {
			String safeCsvPath = fileUploadService.prepareFileForBatch(file);

			String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
			long time = System.currentTimeMillis();
			String batchId = "BATCH_"+ time + "_" + random;

			JobParameters params = new JobParametersBuilder().addString("filePath", safeCsvPath)
					.addString("batchId", batchId).addLong("timestamp", time).toJobParameters();

			JobExecution execution = jobLauncher.run(importJob, params);

			BatchJobResult result = new BatchJobResult(batchId, counterListener.getTotalRead(),
					counterListener.getTotalSaved(), counterListener.getTotalSkipped(),
					execution.getStatus().toString(), "Batch completed successfully");

			return ResponseEntity.ok(result);

		} catch (Exception e) {
			BatchJobResult error = new BatchJobResult("N/A", 0, 0, 0, "FAILED", "Upload failed: " + e.getMessage());
			return ResponseEntity.status(500).body(error);
		}
	}

	// ✅ ERROR DOWNLOAD
	@GetMapping("/errors")
	public ResponseEntity<InputStreamResource> downloadError() throws Exception {

		File file = new File("uploads/error.csv");

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=error.csv")
				.body(new InputStreamResource(new FileInputStream(file)));
	}

	@GetMapping("/batch/{batchId}")
	public List<BatchTemplate> getData(@PathVariable String batchId) {
		return userService.getByBatchId(batchId);
	}

}