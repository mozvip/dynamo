package dynamo.backlog.tasks.music;

import java.util.concurrent.Semaphore;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.reference.ID3V2Version;

import dynamo.core.model.TaskExecutor;
import dynamo.webapps.acoustid.AcoustID;
import dynamo.webapps.acoustid.AcoustIdLookupResults;
import dynamo.webapps.acoustid.AcoustIDFingerprint;

public class CalcAcoustIdExecutor extends TaskExecutor<CalcAcoustIdTask> {
	
	private static Semaphore acoustIdSemaphore = new Semaphore(1);

	public CalcAcoustIdExecutor(CalcAcoustIdTask task) {
		super(task);
	}
	
	@Override
	public void init() throws Exception {
		acoustIdSemaphore.acquire();
	}
	
	@Override
	public void shutdown() throws Exception {
		acoustIdSemaphore.release();
	}


	@Override
	public void execute() throws Exception {
		
		AcoustIDFingerprint fingerprint = AcoustID.getInstance().fingerprint( task.getMusicFilePath() );

		if (fingerprint.getDuration() > 0 && fingerprint.getFingerprint() != null) {

			AudioFile audioFile = AudioFileIO.read(task.getMusicFilePath().toFile());
			Tag audioTag = audioFile.getTagOrCreateDefault();
			if (audioTag instanceof ID3v1Tag) {
				audioTag = ((MP3File) audioFile).convertTag(audioTag, ID3V2Version.ID3_V24);
			}
			audioTag.setField(FieldKey.ACOUSTID_FINGERPRINT, fingerprint.getFingerprint());
			
			// TODO: store duration somewhere too, in MusicFile ?

			AcoustIdLookupResults results = AcoustID.getInstance().lookup( fingerprint.getDuration(), fingerprint.getFingerprint() );
			AcoustID.getInstance().populateTag(results, audioTag, false);
			audioFile.setTag( audioTag );
			audioFile.commit();
		}

	}

}
