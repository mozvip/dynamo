package dynamo.subtitles;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;


public class SubTitlesUtils {
	
	public static List<String> getMatchList( int season, int episode ) {
		
		NumberFormat nf = NumberFormat.getIntegerInstance();
		nf.setMinimumIntegerDigits(2);
		
		List<String> allMatches = new ArrayList<String>();
		allMatches.add( "" + season + "×" + nf.format( episode ) );
		allMatches.add( "" + season + "x" + nf.format( episode ) );
		allMatches.add( "" +  nf.format( season ) + "x" + nf.format( episode ) );
		allMatches.add( ".S" + season + "." );
		allMatches.add( ".S0" + season + "." );
		allMatches.add( "S" + season );
		
		return allMatches;
	}
	
	public static boolean isMatch( String fileName, int season, int episode ) {
		
		List<String> matches = getMatchList( season, episode );
		
		for (String string : matches) {
			if (fileName.contains( string )) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isExactMatch( String fileName, int season, int episode ) {
		
		List<String> matches = getMatchList( season, episode );

		for (String string : matches) {
			if (fileName.equals( string )) {
				return true;
			}
		}
		
		return false;
	}

}
