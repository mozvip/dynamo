package com.github.dynamo.model.music;

import com.github.dynamo.core.Labelized;

public enum MusicQuality implements Labelized {
	
	COMPRESSED("Compressed"),
	LOSSLESS("Lossless");
	
	private String label;
	
	private MusicQuality( String label ) {
		this.label = label;
	}
	
	@Override
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}

}
