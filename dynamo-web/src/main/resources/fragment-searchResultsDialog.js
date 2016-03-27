$('#searchResultsList').on(
		'show.bs.modal',
		function(event) {
			var button = $(event.relatedTarget) // Button that triggered the modal
			var downloadableId = button.data('downloadable') // Extract info from data-* attributes
			// If necessary, you could initiate an AJAX request here (and then do the updating in a callback).
			// Update the modal's content. We'll use jQuery here, but you could use a data binding library or other methods instead.
			var modal = $(this)

			$("#searchResultsListTable tbody").empty();

			$.ajax({
				url : "../services/searchResults/" + downloadableId,
				dataType : "json",
				context : document.body
			}).done(
					function(data) {
						var table = $("#searchResultsListTable tbody");
						$.each(data, function(idx, row) {
							table.append("<tr><td>" + row.title + "</td><td>" + row.type + "</td><td>" + row.sizeInMegs + "</td><td><a href='" + row.url + "'>" + row.url + "</a></td></tr>");
						});
					});

		});