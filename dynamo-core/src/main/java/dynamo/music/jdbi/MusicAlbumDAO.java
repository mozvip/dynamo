package dynamo.music.jdbi;

import java.nio.file.Path;
import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import dynamo.core.model.DownloadableDAO;
import dynamo.jdbi.core.BindContains;
import dynamo.jdbi.core.BindEnum;
import dynamo.jdbi.core.BindPath;
import dynamo.jdbi.core.BindUpper;
import dynamo.model.DownloadableStatus;
import dynamo.model.music.MusicAlbum;
import dynamo.model.music.MusicArtist;
import dynamo.model.music.MusicFile;
import dynamo.model.music.MusicQuality;

public interface MusicAlbumDAO extends DownloadableDAO<MusicAlbum> {

	@SqlQuery("SELECT DOWNLOADABLE.*, MUSICALBUM.* FROM MUSICALBUM INNER JOIN DOWNLOADABLE ON MUSICALBUM.ID = DOWNLOADABLE.ID WHERE MUSICALBUM.ID=:musicAlbumId")
	@Mapper(MusicAlbumMapper.class)
	MusicAlbum find( @Bind("musicAlbumId") long musicAlbumId );

	@SqlQuery("SELECT DOWNLOADABLE.*, MUSICALBUM.* FROM MUSICALBUM INNER JOIN DOWNLOADABLE ON MUSICALBUM.ID = DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS= :status ORDER BY MUSICALBUM.ARTIST_NAME ASC")
	@Mapper(MusicAlbumMapper.class)
	List<MusicAlbum> findByStatus(@BindEnum("status") DownloadableStatus status);

	@SqlQuery("SELECT * FROM MUSICFILE WHERE TAGSMODIFIED=true")
	@Mapper(MusicFileMapper.class)
	List<MusicFile> findModifiedTags();

	@SqlQuery("SELECT * FROM MUSICFILE WHERE PATH=:path")
	@Mapper(MusicFileMapper.class)
	MusicFile findMusicFile(@BindPath("path") Path path);
	
	@SqlQuery("select * from MUSICALBUM INNER JOIN DOWNLOADABLE ON MUSICALBUM.ID = DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS = 'DOWNLOADED' AND NOT EXISTS( SELECT * FROM MUSICFILE WHERE MUSICFILE.ALBUM_ID = MUSICALBUM.ID)")
	List<MusicAlbum> findDeadAlbums();

	@SqlQuery("SELECT NAME FROM MUSICARTIST WHERE UPPER(NAME) LIKE :param ORDER BY NAME")
	List<String> suggestArtists( @Bind("param") String param );

	@SqlQuery("SELECT MUSICARTIST.* FROM MUSICARTIST WHERE UPPER(name) = :artistName")
	@Mapper(MusicArtistMapper.class)
	MusicArtist findArtist(@BindUpper("artistName") String artistName);

	@SqlUpdate("UPDATE MUSICARTIST SET BLACKLISTED=true WHERE UPPER(name) = :artistName")
	void blackList(@BindUpper("artistName") String artistName);

	@SqlUpdate("UPDATE MUSICARTIST SET ALLMUSICURL=:url WHERE name=:artistName")
	void updateAllMusicURL(@Bind("artistName") String artistName, @Bind("url") String url);

	@SqlUpdate("UPDATE MUSICALBUM SET ALLMUSICURL=:allMusicURL WHERE id=:albumId")
	void updateAllMusicURL(@Bind("albumId") long albumId, @Bind("allMusicURL") String allMusicURL);
	
	@SqlUpdate("MERGE INTO MUSICALBUM(ID, ARTIST_NAME, ALLMUSICURL, GENRE, QUALITY, SEARCHSTRING, FOLDER) VALUES(:albumId, :artistName, :allMusicURL, :genre, :quality, :searchString, :folder)")
	public void save(@Bind("albumId") long albumId, @Bind("artistName") String artistName, @Bind("allMusicURL") String allMusicURL, @Bind("genre") String genre, @BindEnum("quality") MusicQuality quality, @Bind("searchString") String searchString, @BindPath("folder") Path folder);

	@SqlQuery("SELECT * FROM MUSICFILE WHERE ALBUM_ID=:albumId ORDER BY TRACK")
	@Mapper(MusicFileMapper.class)
	List<MusicFile> findMusicFiles(@Bind("albumId") long albumId);

