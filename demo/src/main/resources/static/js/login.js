function showSection(section) {
	// Sections
	const sections = ['login', 'register', 'forgot'];
	sections.forEach(s => {
		document.getElementById(`${s}-section`).classList.add('hidden');
		const tab = document.getElementById(`tab-${s}`);
		if (tab) {
			tab.classList.remove('active', 'text-slate-900');
			tab.classList.add('text-slate-400');
		}
	});

	// Show active
	document.getElementById(`${section}-section`).classList.remove('hidden');
	const activeTab = document.getElementById(`tab-${section}`);
	if (activeTab) {
		activeTab.classList.add('active', 'text-slate-900');
		activeTab.classList.remove('text-slate-400');
	}
}

function notify(msg, type = 'success') {
	const toast = document.getElementById('toast');
	const icon = document.getElementById('toast-icon');
	const text = document.getElementById('toast-msg');

	toast.className = `absolute top-8 right-8 left-8 md:left-auto md:w-80 z-50 p-4 rounded-2xl shadow-xl animate-fade-in border flex items-center gap-3 ${type === 'success' ? 'bg-emerald-50 text-emerald-700 border-emerald-200' : 'bg-rose-50 text-rose-700 border-rose-200'
		}`;

	icon.className = `fas ${type === 'success' ? 'fa-check-circle' : 'fa-exclamation-triangle'}`;
	text.innerText = msg;

	toast.classList.remove('hidden');
	setTimeout(() => toast.classList.add('hidden'), 4000);
}
function userLogin() {

	fetch("checkUserLogin", {
		method: "POST",
		headers: {
			"Content-Type": "application/json"
		},
		body: JSON.stringify({
			username: document.getElementById("username").value,
			password: document.getElementById("password").value
		})
	})
		.then(response => response.text())
		.then(data => {
			console.log(data);

			if (data === "Login successful") {
				window.location.href = "/userLogin";
			} else {
				document.getElementById("loginError").innerText = data;
			}
		})
		.catch(error => {
			console.error("Error:", error);
		});
}
