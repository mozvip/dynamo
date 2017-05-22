@ECHO OFF
If NOT exist "music" (
	md music
)
If NOT exist "music-incoming" (
	md music-incoming
)
If NOT exist "movies" (
	md movies
)
If NOT exist "tvshows" (
	md tvshows
)
If NOT exist "magazines" (
	md magazines
)
If NOT exist "books" (
	md books
)
If NOT exist "games" (
	md games
)
vagrant up