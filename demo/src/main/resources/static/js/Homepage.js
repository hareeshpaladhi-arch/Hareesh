async function loadSidebarData() {

    try {

        const response = await fetch("sidebar/list", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            }
        });

        const data = await response.json();

        console.log("Sidebar Data :: ", data);

        let sidebarHtml = "";

        data.forEach(item => {

            sidebarHtml += `
                <div class="sidebar-menu">
                    <i class="${item.menuIcon}"></i>
                    <a href="${item.menuUrl}">
                        ${item.menuName}
                    </a>
                </div>
            `;
        });

        document.getElementById("sidebarContainer").innerHTML = sidebarHtml;

    } catch (error) {

        console.error("Error loading sidebar data :: ", error);
    }
}



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



function downloadErrors() {
	window.location = "/auth/errors";
}
function loadTable(batchId) {

	const mapping = document.getElementById('mappingSection');
	const uploadSection = document.getElementById('uploadSection');
	const tableDataSection = document.getElementById('tableDataSection');
	$("#view-ai-automation div").hide();
    $("#web-breadcrumbs").show();
    $("#web-breadcrumbs div").show();
    $("#automationMainDivId").show();
    $("#tableDataSection").show();

	$("#web-breadcrumbs").find("span")
		.attr('class', 'text-slate-400 hover:text-blue-600 cursor-pointer');

	$("#web-breadcrumbs")
		.find("span")
		.filter(function () {
			return $(this).text().trim().toLowerCase() === "view data";
		})
		.attr("class", "text-slate-900 font-semibold");
	mapping.classList.add('hidden');
	uploadSection.classList.add('hidden');
	tableDataSection.classList.remove('hidden');

	$("#uploadButtonsId").hide();

	// destroy existing table
	if ($.fn.DataTable.isDataTable('#dynamicTable')) {
		$('#dynamicTable').DataTable().destroy();
	}

	$('#tableDataSection').html(`
		<table id="dynamicTable" class="display nowrap" style="width:100%">
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

	$('#dynamicTable').DataTable({
		processing: true,
		serverSide: true,
		paging: true,
		searching: true,
		ordering: true,
		pageLength: 10,

		ajax: function(data, callback, settings) {

			let page = (data.start / data.length);
			let size = data.length;
			let search = data.search.value;

			fetch(`/auth/batch/${batchId}?page=${page}&size=${size}&search=${search}`)
				.then(res => res.json())
				.then(result => {
					callback({
						draw: data.draw,
						recordsTotal: result.totalRecords,
						recordsFiltered: result.totalRecords,
						data: result.data
					});
				})
				.catch(err => {
					hideLoader();
					console.error(err);
				});
		},

		columns: [
			{ data: 'id' },
			{ data: 'className' },
			{ data: 'shortDesc' },
			{ data: 'longDesc' },
			{ data: 'materialType', defaultContent: '-' }
		]
	});
}
function toggleConfigModal(show) {
	if (show) {
		getBatchIdList();
	}
	document.getElementById('configModal').classList.toggle('hidden', !show);
}
function applyConfigModal(show) {

	var batchId = "";
	var processSelectionObj = {};

	$(".modal-content-class .grid").each(function() {
		if (!batchId) {
			batchId = $(this).find("select").val();
		}

		$(this).find("label").each(function() {

			var key = $(this).find("p").text().trim();

			var checked = $(this).find("input[type='checkbox']").is(":checked");

			processSelectionObj[key] = checked;
		});

	});

	if (batchId && !jQuery.isEmptyObject(processSelectionObj)) {
		loadDuplicates();
		classifyData(batchId);
		processSelectionFlow(batchId, processSelectionObj);
	}

	document.getElementById('configModal')
		.classList.toggle('hidden', !show);
}
function processSelectionFlow(batchId, processSelectionObj) {

	if (batchId) {
		var breadCrumbStr = `<div class="flex items-center gap-2">
		<svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" fill="currentColor" viewBox="0 0 256 256" class="text-slate-300"><path d="M181.66,133.66l-80,80a8,8,0,0,1-11.32-11.32L164.69,128,90.34,53.66a8,8,0,0,1,11.32-11.32l80,80A8,8,0,0,1,181.66,133.66Z"></path></svg>
                        <span class="text-slate-900 font-semibold" onclick="loadTable('${batchId}')">View Data</span>   
                    </div>`;
		$("#web-breadcrumbs").append(breadCrumbStr);
		loadTable(batchId);
	}

	for (var key in processSelectionObj) {

		if (processSelectionObj[key]) {

			console.log("Selected Process:", key);


		}
	}
}
function getUploadSectionShow() {
	const mapping = document.getElementById('mappingSection');
	const uploadSection = document.getElementById('uploadSection');
	$("#view-ai-automation div").hide();
    $("#web-breadcrumbs").show();
    $("#web-breadcrumbs div").show();
    $("#automationMainDivId").show();
    $("#automationMainDivId div").show();
	const tableDataSection = document.getElementById('tableDataSection');
	$("#web-breadcrumbs").find("span").attr('class', 'text-slate-400 hover:text-blue-600 cursor-pointer');
	$("#web-breadcrumbs")
    .find("span")
    .filter(function () {
        return $(this).text().trim().toLowerCase() === "upload";
    })
    .attr("class", "text-slate-900 font-semibold");
	mapping.classList.add('hidden');
	uploadSection.classList.remove('hidden');
	tableDataSection.classList.add('hidden');
	$("#uploadButtonsId").show();

}
function getBatchIdList() {
	$.ajax({

		url: "/api/batchList",

		type: "GET",

		success: function(response) {

			$("#config-classify").empty();

			let optionStr = "<option value=''>Select Batch Id</option>";

			if (response && response.length > 0) {

				response.forEach(function(batchId) {

					optionStr += `
                        <option value="${batchId}">
                            ${batchId}
                        </option>
                    `;

				});

			}

			$("#config-classify").html(optionStr);

		},

		error: function(xhr) {

			console.log(xhr);

			alert("Error while fetching Batch Id List");

		}

	});

}
function loadDuplicates() {

    let batchId = $("#config-classify").val();
    let accuracy = $("#accuracy").val();

    // Hide existing sections
    $("#view-ai-automation div").hide();
    $("#web-breadcrumbs").show();
    $("#web-breadcrumbs div").show();

    // Remove existing duplicate table container if already exists
    $("#duplicatesTableId").remove();

    // Append new table container
    $("#view-ai-automation").append(`
        <div id="duplicatesTableId" class="p-2">
            <table id="tableDupDataSection" class="display nowrap" style="width:100%">
                <thead>
                    <tr>
                        <th>Row1 ID</th>
                        <th>Row2 ID</th>
                        <th>Row1 Description</th>
                        <th>Row2 Description</th>
                        <th>Match %</th>
                    </tr>
                </thead>
                <tbody></tbody>
            </table>
        </div>
    `);

    // Breadcrumb
    var breadCrumbStr = `
        <div class="flex items-center gap-2 duplicate-breadcrumb">
            <svg xmlns="http://www.w3.org/2000/svg" 
                 width="12" 
                 height="12" 
                 fill="currentColor" 
                 viewBox="0 0 256 256" 
                 class="text-slate-300">
                <path d="M181.66,133.66l-80,80a8,8,0,0,1-11.32-11.32L164.69,128,90.34,53.66a8,8,0,0,1,11.32-11.32l80,80A8,8,0,0,1,181.66,133.66Z"></path>
            </svg>

            <span class="text-slate-900 font-semibold cursor-pointer" onclick="loadDuplicates()">
                Duplicates View Data
            </span>
        </div>
    `;

    // Avoid duplicate breadcrumb
    if ($("#web-breadcrumbs .duplicate-breadcrumb").length === 0) {
        $("#web-breadcrumbs").append(breadCrumbStr);
    }

    // Initialize DataTable
    let table = $('#tableDupDataSection').DataTable({
        pageLength: 10,
        searching: true,
        ordering: true,
        responsive: true,
        destroy: true
    });

    $.ajax({

        url: "/api/duplicates/" + batchId + "?accuracy=" + accuracy,

        type: "GET",

        success: function(response) {

            table.clear();

            let duplicates = response.duplicates || [];

            if (duplicates.length > 0) {

                $.each(duplicates, function(index, row) {

                    table.row.add([
                        row.row1Id || "",
                        row.row2Id || "",
                        row.row1Desc || "",
                        row.row2Desc || "",
                        (row.matchPercentage || 0) + "%"
                    ]);

                });

                table.draw(false);

            } else {

                table.draw(false);
                alert("No Duplicate Records Found");

            }

        },

        error: function(xhr) {

            console.log(xhr);
            alert("Error while fetching duplicate data");

        }

    });

}
async function logout() {
	try {
		await fetch('/auth/logout', {
			method: 'POST',
			credentials: 'include'  // important — sends and receives cookies
		});
	} catch (err) {
		console.error('Logout failed', err);
	} finally {
		window.location.href = '/login';
	}
}

function classifyData(batchId) {

    $.ajax({
        url: '/api/classify?batchId=' + batchId,
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({}),
        success: function(response) {

            console.log("Classification Response:", response);

            if (response && response.length > 0) {

                response.forEach(function(item) {

                    console.log("Category :", item.category);
                    console.log("Noun :", item.noun);
                    console.log("Modifier :", item.modifier);
                    console.log("Confidence :", item.confidence);
                    loadTable(batchId);
                });
            }
        },
        error: function(xhr) {

            console.log("Error :", xhr.responseText);
        }
    });
}



