package dynamo.utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dynamo.model.ISOType;
import net.didion.loopy.AbstractBlockFileSystem;
import net.didion.loopy.FileEntry;
import net.didion.loopy.iso9660.ISO9660FileSystem;
import net.didion.loopy.udf.UDFFileSystem;

public class ISOFileIdentifier {
	
	private final static Logger LOGGER = LoggerFactory.getLogger( ISOFileIdentifier.class );
	
	public static ISOType identify(Path p) {
		
		LOGGER.info(String.format("Parsing iso file %s", p.toAbsolutePath().toString()));
		
		ISOType isoType = ISOType.UNKNOWN;
		
		AbstractBlockFileSystem fs = null;
		try {
			try {
				fs = new ISO9660FileSystem(p.toFile(), true);
			} catch (IOException e) {
				try {
					fs = new UDFFileSystem(p.toFile(), true);
				} catch (IOException eUDF) {
				}
			}
			
			if ( fs != null ) {
				try {
					Enumeration<FileEntry> eEntries = fs.getEntries();
					while (eEntries.hasMoreElements()) {
						FileEntry entry = eEntries.nextElement();
						if (entry.getName().equals("setup.exe") || entry.getName().equals("autorun.inf")) {
							// we found a PC iso
							isoType = ISOType.PC;
							break;
						}
					}
				} catch (RuntimeException e) {
					// Loopy API can do this in this situation :(

					try(RandomAccessFile raf = new RandomAccessFile(p.toFile(), "r")) {

						int intValue = raf.readInt();
						String hexValue = Integer.toHexString( intValue );
						if (hexValue.equals("57424353")) {	// WBFS
							return ISOType.WII;
						}

						raf.seek(0x18);
						intValue = raf.readInt();
						hexValue = Integer.toHexString( intValue );
						if (hexValue.equals("5d1c9ea3")) {
							return ISOType.WII;
						}

						raf.seek(0x1C);
						intValue = raf.readInt();
						hexValue = Integer.toHexString( intValue );
						if (hexValue.equals("c2339f3d")) {
							return ISOType.GAMECUBE;
						}

					} catch (IOException eRaf) {
					}
					
					// TODO
				}
			}
		} finally {
			if ( fs != null ) {
				try {
					fs.close();
				} catch (IOException e) {
				}
			}
		}
		
		return isoType;

	}

}