	@SqlQuery("SELECT * FROM MUSICFILE INNER JOIN MUSICALBUM ON MUSICFILE.ALBUM_ID=MUSICALBUM.ID WHERE MUSICALBUM.ARTIST_NAME=:artistName ORDER BY TRACK")
	@Mapper(MusicFileMapper.class)
	List<MusicFile> findMusicFiles(@Bind("artistName") String artistName);

	@SqlUpdate("MERGE INTO MUSICFILE(PATH, ALBUM_ID, SONGTITLE, SONGARTIST, TRACK, YEAR, SIZE, TAGSMODIFIED) VALUES(:path, :albumId, :songTitle, :songArtist, :track, :year, :size, :tagsModified)")
	void createMusicFile(@BindPath("path") Path path, @Bind("albumId") long albumId, 
			@Bind("songTitle") String songTitle, @Bind("songArtist") String songArtist, @Bind("track") int track, @Bind("year") int year, @Bind("size") long size, @Bind("tagsModified") boolean tagsModified);

	@SqlUpdate("INSERT INTO MUSICARTIST(NAME, ALLMUSICURL, BLACKLISTED, FAVORITE, AKA) VALUES(:name, :allMusicURL, :blackListed, :favorite, :aka)")
	void createArtist(@Bind("name") String name, @Bind("allMusicURL") String allMusicURL, @Bind("blackListed") boolean blackListed, @Bind("favorite") boolean favorite, @Bind("aka") String aka);

	@SqlQuery("SELECT DOWNLOADABLE.*, MUSICALBUM.* FROM MUSICALBUM INNER JOIN DOWNLOADABLE ON MUSICALBUM.ID = DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS='SUGGESTED' ORDER BY MUSICALBUM.ARTIST_NAME ASC")
	@Mapper(MusicAlbumMapper.class)
	List<MusicAlbum> findSuggestions();

	@SqlQuery("SELECT DOWNLOADABLE.*, MUSICALBUM.* FROM MUSICALBUM INNER JOIN DOWNLOADABLE ON MUSICALBUM.ID = DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS=:status AND UPPER(MUSICALBUM.ARTIST_NAME) LIKE :artistSearchName ORDER BY MUSICALBUM.ARTIST_NAME ASC")
	@Mapper(MusicAlbumMapper.class)
	List<MusicAlbum> find( @BindContains("artistSearchName") String artistSearchName, @BindEnum("status") DownloadableStatus status);

	@SqlQuery("SELECT MUSICARTIST.* FROM MUSICARTIST WHERE UPPER(NAME) LIKE :filter ORDER BY NAME ASC")
	@Mapper(MusicArtistMapper.class)
	List<MusicArtist> findArtists(@BindContains("filter") String filter);

	@SqlQuery("SELECT DOWNLOADABLE.*, MUSICALBUM.* FROM MUSICALBUM INNER JOIN DOWNLOADABLE ON MUSICALBUM.ID = DOWNLOADABLE.ID WHERE SEARCHSTRING=:searchString")
	@Mapper(MusicAlbumMapper.class)
	MusicAlbum findBySearchString( @Bind("searchString") String searchString );

	@SqlQuery("SELECT DOWNLOADABLE.*, MUSICALBUM.* FROM MUSICALBUM INNER JOIN DOWNLOADABLE ON DOWNLOADABLE.ID = MUSICALBUM.ID WHERE ARTIST_NAME = :artistName")
	@Mapper(MusicAlbumMapper.class)
	List<MusicAlbum> findAllAlbumsForArtist(@Bind("artistName") String artistName);

	@SqlQuery("SELECT DOWNLOADABLE.*, MUSICALBUM.* FROM MUSICALBUM INNER JOIN DOWNLOADABLE ON DOWNLOADABLE.ID = MUSICALBUM.ID WHERE ARTIST_NAME = :artistName AND DOWNLOADABLE.STATUS='DOWNLOADED'")
	@Mapper(MusicAlbumMapper.class)
	List<MusicAlbum> findDownloadedAlbumsForArtist(@Bind("artistName") String artistName);

	@SqlUpdate("UPDATE MUSICARTIST SET FAVORITE = :favorite WHERE NAME = :name")
	void updateFavorite(@Bind("name") String name, @Bind("favorite") boolean favorite);

	@SqlUpdate("DELETE FROM MUSICFILE WHERE PATH=:path")
	void deleteFile(@BindPath("path") Path path);

	@SqlUpdate("DELETE FROM MUSICARTIST WHERE NAME=:artistName")
	void deleteArtist(@Bind("artistName") String artistName);

