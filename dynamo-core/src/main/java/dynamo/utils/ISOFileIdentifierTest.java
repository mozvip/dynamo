package dynamo.utils;

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;

import org.junit.Test;

import dynamo.model.ISOType;

public class ISOFileIdentifierTest {

	@Test
	public void testIdentify() {
		assertEquals( ISOType.GAMECUBE, ISOFileIdentifier.identify( Paths.get("E:/emu/Dolphin-x64/roms/NGC Geist/aln-ge.iso")));
	}

}
