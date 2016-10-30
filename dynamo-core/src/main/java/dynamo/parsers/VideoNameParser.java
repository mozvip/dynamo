package dynamo.parsers;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;

import core.RegExp;
import dynamo.core.Language;
import dynamo.core.VideoQuality;
import dynamo.tvshows.model.ManagedSeries;

public class VideoNameParser {

	private final static String NAME_REGEXP = "[é!&,'\\[\\]\\w\\s\\.\\d-:½]+";
	public final static String SEPARATOR_REGEXP = "[\\s\\.]+";
	
	private static String[] filters = new String[] {
			"(.*)" + SEPARATOR_REGEXP + "READNFO" + SEPARATOR_REGEXP + "(.*)",
			"(.*)" + SEPARATOR_REGEXP + "FANSUB" + SEPARATOR_REGEXP + "(.*)",
			"(.*)" + SEPARATOR_REGEXP + "UNRATED" + SEPARATOR_REGEXP + "(.*)",
			"(.*)" + SEPARATOR_REGEXP + "FASTSUB" + SEPARATOR_REGEXP + "(.*)",
			"(.*)" + SEPARATOR_REGEXP + "REPACK" + SEPARATOR_REGEXP + "(.*)",
			"(.*)" + SEPARATOR_REGEXP + "Theatrical\\s+Cut" + SEPARATOR_REGEXP + "(.*)"
	};
	
	public static String clean( String title, String[] filtersRegExps ) {
		
		title = title.replace('_', ' ');
		title = title.replaceAll("\\s+", " ");

		boolean mustContinue = true;
		while (mustContinue) {
			mustContinue = false;
			for (String filter : filtersRegExps) {
				String[] groups = RegExp.parseGroups( title,  filter );
				if (groups != null) {
					title = StringUtils.join( groups, " ");
					mustContinue = true;
				}
			}
		}
		
		for (Language language : Language.values()) {
			if (language.getSubTokens() != null ) {
				for (String subToken : language.getSubTokens()) {
					String[] groups = RegExp.parseGroups( title,  "(.*)" + SEPARATOR_REGEXP + subToken + SEPARATOR_REGEXP + "(.*)" );
					if (groups != null) {
						title = StringUtils.join( groups, " ");
					}
				}
			}
		}
	
		return title;
	}
	
