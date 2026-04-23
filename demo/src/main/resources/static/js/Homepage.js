
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

// Initialize drop zone visual cues


