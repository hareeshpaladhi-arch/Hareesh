
function switchTab(tabId) {
	// Hide all sections
	document.getElementById('view-dashboard').classList.add('hidden');
	document.getElementById('view-ai-automation').classList.add('hidden');

	// Reset menu styles
	document.getElementById('btn-dashboard').classList.remove('active-menu');
	document.getElementById('btn-dashboard').classList.add('text-gray-600');
	document.getElementById('btn-ai-automation').classList.remove('active-menu');
	document.getElementById('btn-ai-automation').classList.add('text-gray-600');

	// Show active section
	document.getElementById('view-' + tabId).classList.remove('hidden');
	document.getElementById('btn-' + tabId).classList.add('active-menu');
	document.getElementById('btn-' + tabId).classList.remove('text-gray-600');
}

/**
 * File Upload Logic
 * Simulates a file processing workflow
 */
function handleFileUpload(event) {
	const file = event.target.files[0];
	if (!file) return;

	// Show feedback
	showToast(`Processing ${file.name}...`);

	// Simulate upload delay
	setTimeout(() => {
		const statusArea = document.getElementById('upload-status');
		const tableBody = document.getElementById('file-table-body');

		statusArea.classList.remove('hidden');

		// Add record to table
		const row = `
                    <tr class="border-b border-gray-50 hover:bg-gray-50 transition-colors">
                        <td class="px-6 py-4 text-sm text-gray-600 font-mono">#${Math.floor(Math.random() * 10000)}</td>
                        <td class="px-6 py-4 text-sm font-medium text-gray-800">${file.name}</td>
                        <td class="px-6 py-4 text-sm text-gray-500">${(file.size / 1024).toFixed(2)} KB</td>
                        <td class="px-6 py-4">
                            <span class="px-2 py-1 bg-blue-100 text-blue-700 rounded-lg text-xs font-semibold">In AI Queue</span>
                        </td>
                    </tr>
                `;

		tableBody.insertAdjacentHTML('afterbegin', row);
		showToast('File uploaded and AI analysis started!');
	}, 1000);
}

/**
 * Toast Notification System
 */
function showToast(message) {
	const toast = document.getElementById('toast');
	const msgEl = document.getElementById('toast-message');
	msgEl.textContent = message;

	toast.classList.remove('translate-y-24');
	setTimeout(() => {
		toast.classList.add('translate-y-24');
	}, 3000);
}
let interval;

function uploadFile() {
	let fileInput = document.getElementById("fileInput");
	let file = fileInput.files[0];

	if (!file) {
		alert("Please select a file");
		return;
	}

	let fd = new FormData();
	fd.append("file", file);

	const overlay = document.getElementById('processingOverlay');
	const bar = document.getElementById('progressBar');
	const txt = document.getElementById('progressText');
	const mapping = document.getElementById('mappingSection');
	const uploadSelection = document.getElementById('uploadSection');
	const tableDataSection = document.getElementById('tableDataSection');

	// Show overlay
	overlay.classList.remove('hidden');
	bar.style.width = "0%";
	txt.innerText = "Uploading...";

	fetch("/auth/upload", {
		method: "POST",
		body: fd
	})
		.then(response => {
			if (!response.ok) {
				throw new Error("Upload failed");
			}
			return response.json(); // ✅ IMPORTANT (backend returns JSON)
		})
		.then(result => {
			// Update progress bar to complete
			bar.style.width = "100%";

			// Show batch result
			txt.innerHTML = `
            <b>Status:</b> ${result.status} <br>
            <b>Read:</b> ${result.totalRead} <br>
            <b>Saved:</b> ${result.totalSaved} <br>
            <b>Skipped:</b> ${result.totalSkipped} <br>
            <b>Message:</b> ${result.message}
        `;

			// Hide after delay
			setTimeout(() => {
				overlay.classList.add('hidden');
				mapping.classList.add('hidden');
				tableDataSection.classList.remove('hidden')
				resetMapping();
				loadTable(result.batchId);
				switchTab('ai-automation');
			}, 3000);
		})
		.catch(err => {
			console.error(err);
			txt.innerText = "Error: " + err.message;
			overlay.classList.add('hidden');
		});
}

function track(id) {
	interval = setInterval(() => {
		fetch("/api/progress/" + id)
			.then(r => r.json())
			.then(d => {

				document.getElementById("bar").style.width = d.percent + "%";
				document.getElementById("bar").innerText = d.percent + "%";

				document.getElementById("status").innerText =
					"Status: " + d.status + " | " + d.read + "/" + d.total;

				if (d.status === "COMPLETED" || d.status === "FAILED") {
					clearInterval(interval);
				}
			});
	}, 2000);
}

function downloadErrors() {
	window.location = "/auth/errors";
}
function loadTable(batchId) {

    fetch("/auth/batch/" + batchId)
        .then(res => res.json())
        .then(result => {

            // remove old table if exists
            $('#tableDataSection').empty();

            // create table dynamically
            $('#tableDataSection').append(`
                <table id="dynamicTable" class="display" style="width:100%">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Class Name</th>
                            <th>Short Desc</th>
                            <th>Long Desc</th>
                            <th>Material Type</th>
                        </tr>
                    </thead>
                </table>
            `);

            // initialize DataTable on the new table
            $('#dynamicTable').DataTable({
                data: result,
                columns: [
                    { data: 'id' },
                    { data: 'className' },
                    { data: 'shortDesc' },
                    { data: 'longDesc' },
                    { data: 'materialType', defaultContent: '-' }
                ]
            });

        })
        .catch(err => console.error(err));
}


// Initialize drop zone visual cues


