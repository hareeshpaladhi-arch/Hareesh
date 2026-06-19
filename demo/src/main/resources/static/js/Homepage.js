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

		const parentMenus = data.filter(item =>
			item.parentId === null ||
			item.parentId === undefined
		);

		const childMenus = data.filter(item =>
			item.parentId !== null &&
			item.parentId !== undefined
		);

		let sidebarHtml = "";

		parentMenus.forEach((parent, index) => {

			const children = childMenus.filter(
				child => child.parentId === parent.id
			);
			var activeClass = "";
			if (index == 0) {
				activeClass = "active-menu ";
			}


			sidebarHtml += `
                <div class="mb-2">

                    <button
                        onclick="toggleMenu(${parent.id})"
                        class="
                             ${activeClass}
                             menu-item
                           w-full
                            flex
                            items-center
                            gap-3
                            px-4
                            py-2
                            rounded-lg
                            text-sm
                            text-slate-400
                            hover:text-white
                            transition-all
                        ">

                        <div class="flex items-center gap-8">

                            ${getMenuIcon(parent.menuName)}

                            <span class="menuTitleClass font-semibold text-sm">
                                ${parent.menuName}
                            </span>

                        </div>

                        ${children.length > 0 ? `
                            <svg id="arrow-${parent.id}"
                                 xmlns="http://www.w3.org/2000/svg"
                                 class="w-4 h-4 transition-transform duration-300"
                                 fill="none"
                                 viewBox="0 0 24 24"
                                 stroke="currentColor">
                                <path stroke-linecap="round"
                                      stroke-linejoin="round"
                                      stroke-width="2"
                                      d="M19 9l-7 7-7-7" />
                            </svg>
                        ` : ''}

                    </button>

                    <div
                        id="submenu-${parent.id}"
                        class="${index === 0 ? '' : 'hidden'} ml-10 mt-1 space-y-1">
            `;

			children.forEach(child => {

				sidebarHtml += `
                    <button
                        onclick="switchTab('${child.menuName}','${child.menuUrl}')"
                        class="
                            menu-item
                            w-full
                            flex
                            items-center
                            gap-3
                            px-4
                            py-2
                            rounded-lg
                            text-sm
                            text-slate-400
                            transition-all
                        ">

                        ${getMenuIcon(child.menuName)}

                        <span class="menuTitleClass">${child.menuName}</span>

                    </button>
                `;
			});

			sidebarHtml += `
                    </div>
                </div>
            `;
		});

		document.getElementById("sideBarNavId").innerHTML = sidebarHtml;

		if (window.lucide) {
			lucide.createIcons();
		}
		document.querySelectorAll('.menu-item').forEach(item => {
			item.addEventListener('click', function() {
				document.querySelectorAll('.menu-item')
					.forEach(m => m.classList.remove('active-menu'));

				this.classList.add('active-menu');
			});
		});

	} catch (error) {

		console.error("Error loading sidebar:", error);
	}
}

function toggleMenu(parentId) {
	if (parentId == 1) {
		switchTab("Insights")
	}

	const submenu = document.getElementById(`submenu-${parentId}`);
	const arrow = document.getElementById(`arrow-${parentId}`);

	if (!submenu) {
		return;
	}

	submenu.classList.toggle("hidden");

	if (arrow) {
		arrow.classList.toggle("rotate-180");
	}
}

function switchTab(menuName, menuUrl) {

	console.log("Selected Menu:", menuName);
	console.log("URL:", menuUrl);

	if (menuUrl && menuUrl !== "#") {
		window.location.href = menuUrl;
	}
}

