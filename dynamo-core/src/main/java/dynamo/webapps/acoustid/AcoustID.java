package dynamo.webapps.acoustid;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;

import dynamo.core.Enableable;
import dynamo.core.configuration.Configurable;
import dynamo.core.configuration.Reconfigurable;

public class AcoustID implements Enableable, Reconfigurable {
	
	@Configurable(folder=false, required=true)
	private Path fpcalcPath;

	// registed on https://acoustid.org/applications for Dynamo 0.0.1-SNAPSHOT
	private String apiKey = "iZWMfBHr";
	
	@Override
	public boolean isEnabled() {
		return fpcalcPath != null && Files.isExecutable( fpcalcPath );
	}
	
	public Path getFpcalcPath() {
		return fpcalcPath;
	}
	
	public void setFpcalcPath(Path fpcalcPath) {
		this.fpcalcPath = fpcalcPath;
	}
	
	private AcoustIDClient client;
	
	private AcoustID() {
		client = new AcoustIDClient( apiKey );
	}
	
	

	public AcoustIdLookupResults lookup(int duration, String fingerprint) {
		return client.lookup(duration, fingerprint);
	}

	public AcoustIdLookupResults lookup(String trackid) {
		return client.lookup(trackid);
	}



	static class SingletonHolder {
		static AcoustID instance = new AcoustID();
	}

	public static AcoustID getInstance() {
		return SingletonHolder.instance;
	}
	
	public AcoustIDFingerprint fingerprint( Path musicFile ) throws IOException {
		return calculator.calculate(musicFile);
	}
	
	private AcoustIDFingerprintCalculator calculator = null;
	
	@Override
	public void reconfigure() {
		calculator = new AcoustIDFingerprintCalculator( fpcalcPath );
	}
	
	
	public void populateTag( AcoustIdLookupResults results, Tag audioTag, boolean overwrite ) throws KeyNotFoundException, FieldDataInvalidException {
		if (results.getResults().size() > 0) {
			AcoustIdLookupResult bestResult = results.getResults().get(0);

			audioTag.setField(FieldKey.ACOUSTID_ID, bestResult.getId());

			if (bestResult.getRecordings() != null && bestResult.getRecordings().size() > 0) {
				Recording preferedRecording = bestResult.getRecordings().get(0);
				
				String songTitle = preferedRecording.getTitle();
				
				if (overwrite || StringUtils.isBlank( audioTag.getFirst( FieldKey.TITLE ))) {
					audioTag.setField(FieldKey.TITLE, songTitle);
				}

				List<Artist> artists = preferedRecording.getArtists();
				if (artists != null) {
					Artist artist = artists.get(0);

					if (overwrite || StringUtils.isBlank( audioTag.getFirst( FieldKey.MUSICBRAINZ_ARTISTID ))) {
						audioTag.setField(FieldKey.MUSICBRAINZ_ARTISTID, artist.getId());
					}
	
					if (overwrite || StringUtils.isBlank( audioTag.getFirst( FieldKey.ARTIST ))) {
						audioTag.setField(FieldKey.ARTIST, artist.getName());
					}
					if (overwrite || StringUtils.isBlank( audioTag.getFirst( FieldKey.ALBUM_ARTIST ))) {
						audioTag.setField(FieldKey.ALBUM_ARTIST, artist.getName());
					}
				}

				if (overwrite || StringUtils.isBlank( audioTag.getFirst( FieldKey.ALBUM ))) {
					String album = null;
					if (preferedRecording.getReleasegroups().size() > 0) {
						for (ReleaseGroup releaseGroup : preferedRecording.getReleasegroups()) {
							String releaseGroupId = releaseGroup.getId();
							album = releaseGroup.getTitle();
						}
					}
					if (StringUtils.isNotBlank( album )) {
						audioTag.setField(FieldKey.ALBUM, album);
					}
				}

			}
		}		
	}
}
