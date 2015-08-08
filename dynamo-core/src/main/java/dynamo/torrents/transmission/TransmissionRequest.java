package dynamo.torrents.transmission;

public class TransmissionRequest {
	
	private TransmissionRequestArguments arguments;
	private String method;
	private int tag;
	
	public TransmissionRequest( String method ) {
		this.method = method;
	}

	public TransmissionRequestArguments getArguments() {
		if (arguments == null) {
			arguments = new TransmissionRequestArguments();
		}
		return arguments;
	}
	public void setArguments(TransmissionRequestArguments arguments) {
		this.arguments = arguments;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public int getTag() {
		return tag;
	}
	public void setTag(int tag) {
		this.tag = tag;
	}
	


}