	protected static VideoInfo getVideoInfo( String title ) {

		title = clean( title, filters );

		String[] groups = RegExp.parseGroups(title, "(" + NAME_REGEXP + ")" + SEPARATOR_REGEXP + "(MULTI VFF 720p BluRay x264 AC3).*");
		if (groups != null) {
			String name = getName( groups[0] );
			return new ParsedMovieInfo( name, -1, groups[1] );
		}

		groups = RegExp.parseGroups(title, "(" + NAME_REGEXP + ")" + SEPARATOR_REGEXP + "(S\\d{2}E\\d{2}E\\d{2})(.*)");
		if (groups != null) {
			String name = getName( groups[0] ); 
			String seasonEpisode = groups[1];
			
			String[] episodeDetails = RegExp.parseGroups( seasonEpisode, "S(\\d{2})E(\\d{2})E(\\d{2})");
			return new TVShowEpisodeInfo( name, Integer.parseInt( episodeDetails[0]), Integer.parseInt( episodeDetails[1]), Integer.parseInt( episodeDetails[2]), groups[2] );
		} 

		groups = RegExp.parseGroups(title, "(" + NAME_REGEXP + ")" + SEPARATOR_REGEXP + "(S\\d{2}E\\d{2})(.*)");
		if (groups != null) {
			String name = getName( groups[0] ); 
			String seasonEpisode = groups[1];

			String[] episodeDetails = RegExp.parseGroups( seasonEpisode, "S(\\d{2})E(\\d{2})");
			return new TVShowEpisodeInfo( name, Integer.parseInt( episodeDetails[0]), Integer.parseInt( episodeDetails[1]), groups[2] );
		}
		
		groups = RegExp.parseGroups(title, "(" + NAME_REGEXP + ")" + SEPARATOR_REGEXP + "(\\d{1}X\\d{2})(.*)");
		if (groups != null) {
			String name = getName( groups[0] ); 
			String seasonEpisode = groups[1];
			
			String[] episodeDetails = RegExp.parseGroups( seasonEpisode, "(\\d{1})X(\\d{2})");
			return new TVShowEpisodeInfo( name, Integer.parseInt( episodeDetails[0]), Integer.parseInt( episodeDetails[1]), groups[2] );
		} 
		
		groups = RegExp.parseGroups(title, "(" + NAME_REGEXP + ")" + SEPARATOR_REGEXP + "S(\\d{1,2})\\s+-\\s+(\\d{1,2})\\s+(.*)");
		if (groups != null) {
			String name = getName( groups[0] ); 
			return new TVShowEpisodeInfo( name, Integer.parseInt( groups[1]), Integer.parseInt( groups[2]), groups[3] );
		} 

		groups = RegExp.parseGroups(title, "(" + NAME_REGEXP + ")" + SEPARATOR_REGEXP + "(\\d{1})(\\d{2})\\.(.*)");
		if (groups != null) {
			String name = getName( groups[0] );
			return new TVShowEpisodeInfo( name, Integer.parseInt( groups[1]), Integer.parseInt( groups[2]), groups[3] );
		}

		groups = RegExp.parseGroups(title, "(" + NAME_REGEXP + ")" + SEPARATOR_REGEXP + "\\((\\d{4})\\)(.*)");
		if (groups != null) {
			String name = getName( groups[0] );
			return new ParsedMovieInfo( name, Integer.parseInt( groups[1]), groups[2] );
		}

		groups = RegExp.parseGroups(title, "(" + NAME_REGEXP + ")" + SEPARATOR_REGEXP + "(\\d{4}[\\.\\s]{1})(.*)");
		if (groups != null) {
			String name = getName( groups[0] );
			return new ParsedMovieInfo( name, Integer.parseInt( groups[1].substring(0,  4)), groups[2] );
		}

		groups = RegExp.parseGroups(title, "(" + NAME_REGEXP + ")" + SEPARATOR_REGEXP + "(\\d{3})\\D{1}(.*)");
		if (groups != null) {
			String name = getName( groups[0] ); 
			String seasonEpisode = groups[1];

			String[] episodeDetails = RegExp.parseGroups( seasonEpisode, "(\\d{1})(\\d{2})");
			return new TVShowEpisodeInfo( name, Integer.parseInt( episodeDetails[0]), Integer.parseInt( episodeDetails[1]), groups[2] );
		}
		
		groups = RegExp.parseGroups(title, "(" + NAME_REGEXP + ")" + SEPARATOR_REGEXP + "(.*)" + SEPARATOR_REGEXP + "(\\d{4})$");
		if (groups != null) {
			String name = getName( groups[0] );
			return new ParsedMovieInfo( name, Integer.parseInt( groups[2]), groups[1] );
		}		

		return null;

	}
	
