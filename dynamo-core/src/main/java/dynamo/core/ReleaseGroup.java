package dynamo.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ReleaseGroup {
		
	_0TV( new String[] {"0tv"} ),
	_2HD( new String[] {"2hd"} ),
	_7O9( new String[] {"7o9"} ),
	AAF(new String[] {"aaf"}),
	acROBATT_MBE(new String[]{"acROBATT&MBE"}),
	ADDiCTiON(new String[]{"ADDiCTiON"}),
	ASAP( new String[] {"ASAP", "asa"}),
	AVCHD( new String[] {"avchd"} ),
	AVS(new String[] {"AVS"}),
	BIA( new String[] {"bia", "-bia.", ".bia."} ),
	BWB( new String[] {"BWB"}),
	BTN( new String[] {"BTN"}),
	Chotab(new String[] {"Chotab"}),
	CLUE( new String[] {"CLUE"}),
	COMPULSION( new String[] {"compulsion"}),
	EbP( new String[] {"EbP"}),
	EVOLVE( new String[] {"EVOLVE"}),
	DiViSiON( new String[] {"DiViSiON"}),
	CTU( new String[] {"ctu", "720p-CTU", ".ctu.", "-ctu."} ),
	ECI( new String[] {"ECI"}),
	GFY(new String[] {"GFY"}),
	HDxT(new String[] {"HDxT", "hdxt"}),
	HANNIBAL(new String[] {"hannibal"}),
	HYPE(new String[] {"Hype"}),
	KiNGS(new String[] {"KiNGS"}),
	KILLERS(new String[] {"KILLERS"}),
	LOL_DIMENSION( new String[] {"dimension", ".dim.", "-DIMENSION.", "-DIM.", ".DiMENSiON.", ".lol.", "LOL", "SYS"} ),
	MAGiCDRAGON(new String[] {"MAGiCDRAGON"}),
	NTB(new String[] {"NTb"}),
	ORENJI( new String[] { "orenji", "ore" }),
	PiLAF(new String[] {"PiLAF"}),
	FEVER( new String[] {"FEVER"} ),
	FOV( new String[] {"fov", "DVDRip.XviD-FoV" } ),
	FQM( new String[] {"FQM"} ),
	HAGGIS( new String[] {".haggis.", "-haggis."} ),
	XOR( new String[] {".xor."} ),
	IMMERSE( new String[] {"immerse", ".IMM."} ),
	CTRLHD( new String[] {"ctrlhd", "CtrlHD", "ctrl"} ),
	NFHD(new String[] {"NFHD"} ),
	TLA(new String[] {"tla"} ),
	SINNERS( new String[] {"sinners"} ),
	NOTV( new String[] {".notv.", "-notv.", "NoTV"} ),
	PDTV( new String[] {"pdtv"} ),
	P0W4( new String[] {"P0W4"} ),
	SAiNTS( new String[] {"-SAiNTS.", "SAiNTS"} ),
	SAPHIRE( new String[] {"SAPHiRE"}),
	SITV( new String[] {"sitv"} ),
	RANDi(new String[] {"RANDi"}),
	RED(new String[] {"RED"}),
	RTV( new String[] {"rtv"} ),
	REWARD(new String[] {"reward"} ),
	RIVER(new String[] {"river"} ),
	TNS(new String[] {"tns"}),
	TOPAZ(new String[] {"TOPAZ", "tpz-"} ),
	ViPER(new String[] {"ViPER"}),
	YESTV( new String[] {"yestv"} ),
	YOOX(new String[] {"YOOX"} ),
	UNKNOWN(null);
	
	private final static Logger logger = LoggerFactory
			.getLogger(ReleaseGroup.class);
	
	
	private String[] aliases;
	
	private ReleaseGroup( String[] aliases ) {
		this.aliases = aliases;
	}
	
	public String[] getAliases() {
		return aliases;
	}
	
	public boolean match( String filename ) {
		if (aliases != null) {
			for (String alias : aliases) {
				if (StringUtils.containsIgnoreCase(filename, alias)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static ReleaseGroup firstMatch( String name ) {
		for (ReleaseGroup release : ReleaseGroup.values()) {
			if (release.match(name)) {
				return release;
			}
		}
		return UNKNOWN;
	}

	public static Collection<ReleaseGroup> allMatches(String name) {
		List<ReleaseGroup> groups = new ArrayList<ReleaseGroup>();
		for (ReleaseGroup release : ReleaseGroup.values()) {
			if (release.match(name)) {
				groups.add(release);
			}
		}
		return groups;
	}

}
