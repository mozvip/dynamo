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

	@SqlQuery("SELECT DOWNLOADABLE_FILE.*, MUSIC_FILE.* FROM DOWNLOADABLE_FILE INNER JOIN MUSIC_FILE ON DOWNLOADABLE_FILE.FILE_ID = MUSIC_FILE.FILE_ID WHERE TAGSMODIFIED=true")
	@Mapper(MusicFileMapper.class)
	List<MusicFile> findModifiedTags();

	@SqlQuery("SELECT DOWNLOADABLE_FILE.*, MUSIC_FILE.* FROM DOWNLOADABLE_FILE INNER JOIN MUSIC_FILE ON DOWNLOADABLE_FILE.FILE_ID = MUSIC_FILE.FILE_ID WHERE FILE_PATH=:path")
	@Mapper(MusicFileMapper.class)
	MusicFile findMusicFile(@BindPath("path") Path path);
	
	@SqlQuery("select * from MUSICALBUM INNER JOIN DOWNLOADABLE ON MUSICALBUM.ID = DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS = 'DOWNLOADED' AND NOT EXISTS( SELECT * FROM MUSIC_FILE WHERE MUSIC_FILE.ALBUM_ID = MUSICALBUM.ID)")
	List<MusicAlbum> findDeadAlbums();

	@SqlQuery("SELECT NAME FROM MUSICARTIST WHERE UPPER(NAME) LIKE :param ORDER BY NAME")
	List<String> suggestArtists( @Bind("param") String param );

	@SqlQuery("SELECT MUSICARTIST.* FROM MUSICARTIST WHERE UPPER(name) = :artistName")
	@Mapper(MusicArtistMapper.class)
	MusicArtist findArtist(@BindUpper("artistName") String artistName);

	@SqlUpdate("UPDATE MUSICARTIST SET BLACKLISTED=true WHERE UPPER(name) = :artistName")
	void blackList(@BindUpper("artistName") String artistName);
	
	@SqlUpdate("MERGE INTO MUSICALBUM(ID, ARTIST_NAME, TADB_ALBUM_ID, GENRE, QUALITY, SEARCHSTRING, FOLDER) VALUES(:albumId, :artistName, :tadbAlbumId, :genre, :quality, :searchString, :folder)")
	public void save(@Bind("albumId") long albumId, @Bind("artistName") String artistName, @Bind("tadbAlbumId") Long tadbAlbumId, @Bind("genre") String genre, @BindEnum("quality") MusicQuality quality, @Bind("searchString") String searchString, @BindPath("folder") Path folder);

	@SqlQuery("SELECT DOWNLOADABLE_FILE.*, MUSIC_FILE.* FROM DOWNLOADABLE_FILE INNER JOIN MUSIC_FILE ON DOWNLOADABLE_FILE.FILE_ID = MUSIC_FILE.FILE_ID WHERE DOWNLOADABLE_ID=:albumId ORDER BY FILE_INDEX")
	@Mapper(MusicFileMapper.class)
	List<MusicFile> findMusicFiles(@Bind("albumId") long albumId);

	@SqlQuery("SELECT DOWNLOADABLE_FILE.*, MUSIC_FILE.* FROM DOWNLOADABLE_FILE INNER JOIN MUSIC_FILE ON DOWNLOADABLE_FILE.FILE_ID = MUSIC_FILE.FILE_ID INNER JOIN MUSICALBUM ON DOWNLOADABLE_FILE.DOWNLOADABLE_ID=MUSICALBUM.ID WHERE MUSICALBUM.ARTIST_NAME=:artistName ORDER BY FILE_INDEX")
	@Mapper(MusicFileMapper.class)
	List<MusicFile> findMusicFiles(@Bind("artistName") String artistName);

	@SqlUpdate("MERGE INTO MUSIC_FILE(FILE_ID, SONGTITLE, SONGARTIST, YEAR, TAGSMODIFIED) VALUES(:fileId, :songTitle, :songArtist, :year, :tagsModified)")
	void createMusicFile(@Bind("fileId") long fileId, 
			@Bind("songTitle") String songTitle, @Bind("songArtist") String songArtist, @Bind("year") int year, @Bind("tagsModified") boolean tagsModified);

	@SqlUpdate("INSERT INTO MUSICARTIST(NAME, TADB_ARTIST_ID, BLACKLISTED, FAVORITE, AKA) VALUES(:name, :tadbArtistId, :blackListed, :favorite, :aka)")
	void createArtist(@Bind("name") String name, @Bind("tadbArtistId") Long tadbArtistId, @Bind("blackListed") boolean blackListed, @Bind("favorite") boolean favorite, @Bind("aka") String aka);

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

	@SqlUpdate("DELETE FROM MUSICARTIST WHERE NAME=:artistName")
	void deleteArtist(@Bind("artistName") String artistName);

	@SqlUpdate("UPDATE MUSIC_FILE SET SONGARTIST=:songArtist, SONGTITLE=:songTitle, YEAR=:year, TAGSMODIFIED=:tagsModified WHERE FILE_ID=:fileId")
	void updateMusicFile(@Bind("fileId") long fileId, @Bind("songArtist") String songArtist, @Bind("songTitle") String songTitle, @Bind("year") int year, @Bind("tagsModified") boolean tagsModified);

	@SqlUpdate("UPDATE MUSIC_FILE SET TAGSMODIFIED=:tagsModified WHERE FILE_ID=:fileId")
	void updateTagsModified(@Bind("fileId") long fileId, @Bind("tagsModified") boolean tagsModified);

	@SqlQuery("SELECT DOWNLOADABLE.*, MUSICALBUM.* FROM MUSICALBUM INNER JOIN DOWNLOADABLE ON DOWNLOADABLE.ID = MUSICALBUM.ID WHERE ARTIST_NAME = :artistName AND (DOWNLOADABLE.STATUS='DOWNLOADED' OR DOWNLOADABLE.STATUS='WANTED')")
	@Mapper(MusicAlbumMapper.class)
	List<MusicAlbum> findAllAlbumsToDisplayForArtist(@Bind("artistName") String artistName);

	@SqlQuery("SELECT DOWNLOADABLE_FILE.*, MUSIC_FILE.* FROM DOWNLOADABLE_FILE INNER JOIN MUSIC_FILE ON DOWNLOADABLE_FILE.FILE_ID = MUSIC_FILE.FILE_ID WHERE POSITION(:folder, FILE_PATH) = 1")
	@Mapper(MusicFileMapper.class)
	List<MusicFile> findFilesInFolder(@BindPath("folder") Path folder);

	@SqlQuery("SELECT DOWNLOADABLE.*, MUSICALBUM.* FROM MUSICALBUM INNER JOIN DOWNLOADABLE ON MUSICALBUM.ID = DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS='DOWNLOADED' AND UPPER(ARTIST_NAME) LIKE :artistsSearchFilter ORDER BY NAME LIMIT :limit OFFSET :start")
	@Mapper(MusicAlbumMapper.class)
	List<MusicAlbum> getDownloadedAlbums(@BindContains("artistsSearchFilter") String artistsSearchFilter, @Bind("start") int start, @Bind("limit") int limit);

	@SqlQuery("SELECT COUNT(*) FROM MUSICALBUM INNER JOIN DOWNLOADABLE ON MUSICALBUM.ID = DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS='DOWNLOADED' AND UPPER(ARTIST_NAME) LIKE :artistsSearchFilter")
	int getDownloadedAlbumsCount(@BindContains("artistsSearchFilter") String artistsSearchFilter);
	
	@SqlQuery("SELECT DOWNLOADABLE_FILE.*, MUSIC_FILE.* FROM DOWNLOADABLE_FILE INNER JOIN MUSIC_FILE ON DOWNLOADABLE_FILE.FILE_ID = MUSIC_FILE.FILE_ID WHERE DOWNLOADABLE_FILE.DOWNLOADABLE_ID = :downloadableId")
	@Mapper(MusicFileMapper.class)
	List<MusicFile> getMusicFiles(@Bind("downloadableId") long downloadableId );

	@SqlQuery("SELECT DOWNLOADABLE.*, MUSICALBUM.* FROM MUSICALBUM INNER JOIN DOWNLOADABLE ON MUSICALBUM.ID = DOWNLOADABLE.ID WHERE DOWNLOADABLE.STATUS='DOWNLOADED' AND DOWNLOADABLE.COVER_IMAGE IS NOT NULL")
	@Mapper(MusicAlbumMapper.class)
	List<MusicAlbum> findAlbumsWithImage();

}