	public static TVShowEpisodeInfo getTVShowEpisodeInfo( ManagedSeries series, Path path ) {
		
		// this method is called when we know that the file is a TV Show episode for a know show, in this case, we can use specific regexps to only retrieve season & episode info
		
		String name = path.getFileName().toString();
		
		name = RegExp.filter(name, "(.*)\\.\\w+");		// removes extension
		
		String[] regExps;
		
		if (series.isUseAbsoluteNumbering()) {
			
			regExps = new String[] {
					".*Ep(\\d{2})(.*)",
					".*Ep\\.(\\d{2})(.*)"
			};
			
		} else {

			regExps = new String[] {
					".*S(\\d{2})E(\\d{2})-?E?(\\d{2})(.*)",
					".*S(\\d{2})E(\\d{2})-S\\d{2}E(\\d{2})(.*)",
					".*S(\\d{2})\\s+-\\s+(\\d{2})(.*)",
					".*S(\\d{2})\\.E(\\d{2})(.*)",
					".*S(\\d{2})E(\\d{2})(.*)",
					".*(\\d{1,2})X(\\d{2})(.*)",
					".*\\.(\\d{2})(\\d{2})(.*)",
					".*[^x](\\d{1})(\\d{2})[^\\dp](.*)",
					".*Ep(\\d{2})(.*)",
					".*Ep\\.(\\d{2})(.*)"
			};
			
		}

		for (String regexp : regExps) {
			String[] groups = RegExp.parseGroups( name, regexp );
			if (groups != null) {
				
				if (series.isUseAbsoluteNumbering()) {
					
					return new TVShowEpisodeInfo( series.getName(), 1, Integer.parseInt( groups[0]), groups[1] );
					
				} else {
				
					if (groups.length > 3) {
						// multiple episodes
						return new TVShowEpisodeInfo( series.getName(), Integer.parseInt( groups[0]), Integer.parseInt( groups[1]), Integer.parseInt( groups[2]), groups[3] );
					} else if (groups.length > 2) {
						return new TVShowEpisodeInfo( series.getName(), Integer.parseInt( groups[0]), Integer.parseInt( groups[1]), groups[2] );
					} else {
						return new TVShowEpisodeInfo( series.getName(), 1, Integer.parseInt( groups[0]), groups[1] );
					}
				}
			}
		}

		// logger.warn( String.format("VideoNameParser was unable to parse %s", name ));

		return null;
	}
	
	public static VideoInfo getVideoInfo( Path path ) {
		String fileNameWithoutExtension = path.getFileName().toString();
		if (Files.isRegularFile( path )) {
			fileNameWithoutExtension = fileNameWithoutExtension.substring(0, fileNameWithoutExtension.lastIndexOf('.'));			
		}
		VideoInfo info = getVideoInfo( fileNameWithoutExtension );
		if (info == null && Files.isRegularFile( path ) && path.getParent() != null) {
			// sometimes the parent folder is correctly named
			Path parent = path.getParent();
			if (parent.getFileName() != null) { // I don't understand why this happens 
				info = getVideoInfo( parent.getFileName().toString() );
			}
		}

		if ( info != null && info.getName() != null ) {
			info.setName( info.getName().trim() );
		}
		
		return info;
	}

	private static String getName(String string) {
		String name = string;
		name = name.replace('.', ' ');
		name = name.replace('_', ' ');
		
		name = RegExp.keepOnlyGroups(name, "(.*)" + SEPARATOR_REGEXP + "[Bb][Ll][Uu][Rr][Aa][Yy](.*)");
		name = RegExp.keepOnlyGroups(name, "(.*)" + SEPARATOR_REGEXP + "[Ff][Rr]E[Nn][Cc][Hh](.*)");
		name = RegExp.keepOnlyGroups(name, "(.*)" + SEPARATOR_REGEXP + "X264(.*)");
		return name;
	}

	public static VideoQuality getQuality( String title ) {
		VideoQuality quality = VideoQuality.findMatch( title );
		return quality != null ? quality : VideoQuality.SD;
	}
	
	public static ParsedMovieInfo getMovieInfo( String string ) {

		string = clean( string, filters );
		// this method is called when we know that the file is a Movie file
		VideoInfo info = getVideoInfo( string );
		if (info instanceof ParsedMovieInfo) {
			return (ParsedMovieInfo) info;
		}
		return null;

	}	

	public static ParsedMovieInfo getMovieInfo(Path path) {
		
		// this method is called when we know that the file is a Movie file

		VideoInfo info = getVideoInfo(path);
		if (info instanceof ParsedMovieInfo) {
			return (ParsedMovieInfo) info;
		}

		String fileNameWithoutExtension = path.getFileName().toString();
		if (Files.isRegularFile( path )) {
			fileNameWithoutExtension = fileNameWithoutExtension.substring(0, fileNameWithoutExtension.lastIndexOf('.'));			
		}
		String title = clean( fileNameWithoutExtension, filters );
		return new ParsedMovieInfo( title, null );
	}

}
