package dynamo.games.model;

import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Path;

import dynamo.core.ExtensionsFileFilter;

public enum GamePlatform {
	
	UNKNOWN("Unknown", null),
	PC("PC", null),
	PS1("Sony Playstation", new ExtensionsFileFilter(".cue", ".ccd", ".img", ".sub", ".bin", ".iso"), 1.0f, 3000),
	PS2("Sony Playstation 2", new ExtensionsFileFilter(".cso", ".iso", ".gz", ".nrg", ".gi", ".mdf"), 0.70f, 10000),
	PS3("Sony Playstation 3", null, 0.86f, 40000),
	PSP("Sony PSP", new ExtensionsFileFilter(".iso", ".cso"), 0.582f, 3000),
	XBOX360("Microsoft Xbox 360", new ExtensionsFileFilter(".iso")),
	ANDROID("Android", new ExtensionsFileFilter(".apk")),
	NINTENDO_DS("Nintendo DS", new ExtensionsFileFilter(".nds")),
	NINTENDO_3DS("Nintendo 3DS", new ExtensionsFileFilter(".3ds")),
	NINTENDO_GAMECUBE("Nintendo GameCube", new ExtensionsFileFilter(".gcz", ".wbfs", ".iso"), 0.70f, 6000),
	NINTENDO_WII("Nintendo WII", new ExtensionsFileFilter(".gcz", ".wbfs", ".iso"), 0.70f, 10000);

	private String label;
	private float coverImageRatio = 1.0f;
	private Filter<Path> fileFilter = null;
	private int maxSizeInMbs = 0;	// helps search engine top avoid compilations or obviously wrong results
	
	private GamePlatform( String label, Filter<Path> filter ) {
		this.label = label;
		this.fileFilter = filter;
		this.coverImageRatio = 1.0f;
	}

	private GamePlatform( String label, Filter<Path> filter, float coverImageRatio, int maxSizeInMbs ) {
		this.label = label;
		this.fileFilter = filter;
		this.coverImageRatio = coverImageRatio;
		this.maxSizeInMbs = maxSizeInMbs;
	}
	
	public String getLabel() {
		return label;
	}
	
	public float getCoverImageRatio() {
		return coverImageRatio;
	}

	public Filter<Path> getFileFilter() {
		return fileFilter;
	}
	
	public int getMaxSizeInMbs() {
		return maxSizeInMbs;
	}
	
	public static GamePlatform match( String label ) {
		for (GamePlatform platform : values()) {
			if (platform.getLabel().equalsIgnoreCase( label )) {
				return platform;
			}
		}
		return null;
	}

}
