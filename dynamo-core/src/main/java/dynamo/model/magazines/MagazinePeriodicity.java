package dynamo.model.magazines;

public enum MagazinePeriodicity {
	
	DAILY("Daily"),
	WEEKLY("Weekly"),
	MONTHLY("Monthly"),
	OTHER("Other");
	
	private String label;
	
	private MagazinePeriodicity( String label ) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

}
