package dynamo.webapps.acoustid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

import core.RegExp;

public class AcoustIDFingerprintCalculator {
	
	private Path pathToFpCalc;
	
	public AcoustIDFingerprintCalculator( Path pathToFpCalc ) {
		this.pathToFpCalc = pathToFpCalc;
	}
	
	public AcoustIDFingerprint calculate( Path musicFile ) throws IOException {
		if (!Files.isExecutable( pathToFpCalc)) {
			throw new IOException( String.format("%s is not executable", pathToFpCalc.toString()));
		}
		
		if (!Files.isReadable( musicFile )) {
			throw new IOException( String.format("%s is not readable", musicFile.toString()));
		}

		String fpcalcPath = pathToFpCalc.toAbsolutePath().normalize().toString();

		ProcessBuilder fpcalcPb = new ProcessBuilder(fpcalcPath, musicFile.toAbsolutePath().normalize().toString());
		fpcalcPb.directory( musicFile.getParent().toFile() );
		Process p = fpcalcPb.start();

		AcoustIDFingerprint fingerprint = new AcoustIDFingerprint();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				
				String file = RegExp.extract(line, "FILE=(.+)");
				if (file != null) {
					fingerprint.setFile(file);
				} else {
					String durationStr = RegExp.extract(line, "DURATION=(\\d+)");
					if (durationStr != null) {
						fingerprint.setDuration( Integer.parseInt(durationStr) );
					} else {
						fingerprint.setFingerprint( RegExp.extract(line, "FINGERPRINT=(.+)") );
					}
				}
			}
		}

		return fingerprint;
	}

}
