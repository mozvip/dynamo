package dynamo.model;

import dynamo.games.model.GamePlatform;

public enum ISOType {
	
	UNKNOWN, PC, DVD_VIDEO, GAMECUBE(GamePlatform.NINTENDO_GAMECUBE), WII(GamePlatform.NINTENDO_WII), PSP(GamePlatform.PSP), PS1(GamePlatform.PS1), PS2(GamePlatform.PS2);
	
	private GamePlatform gamePlatform = null;
	
	private ISOType() {
	}
	
	private ISOType( GamePlatform platform ) {
		this.gamePlatform = platform;
	}
	
	public GamePlatform getGamePlatform() {
		return gamePlatform;
	}
	
	public void setGamePlatform(GamePlatform gamePlatform) {
		this.gamePlatform = gamePlatform;
	}

}
