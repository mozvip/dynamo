function disappear( animation, item ) {
	item.addClass( animation + ' animated');
	setTimeout(function() {
		item.remove();
	}, 1000);
}