	@SqlUpdate("UPDATE MUSICFILE SET ALBUM_ID=:albumId, SONGARTIST=:songArtist, SONGTITLE=:songTitle, TRACK=:track, YEAR=:year, SIZE=:size, TAGSMODIFIED=:tagsModified WHERE PATH=:path")
	void updateMusicFile(@BindPath("path") Path path, @Bind("albumId") long albumId, @Bind("songArtist") String songArtist, @Bind("songTitle") String songTitle,  @Bind("track") int track,  @Bind("year") int year,  @Bind("size") long size, @Bind("tagsModified") boolean tagsModified);

	@SqlUpdate("UPDATE MUSICFILE SET TAGSMODIFIED=:tagsModified WHERE PATH=:path")
	void updateTagsModified(@BindPath("path") Path path, @Bind("tagsModified") boolean tagsModified);

	@SqlQuery("SELECT DOWNLOADABLE.*, MUSICALBUM.* FROM MUSICALBUM INNER JOIN DOWNLOADABLE ON DOWNLOADABLE.ID = MUSICALBUM.ID WHERE ARTIST_NAME = :artistName AND (DOWNLOADABLE.STATUS='DOWNLOADED' OR DOWNLOADABLE.STATUS='WANTED')")
	@Mapper(MusicAlbumMapper.class)
	List<MusicAlbum> findAllAlbumsToDisplayForArtist(@Bind("artistName") String artistName);

	@SqlQuery("SELECT * FROM MUSICFILE WHERE POSITION(:folder, PATH) = 1")
	@Mapper(MusicFileMapper.class)
	List<MusicFile> findFilesInFolder(@BindPath("folder") Path folder);

	@SqlQuery("SELECT DOWNLOADABLE.*, MUSICALBUM.* FROM MUSICALBUM INNER JOIN DOWNLOADABLE ON MUSICALBUM.ID = DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS='DOWNLOADED' AND UPPER(ARTIST_NAME) LIKE :artistsSearchFilter ORDER BY NAME LIMIT :limit OFFSET :start")
	@Mapper(MusicAlbumMapper.class)
	List<MusicAlbum> getDownloadedAlbums(@BindContains("artistsSearchFilter") String artistsSearchFilter, @Bind("start") int start, @Bind("limit") int limit);

	@SqlQuery("SELECT COUNT(*) FROM MUSICALBUM INNER JOIN DOWNLOADABLE ON MUSICALBUM.ID = DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS='DOWNLOADED' AND UPPER(ARTIST_NAME) LIKE :artistsSearchFilter")
	int getDownloadedAlbumsCount(@BindContains("artistsSearchFilter") String artistsSearchFilter);

	@SqlQuery("SELECT COUNT(*) FROM MUSICFILE INNER JOIN MUSICALBUM ON MUSICALBUM.ID = MUSICFILE.ALBUM_ID INNER JOIN DOWNLOADABLE ON MUSICALBUM.ID = DOWNLOADABLE.ID WHERE UPPER(ARTIST_NAME) LIKE :artistsSearchFilter AND UPPER(NAME) LIKE :albumNameFilter")
	int getMusicFilesCount(@BindContains("artistsSearchFilter") String artistsSearchFilter, @BindContains("albumNameFilter") String albumNameFilter );

	@SqlQuery("SELECT COUNT(*) FROM MUSICFILE WHERE MUSICFILE.ALBUM_ID = :albumId")
	int getMusicFilesCount(@Bind("albumId") long albumId );

	@SqlQuery("SELECT MUSICFILE.* FROM MUSICFILE INNER JOIN MUSICALBUM ON MUSICALBUM.ID = MUSICFILE.ALBUM_ID INNER JOIN DOWNLOADABLE ON MUSICALBUM.ID = DOWNLOADABLE.ID WHERE UPPER(ARTIST_NAME) LIKE :artistsSearchFilter AND UPPER(NAME) LIKE :albumNameFilter ORDER BY NAME LIMIT :limit OFFSET :start")
	@Mapper(MusicFileMapper.class)
	List<MusicFile> getMusicFiles(@BindContains("artistsSearchFilter") String artistsSearchFilter, @BindContains("albumNameFilter") String albumNameFilter, @Bind("start") int start, @Bind("limit") int limit );

	@SqlQuery("SELECT DOWNLOADABLE.*, MUSICALBUM.* FROM MUSICALBUM INNER JOIN DOWNLOADABLE ON MUSICALBUM.ID = DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS='DOWNLOADED' AND DOWNLOADABLE.COVER_IMAGE IS NOT NULL")
	@Mapper(MusicAlbumMapper.class)
	List<MusicAlbum> findAlbumsWithImage();

}