function getMenuIcon(menuName) {

	const name = menuName.toLowerCase();

	const iconClass = "w-5 h-5 text-slate-800 stroke-[2.8]";

	switch (name) {

		case "dashboard":
			return `<i data-lucide="layout-dashboard" class="${iconClass}"></i>`;

		case "product":
			return `<i data-lucide="package" class="${iconClass}"></i>`;

		case "service":
			return `<i data-lucide="briefcase" class="${iconClass}"></i>`;

		case "asset":
			return `<i data-lucide="boxes" class="${iconClass}"></i>`;

		case "vendor":
			return `<i data-lucide="building-2" class="${iconClass}"></i>`;

		case "customer":
			return `<i data-lucide="users" class="${iconClass}"></i>`;

		case "automation":
			return `<i data-lucide="bot" class="${iconClass}"></i>`;

		case "extracted data":
			return `<i data-lucide="database" class="${iconClass}"></i>`;

		case "reference data":
			return `<i data-lucide="book-open" class="${iconClass}"></i>`;

		case "classification":
			return `<i data-lucide="tags" class="${iconClass}"></i>`;

		case "characteristics":
			return `<i data-lucide="list-tree" class="${iconClass}"></i>`;

		case "workflow":
			return `<i data-lucide="git-branch" class="${iconClass}"></i>`;

		case "vendor master":
			return `<i data-lucide="contact-round" class="${iconClass}"></i>`;

		case "customer master":
			return `<i data-lucide="user-round" class="${iconClass}"></i>`;

		case "hierarchy":
			return `<i data-lucide="network" class="${iconClass}"></i>`;

		case "compliance":
			return `<i data-lucide="shield-check" class="${iconClass}"></i>`;

		case "reference":
			return `<i data-lucide="book" class="${iconClass}"></i>`;

		case "data quality":
			return `<i data-lucide="badge-check" class="${iconClass}"></i>`;

		case "profiling":
			return `<i data-lucide="chart-column" class="${iconClass}"></i>`;

		case "validation rules":
			return `<i data-lucide="check-check" class="${iconClass}"></i>`;

		case "duplicate detection":
			return `<i data-lucide="copy" class="${iconClass}"></i>`;

		case "standardization":
			return `<i data-lucide="wand-sparkles" class="${iconClass}"></i>`;

		case "governance":
			return `<i data-lucide="shield" class="${iconClass}"></i>`;

		case "business rules":
			return `<i data-lucide="gavel" class="${iconClass}"></i>`;

		case "change requests":
			return `<i data-lucide="file-pen-line" class="${iconClass}"></i>`;

		case "approval queue":
			return `<i data-lucide="clipboard-check" class="${iconClass}"></i>`;

		case "audit trail":
			return `<i data-lucide="history" class="${iconClass}"></i>`;

		case "integration":
			return `<i data-lucide="plug" class="${iconClass}"></i>`;

		case "import data":
			return `<i data-lucide="file-down" class="${iconClass}"></i>`;

		case "export data":
			return `<i data-lucide="file-up" class="${iconClass}"></i>`;

		case "api management":
			return `<i data-lucide="cloud" class="${iconClass}"></i>`;

		case "reports & analytics":
			return `<i data-lucide="bar-chart-3" class="${iconClass}"></i>`;

		case "administration":
			return `<i data-lucide="user-cog" class="${iconClass}"></i>`;

		case "users":
			return `<i data-lucide="users" class="${iconClass}"></i>`;

		case "roles":
			return `<i data-lucide="user-check" class="${iconClass}"></i>`;

		case "permissions":
			return `<i data-lucide="lock" class="${iconClass}"></i>`;

		case "settings":
			return `<i data-lucide="settings-2" class="${iconClass}"></i>`;

		default:
			return `<i data-lucide="circle" class="${iconClass}"></i>`;
	}
}


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





function downloadErrors() {
	window.location = "/auth/errors";
}

