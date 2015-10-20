function humanFileSize(bytes, si) {
	var thresh = si ? 1000 : 1024;
	if (Math.abs(bytes) < thresh) {
		return bytes + ' B';
	}
	var units = si ? [ 'kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB' ] : [
			'KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB', 'ZiB', 'YiB' ];
	var u = -1;
	do {
		bytes /= thresh;
		++u;
	} while (Math.abs(bytes) >= thresh && u < units.length - 1);
	return bytes.toFixed(1) + ' ' + units[u];
}

$('#fileList').on(
		'show.bs.modal',
		function(event) {
			var button = $(event.relatedTarget) // Button that triggered the modal
			var downloadableId = button.data('downloadable') // Extract info from data-* attributes
			// If necessary, you could initiate an AJAX request here (and then do the updating in a callback).
			// Update the modal's content. We'll use jQuery here, but you could use a data binding library or other methods instead.
			var modal = $(this)

			$("#fileListTable tbody").empty();

			$.ajax({
				url : "../services/file-list?downloadableId=" + downloadableId,
				dataType : "json",
				context : document.body
			}).done(
					function(data) {
						var table = $("#fileListTable tbody");
						$.each(data, function(idx, row) {
							table.append("<tr><td><a class='btn btn-sm btn-danger deleteButton' data-path=\"" + row.filePath + "\"><i class='fa fa-trash'></i></a></td><td>" + row.filePath
									+ "</td><td>"
									+ humanFileSize(row.size, false)
									+ "</td></tr>");
						});
						$('.deleteButton').click( function() {
							var path = $(this).data('path');
							if (confirm("Are you sure you want to delete " + path + " ?")) {
								$.ajax({
									url : "../services/file-list?path=" + path,
									method: "DELETE"
								});
								$(this).closest("tr").remove();
							}
						});
					});

		});

function showFiles(e) {
	if (e.status == 'success') {
		$('#fileList').modal();
	}
}
function removeFile(data) {
	if (data.status == 'begin') {
		removeParent($(data.source), 'tr');
	}
}
