package dynamo.torrents.transmission;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;
import retrofit.client.Header;

public class TransmissionErrorHandler implements ErrorHandler {
	
	private String xTransmissionSessionId;

	@Override
	public Throwable handleError(RetrofitError cause) {
		
		if (cause.getResponse() != null && cause.getResponse().getStatus() == 409) {
			for (Header header : cause.getResponse().getHeaders()) {
				if (header.getName() != null && header.getName().equalsIgnoreCase("X-Transmission-Session-Id")) {
					xTransmissionSessionId = header.getValue();
					break;
				}
			}
		} 
		return cause;
	}
	
	public String getxTransmissionSessionId() {
		return xTransmissionSessionId;
	}
	
	public void setxTransmissionSessionId(String xTransmissionSessionId) {
		this.xTransmissionSessionId = xTransmissionSessionId;
	}

}