function loadTable(batchId, tableId) {
	if (batchId == null || batchId == "" || batchId == undefined) {
		batchId = "NULL";
	}
	$("#view-ai-automation div").hide();
	$("#web-breadcrumbs").show();
	$("#automationMainDivId").show();
	$("#tableDataSection").show();
	$("#uploadSection").hide();
	$("#mappingSection").hide();
	$("#uploadButtonsId").hide();

	if ($.fn.DataTable.isDataTable('#' + tableId)) {
		$('#' + tableId).DataTable().destroy();
	}
	loader(true);
	let selectedBatchId = $("#batchSelectId").val();
	let finalBatchId = batchId || selectedBatchId;

	if (finalBatchId != null && finalBatchId != "" && finalBatchId != undefined) {
		let page = 0;
		let size = 10;
		let search = "";
		$.ajax({
			url: `/api/batch/${finalBatchId}?tableId=${tableId}&page=${page}&size=${size}&search=${encodeURIComponent(search)}`,
			type: 'GET',
			success: function(response) {
				loader(false);

				console.log("Classification Response:", response);

				if (response) {

					// Build dynamic header
					let headerHtml = `
            <th width="50">
                <input type="checkbox" id="selectAllRows">
            </th>
        `;

					let columns = [{
						data: null,
						orderable: false,
						searchable: false,
						render: function(data, type, row) {
							return `<input type="checkbox" class="rowCheckbox" value="${row.id}">`;
						}
					}];

					response.columns.forEach(col => {

						let ColumnHeaderName = col.data
							.replace(/([A-Z])/g, ' $1')
							.replace(/^./, str => str.toUpperCase());

						headerHtml += `<th>${ColumnHeaderName}</th>`;

						if (col.type === "id") {

							columns.push({
								data: col.data
							});

						} else if (col.type === "textarea") {

							columns.push({
								data: col.data,
								render: function(data, type, row) {
									return `<textarea
                            class="editable ${col.data}"
                            style="width:${col.width};"
                            data-id="${row.id}"
                            data-field="${col.data}">${data || ''}</textarea>`;
								}
							});

						} else {

							columns.push({
								data: col.data,
								render: function(data, type, row) {
									return `<input type="text"
                            class="editable ${col.data}"
                            data-id="${row.id}"
                            data-field="${col.data}"
                            value="${data || ''}">`;
								}
							});

						}
					});

					$("#" + tableId).html(`
                <thead>
                    <tr>
                        ${headerHtml}
                    </tr>
                </thead>
        `);

					let table = $('#' + tableId).DataTable({
						processing: true,
						serverSide: true,
						searching: false,
						ordering: true,
						scrollY: "250px",
						scrollX: true,
						scrollCollapse: true,
						paging: true,
						pageLength: 10,
						lengthMenu: [10, 25, 50, 100],
						info: true,
						autoWidth: true,

						dom: '<"top flex items-center justify-between"Bfl>rtip',

						ajax: function(data, callback) {

							let page = data.start / data.length;
							let size = data.length;
							let search = data.search.value;
							// Get sorting info
							let sortBy = "id"; // default column
							let sortDir = "asc";

							if (data.order && data.order.length > 0) {
								let columnIndex = data.order[0].column;
								sortDir = data.order[0].dir;
								sortBy = data.columns[columnIndex].data;
							}

							fetch(
								`/api/batch/${finalBatchId}` +
								`?tableId=${tableId}` +
								`&page=${page}` +
								`&size=${size}` +
								`&search=${encodeURIComponent(search)}` +
								`&sortBy=${encodeURIComponent(sortBy)}` +
								`&sortDir=${encodeURIComponent(sortDir)}`
							)
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
									console.error(err);
								});
						},

						columns: columns,

						dom: '<"top  items-center justify-between"Bfl>rtip',

						buttons: [
							{
								extend: 'excelHtml5',
								className: 'buttons-excel',
								exportOptions: {
									columns: ':visible:not(:first-child)',
									format: {
										body: function(data, row, column, node) {
											let $node = $(node);
											let input = $node.find('input, textarea');
											if (input.length > 0) {
												return input.val();
											}
											return data;
										}
									}
								}
							},
							{
								extend: 'csvHtml5',
								className: 'buttons-csv',
								exportOptions: {
									format: {
										body: function(data, row, column, node) {
											let $node = $(node);
											let input = $node.find('input, textarea');
											return input.length ? input.val() : data;
										}
									}
								}
							},
							{
								extend: 'pdfHtml5',
								className: 'buttons-pdf',
								exportOptions: {
									format: {
										body: function(data, row, column, node) {
											let $node = $(node);
											let input = $node.find('input, textarea');
											return input.length ? input.val() : data;
										}
									}
								}
							}
						],

						initComplete: function() {
							loadTableButtons(tableId, table);
						}
					});
				}
			},
			error: function(error) {
				loader(false);
				console.error("Error loading table:", error);
			}
		});
	} else {
		loader(false);
		console.warn("No batch ID provided");
	}
}



