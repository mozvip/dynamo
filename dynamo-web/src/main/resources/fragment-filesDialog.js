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
							table.append("<tr><td></td><td>" + row.filePath
									+ "</td><td>"
									+ humanFileSize(row.size, false)
									+ "</td></tr>");
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
