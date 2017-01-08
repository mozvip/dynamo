package com.github.dynamo.core.exceptions;

public class LoginFailedException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String website;
	private String login;
	
	public LoginFailedException( String website, String login ) {
		this.website = website;
		this.login = login;
	}
	
	public String getWebsite() {
		return website;
	}
	
	public String getLogin() {
		return login;
	}

}