function getSelectedIds(tableId) {

	let ids = [];

	$("#" + tableId + '.rowCheckbox:checked').each(function() {
		ids.push($(this).val());
	});

	return ids;
}

function updateSelectedRecords(tableId) {
	loader(true);

	let selectedRows = getSelectedRows(tableId);

	if (selectedRows.length === 0) {
		loader(false);
		openDialog({
			title: "Message",
			message: "Please select at least one record.",
			width: 400
		});
		return;
	}


	fetch(`/api/updateRecords?tableId=${encodeURIComponent(tableId)}`, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},

		body: JSON.stringify(selectedRows)
	})
		.then(response => response.text())
		.then(result => {
			loader(false);

			openDialog({
				title: "Message",
				message: result,
				width: 400
			});

			$('#' + tableId).DataTable().ajax.reload(null, false);

		})
		.catch(error => {
			console.error(error);
			alert('Update failed');
		});
}

function getSelectedRows(tableId) {
	let rows = [];

	$('#' + tableId + ' tbody tr').each(function() {
		let checkbox = $(this).find('.rowCheckbox');
		if (checkbox.length && checkbox.is(':checked')) {
			let rowData = $('#' + tableId).DataTable().row(this).data();
			if (rowData) {
				rows.push(rowData);
			}
		}
	});

	return rows;
}




