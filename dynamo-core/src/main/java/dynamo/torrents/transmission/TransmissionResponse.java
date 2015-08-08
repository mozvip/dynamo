package dynamo.torrents.transmission;


public class TransmissionResponse {

	private String result;
	private String tag;
	private TransmissionResponseArguments arguments;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public TransmissionResponseArguments getArguments() {
		return arguments;
	}

	public void setArguments(TransmissionResponseArguments arguments) {
		this.arguments = arguments;
	}

}
