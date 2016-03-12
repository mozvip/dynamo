$('#suggestionsList').on(
		'show.bs.modal',
		function(event) {
			var button = $(event.relatedTarget) // Button that triggered the modal
			var downloadableId = button.data('downloadable') // Extract info from data-* attributes
			// If necessary, you could initiate an AJAX request here (and then do the updating in a callback).
			// Update the modal's content. We'll use jQuery here, but you could use a data binding library or other methods instead.
			var modal = $(this)

			$("#suggestionsListTable tbody").empty();

			$.ajax({
				url : "../services/suggestions/" + downloadableId,
				dataType : "json",
				context : document.body
			}).done(
					function(data) {
						var table = $("#suggestionsListTable tbody");
						$.each(data, function(idx, row) {
							table.append("<tr><td><a href='" + row.url + "'>" + row.url + "</a></td></tr>");
						});
					});

		});

function showSuggestions(e) {
	if (e.status == 'success') {
		$('#suggestionsList').modal();
	}
}