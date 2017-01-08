package com.github.dynamo.core;

import java.io.IOException;
import java.nio.file.Path;

public interface FolderIdentifier {
	
	public boolean is( Path dir ) throws IOException;
	public void onIdentify( Path dir );

}
