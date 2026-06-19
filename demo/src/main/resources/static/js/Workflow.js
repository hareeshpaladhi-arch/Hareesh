function sendOtp() {

	const userName = $("#username").val().trim();

	const requestData = {
		userName: userName,
		subject: "OTP Verification",
		mailType: "otp",
		data: {
			userName: userName
		}
	};

	$.ajax({
		url: "/mail/send",
		method: "POST",
		contentType: "application/json",
		data: JSON.stringify(requestData),
		success: function(res) {
			$("#verification-view").show();
			$("#login-section").hide();
			$("#otpSendMsgId").text("OTP "+res);
			startOtpTimer();
		},
		error: function(err) {
			$("#otpSendMsgId").text("Unable to send OTP");
		}
	});
}
function startOtpTimer() {
	const timerDisplay = document.getElementById('timer');
            let timeLeft = 30;
            timerDisplay.textContent = `${timeLeft}s`;
            const countdown = setInterval(() => {
                timeLeft--;
                timerDisplay.textContent = `${timeLeft}s`;
                if (timeLeft <= 0) {
                    clearInterval(countdown);
                    timerDisplay.innerHTML = `<button class="text-orange-600 hover:underline">Resend</button>`;
                }
            }, 1000);
        }
function verifyOtp() {

	const email = $("#email").val();
	const otp = $("#otp").val();

	if (!otp) {
		alert("Please enter OTP.");
		return;
	}

	$.ajax({
		url: "/mail/verifyOtp",
		type: "POST",
		data: {
			email: email,
			otp: otp
		},
		success: function(response) {

			alert(response.message);

			if (response.success) {
				localStorage.setItem("token", data.token);

				console.log("Login Success:", data);


				window.location.href = "/userLogin";

				console.log(response.code); // OTP_VERIFIED

			} else {
				$("#otpError").text(response.message);

				switch (response.code) {

					case "INVALID_OTP":
						$("#otp").focus();
						break;

					case "OTP_EXPIRED":
						$("#resendOtpBtn").show();
						break;

					case "OTP_ALREADY_USED":
						$("#resendOtpBtn").show();
						break;
				}
			}
		}
	});
}