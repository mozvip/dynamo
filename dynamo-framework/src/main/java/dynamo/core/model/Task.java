package dynamo.core.model;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class Task {

	private Date minDate;
	
	public Date getMinDate() {
		return minDate;
	}

	public void setMinDate(Date minDate) {
		this.minDate = minDate;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString( this );
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.toString().equals( toString() );
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

}
