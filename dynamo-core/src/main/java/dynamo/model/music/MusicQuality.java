package dynamo.model.music;

public enum MusicQuality {
	
	COMPRESSED("Compressed"),
	LOSSLESS("Lossless");
	
	private String label;
	
	private MusicQuality( String label ) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}

}