function deleteSelectedRecords(tableId) {
	loader(true);

	let selectedIds = getSelectedIds(tableId);

	if (selectedIds.length === 0) {
		loader(false)
		openDialog({
			title: "Message",
			message: "Please select at least one record.",
			width: 400
		});
		return;
	}

	console.log("Delete IDs:", selectedIds);

	fetch('/api/deleteRecords', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		 body: JSON.stringify({
        tableId: tableId,
        selectedIds: selectedIds
    })
	})
		.then(response => response.text())
		.then(result => {
			loader(false);

			openDialog({
				title: "Message",
				message: result,
				width: 400
			});

			$('#'+tableId).DataTable().ajax.reload(null, false);

		})
		.catch(error => {
			console.error(error);
			alert('Delete failed');
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
	$(".modal-content-class .grid").each(function() {
		if (!batchId) {
			batchId = $(this).find("select").val();
		}
	});

	if (batchId) {
		loadTable(batchId);
	}

	document.getElementById('configModal')
		.classList.toggle('hidden', !show);
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
		.filter(function() {
			return $(this).text().trim().toLowerCase() === "upload";
		})
		.attr("class", "text-slate-900 font-semibold");
	mapping.classList.add('hidden');
	$("#mappingSection").hide();
	uploadSection.classList.remove('hidden');
	tableDataSection.classList.add('hidden');
	$("#tableDataSection").hide();
	$("#uploadButtonsId").show();

}
function getBatchIdList(selectId) {
	loader(true);
	$.ajax({

		url: "/api/batchList",

		type: "GET",

		success: function(response) {
			loader(false);

			let optionStr = "<option value=''>All Batches</option>";

			if (response && response.length > 0) {

				response.forEach(function(batchId) {

					optionStr += `
                        <option value="${batchId}">
                            ${batchId}
                        </option>
                    `;

				});

			}
			if (selectId != null && selectId != "" && selectId != undefined) {
				$("#" + selectId).html(optionStr);
			} else {
				$("#batchSelectId").html(optionStr);
			}


		},

		error: function(xhr) {

			console.log(xhr);

			alert("Error while fetching Batch Id List");

		}

	});

}
function loadDuplicates() {
	loader(true)

	let batchId = $("#batchSelectId").val();
	let accuracy = 100;

	if (!batchId) {
		loader(false)
		openDialog({
			title: "Message",
			message: "Please select one BatchId.",
			width: 400
		});
		return;
	}

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
			loader(false)

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
				openDialog({
					title: "DUPLICATES",
					message: "No Duplicate Records Found",
					width: 400
				});

			}

		},

		error: function(xhr) {
			loader(false)
			console.log(xhr);
			openDialog({
				title: "DUPLICATES",
				message: xhr.status + " " + xhr.statusText,
				width: 400
			});


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

function classifyData() {
	loader(true);
	let batchId = $("#batchSelectId").val();
	if (batchId != null && batchId != "" && batchId != undefined) {
		$.ajax({
			url: '/api/classify?batchId=' + batchId,
			type: 'POST',
			contentType: 'application/json',
			data: JSON.stringify({}),
			success: function(response) {
				loader(false);

				console.log("Classification Response:", response);

				if (response && response.length > 0) {
					let count = 0;

					response.forEach(function(item) {
						console.log("Category :", item.category);
						console.log("Noun :", item.noun);
						console.log("Modifier :", item.modifier);
						console.log("Confidence :", item.confidence);
						count++;
					});

					openDialog({
						title: "AI ClASS",
						message: "Total Classes updated: " + count + " Out of " + response.length,
						width: 400
					});
					loadTable(batchId);
				} else {
					openDialog({
						title: "AI ClASS",
						message: "No Classes Found",
						width: 400
					});
				}
			},
			error: function(xhr) {
				loader(false);
				openDialog({
					title: "AI ClASS",
					message: xhr.responseText,
					width: 400
				});

				console.log("Error :", xhr.responseText);
			}
		});
	}
	else {
		loader(false)
		openDialog({
			title: "Message",
			message: "Please select one BatchId.",
			width: 400
		});
	}
}

function charData() {
	loader(true);
	let batchId = $("#batchSelectId").val();
	if (batchId != null && batchId != "" && batchId != undefined) {
		$.ajax({
			url: '/api/characterstics?batchId=' + batchId,
			type: 'POST',
			contentType: 'application/json',
			data: JSON.stringify({}),
			success: function(response) {
				loader(false);

				console.log("Classification Response:", response);

				if (response && response.length > 0) {
					let count = 0;

					response.forEach(function(item) {
						console.log("Category :", item.category);
						console.log("Noun :", item.noun);
						console.log("Modifier :", item.modifier);
						console.log("Confidence :", item.confidence);
						count++;
					});

					openDialog({
						title: "AI CHAR RUN",
						message: "Total Classes updated: " + count + " Out of " + response.length,
						width: 400
					});
					loadTable(batchId);
				} else {
					openDialog({
						title: "AI CHAR RUN",
						message: "No Classes Found",
						width: 400
					});
				}
			},
			error: function(xhr) {
				loader(false);
				openDialog({
					title: "AI CHAR RUN",
					message: xhr.responseText,
					width: 400
				});

				console.log("Error :", xhr.responseText);
			}
		});
	}
	else {
		loader(false)
		openDialog({
			title: "Message",
			message: "Please select one BatchId.",
			width: 400
		});
	}
}

function enrinchData() {
	loader(true);
	let batchId = $("#batchSelectId").val();
	if (batchId != null && batchId != "" && batchId != undefined) {
		$.ajax({
			url: '/api/enrinch?batchId=' + batchId,
			type: 'POST',
			contentType: 'application/json',
			data: JSON.stringify({}),
			success: function(response) {
				loader(false);

				console.log("Classification Response:", response);

				if (response && response.length > 0) {
					let count = 0;

					response.forEach(function(item) {
						console.log("Category :", item.category);
						console.log("Noun :", item.noun);
						console.log("Modifier :", item.modifier);
						console.log("Confidence :", item.confidence);
						count++;
					});

					openDialog({
						title: "AI CHAR RUN",
						message: "Total Classes updated: " + count + " Out of " + response.length,
						width: 400
					});
					loadTable(batchId);
				} else {
					openDialog({
						title: "AI CHAR RUN",
						message: "No Classes Found",
						width: 400
					});
				}
			},
			error: function(xhr) {
				loader(false);
				openDialog({
					title: "AI CHAR RUN",
					message: xhr.responseText,
					width: 400
				});

				console.log("Error :", xhr.responseText);
			}
		});
	}
	else {
		loader(false)
		openDialog({
			title: "Message",
			message: "Please select one BatchId.",
			width: 400
		});
	}
}
function missingData() {
	loader(true);
	let batchId = $("#batchSelectId").val();
	if (batchId != null && batchId != "" && batchId != undefined) {
		$.ajax({
			url: '/api/enrinch?batchId=' + batchId,
			type: 'POST',
			contentType: 'application/json',
			data: JSON.stringify({}),
			success: function(response) {
				loader(false);

				console.log("Classification Response:", response);

				if (response && response.length > 0) {
					let count = 0;

					response.forEach(function(item) {
						console.log("Category :", item.category);
						console.log("Noun :", item.noun);
						console.log("Modifier :", item.modifier);
						console.log("Confidence :", item.confidence);
						count++;
					});

					openDialog({
						title: "AI CHAR RUN",
						message: "Total Classes updated: " + count + " Out of " + response.length,
						width: 400
					});
					loadTable(batchId);
				} else {
					openDialog({
						title: "AI CHAR RUN",
						message: "No Classes Found",
						width: 400
					});
				}
			},
			error: function(xhr) {
				loader(false);
				openDialog({
					title: "AI CHAR RUN",
					message: xhr.responseText,
					width: 400
				});

				console.log("Error :", xhr.responseText);
			}
		});
	}
	else {
		loader(false)
		openDialog({
			title: "Message",
			message: "Please select one BatchId.",
			width: 400
		});
	}
}
function openDialog(options) {
	$("#commonDialog").show();
	$("#commonDialog")
		.html(options.message || "")
		.dialog({
			modal: true,
			title: options.title || "Message",
			width: options.width || 500,
			height: options.height || "auto",
			resizable: false,
			buttons: options.buttons || {
				"OK": function() {
					$(this).dialog("close");
				}
			},
			close: function() {
				$(this).html("");
			}
		});
}


function mappingDialog(options) {
	$("#mappingSection").show();
	$("#mappingSection")
		.dialog({
			modal: true,
			title: options.title || "Message",
			width: options.width || 500,
			height: options.height || "auto",
			resizable: false,
			close: function() {
				$(this).dialog("close");
			}
		});
}
function loader(display) {
	if (display) {
		$("#loaderDivId").show();
		$("body").css("pointer-events", "none");
		$("#loaderDivId").css("pointer-events", "auto"); // keep loader clickable
	} else {
		$("#loaderDivId").hide();
		$("body").css("pointer-events", "auto");
	}
}

function showTab(tabId, tableId) {
	document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
	document.getElementById(tabId).classList.add('active');
	document.querySelectorAll('button[id^="tab-"]').forEach(b => b.classList.remove('text-indigo-600'));
	const selectedBtn = document.getElementById('tab-' + tabId);
	selectedBtn.classList.add('text-indigo-600');
	const pill = document.getElementById('pill');
	const containerRect = document.getElementById('tab-container').getBoundingClientRect();
	const tabRect = selectedBtn.getBoundingClientRect();
	pill.style.width = tabRect.width + 'px';
	pill.style.transform = `translateX(${tabRect.left - containerRect.left}px)`;
	if (tabId && tabId != null && tabId != "" && tabId != undefined) {
		let batchExtSelectId = $("#batchExtSelectId").val();
		let batchId = batchExtSelectId != null && batchExtSelectId != "" && batchExtSelectId != undefined ? batchExtSelectId : "NULL"
		loadTable(batchId, tableId);

	}
}
function loadTableButtons(tableId, table, buttonList = []) {
	if (tableId == "dynamicTable") {
		buttonList = ["import", "class", "char", "duplicate", "enrich", "missing", "duplicate", "description"]
	}

	let dynamicButtons = "";

	const buttonConfig = {
		import: {
			action: `document.getElementById('fileInput').click()`,
			title: "Import",
			class: "bg-green-600",
			icon: "upload"
		},
		class: {
			action: "classifyData()",
			title: "AI CLASS",
			class: "bg-amber-400",
			icon: "star"
		},
		char: {
			action: "charData()",
			title: "AI CHAR RUN",
			class: "bg-blue-500",
			icon: "zap"
		},
		enrich: {
			action: "enrinchData()",
			title: "ENRICH",
			class: "bg-teal-500",
			icon: "globe"
		},
		missing: {
			action: "missingData()",
			title: "MISSING DATA",
			class: "bg-red-500",
			icon: "alert-triangle"
		},
		duplicate: {
			action: "loadDuplicates()",
			title: "DUPLICATES",
			class: "bg-green-600",
			icon: "file-text"
		},
		description: {
			action: "loadDescription()",
			title: "DESCRIPTION",
			class: "bg-teal-500",
			icon: "layers"
		}
	};

	buttonList.forEach(btn => {
		let cfg = buttonConfig[btn.toLowerCase()];
		if (cfg) {
			dynamicButtons += `
				<span onclick="${cfg.action}"
					title="${cfg.title}"
					class="${cfg.class} text-white px-4 py-2 rounded font-semibold flex items-center gap-2 text-sm">
					<i data-lucide="${cfg.icon}" class="w-4 h-4"></i>
				</span>
			`;
		}
	});

	$('#' + tableId + '_wrapper .top').append(`
		<div class="tableButtonDivClass flex gap-2 ml-4 items-center">

			${dynamicButtons}

			<span id="updateBtn${tableId}" title="Update"
				class="flex items-center gap-2 px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition">
				<i data-lucide="square-pen" class="w-4 h-4"></i>
			</span>

			<span id="deleteBtn${tableId}" title="Delete"
				class="flex items-center gap-2 px-3 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition">
				<i data-lucide="trash-2" class="w-4 h-4"></i>
			</span>

			<div class="relative">
				<button id="exportMenuBtn${tableId}" title="Export"
					class="flex items-center gap-2 px-3 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition">
					<i data-lucide="download" class="w-4 h-4"></i>
				</button>

				<div id="exportDropdown${tableId}"
					class="hidden absolute mt-2 w-40 bg-white shadow-lg rounded-md border z-50">
					<button class="export-option w-full text-left px-3 py-2 hover:bg-gray-100" data-type="excel">Excel</button>
					<button class="export-option w-full text-left px-3 py-2 hover:bg-gray-100" data-type="csv">CSV</button>
					<button class="export-option w-full text-left px-3 py-2 hover:bg-gray-100" data-type="pdf">PDF</button>
				</div>
			</div>

		</div>
	`);

	lucide.createIcons();
	$(".dt-buttons").hide();
	$(document).on('focus', '.editable' + tableId, function() {
		focusedId = $(this).data('id');
		focusedField = $(this).data('field');
	});

	// Track cursor position while typing
	$(document).on('keyup click', '.editable' + tableId, function() {
		cursorPos = this.selectionStart;
	});

	$(document).off("click", "#exportMenuBtn" + tableId)
		.on("click", "#exportMenuBtn", function() {
			$("#exportDropdown").toggleClass("hidden");
		});

	$(document).off("click", ".export-option" + tableId)
		.on("click", ".export-option", function() {
			let type = $(this).data("type");
			$("#exportDropdown").addClass("hidden");
			if (type === "excel") table.button(0).trigger();
			if (type === "csv") table.button(1).trigger();
			if (type === "pdf") table.button(2).trigger();
		});

	$(document).off("click", "#updateBtn" + tableId)
		.on("click", "#updateBtn" + tableId, function() {
			updateSelectedRecords(tableId);
		});

	$(document).off("click", "#deleteBtn" + tableId)
		.on("click", "#deleteBtn" + tableId, function() {
			deleteSelectedRecords(tableId);
		});

	$(document).off("change", "#selectAllRows" + tableId)
		.on("change", "#selectAllRows", function() {
			$('.rowCheckbox').prop('checked', this.checked);
		});

	$(document).off("input", ".editable" + tableId)
		.on("input", ".editable", function() {
			let $input = $(this);
			let tr = $input.closest('tr');
			let rowData = table.row(tr).data();
			if (!rowData) {
				return;
			}
			rowData[$input.data('field')] = $input.val();
			tr.find(".rowCheckbox").prop("checked", true);
		});

	$(document).on("click", function(e) {
		if (!$(e.target).closest("#exportMenuBtn+tableId, #exportDropdown+tableId").length) {
			$("#exportDropdown").addClass("hidden");
		}
	});
}


