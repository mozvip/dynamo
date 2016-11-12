package dynamo.backlog.tasks.music;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.Semaphore;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.reference.ID3V2Version;

import core.RegExp;
import dynamo.core.model.TaskExecutor;
import dynamo.webapps.acoustid.AcoustId;
import dynamo.webapps.acoustid.AcoustIdLookupResults;

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
		
		String fpcalcPath = AcoustId.getInstance().getFpcalcPath().toAbsolutePath().toString();

		ProcessBuilder fpcalcPb = new ProcessBuilder(fpcalcPath, task.getMusicFilePath().toAbsolutePath().normalize().toString());
		fpcalcPb.directory(new File("."));
		Process p = fpcalcPb.start();

		int duration = 0;
		String fingerprint = null;

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {

				String durationStr = RegExp.extract(line, "DURATION=(\\d+)");
				if (durationStr != null) {
					duration = Integer.parseInt(durationStr);
				} else {
					fingerprint = RegExp.extract(line, "FINGERPRINT=(.+)");
				}

			}
		}

		if (duration > 0 && fingerprint != null) {

			AudioFile audioFile = AudioFileIO.read(task.getMusicFilePath().toFile());
			Tag audioTag = audioFile.getTagOrCreateDefault();
			if (audioTag instanceof ID3v1Tag) {
				audioTag = ((MP3File) audioFile).convertTag(audioTag, ID3V2Version.ID3_V24);
			}
			audioTag.setField(FieldKey.ACOUSTID_FINGERPRINT, fingerprint);
			
			// TODO: store duration somewhere too, in MusicFile ?

			AcoustIdLookupResults results = AcoustId.getInstance().lookup(duration, fingerprint);
			AcoustId.getInstance().populateTag(results, audioTag, false);
			audioFile.setTag( audioTag );
			audioFile.commit();
		}

	}

}
