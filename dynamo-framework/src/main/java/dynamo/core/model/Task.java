package dynamo.core.model;

import java.time.LocalDateTime;

import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class Task {

	private LocalDateTime minDate;
	
	public LocalDateTime getMinDate() {
		return minDate;
	}

	public void setMinDate(LocalDateTime minDate) {
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
