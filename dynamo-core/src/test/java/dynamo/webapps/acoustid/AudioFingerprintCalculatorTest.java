package dynamo.webapps.acoustid;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.Test;

public class AudioFingerprintCalculatorTest {

	@Test
	public void testCalculate() throws IOException {
		
		AcoustIDFingerprintCalculator fingerprinter = new AcoustIDFingerprintCalculator( Paths.get("d:\\apps\\fpcalc.exe"));
		AcoustIDFingerprint fingerprint = fingerprinter.calculate( Paths.get("\\\\DLINK-4T\\Volume_2\\music-albums\\Foo Fighters\\Greatest Hits\\01 - All My Life.mp3"));
		
		System.out.println( fingerprint.getFingerprint() );
	}

}
