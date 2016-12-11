package dynamo.webapps.acoustid;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.junit.Test;

public class AcoustIDClientTest {

	@Test
	public void test() throws IOException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException {
		
		Path audioFilePath = Paths.get("\\\\DLINK-4T\\Volume_2\\music-albums\\Foo Fighters\\Greatest Hits\\01 - All My Life.mp3");
		
		AudioFile audioFile = AudioFileIO.getDefaultAudioFileIO().readFile( audioFilePath.toFile() );
		
		String fingerprint = audioFile.getTag().getFirst( FieldKey.ACOUSTID_FINGERPRINT );
		if ( fingerprint == null ) {
			AcoustIDFingerprintCalculator fingerprinter = new AcoustIDFingerprintCalculator( Paths.get("d:\\apps\\fpcalc.exe"));
			fingerprint = fingerprinter.calculate( audioFilePath ).getFingerprint();
		}
		
		int duration = audioFile.getAudioHeader().getTrackLength();

		String albumArtist = audioFile.getTag().getFirst( FieldKey.ALBUM_ARTIST );

		AcoustIDClient client = new AcoustIDClient( "iZWMfBHr" );
		AcoustIdLookupResults results = client.lookupReleases( duration, fingerprint );
		
		results.getResults().get(0).getReleases().stream().filter( r -> r.getArtists().get(0).getName().equals( albumArtist ) ).forEach( r -> System.out.println(r.getTitle() + " " + r.getTrack_count()) );
	}

}
