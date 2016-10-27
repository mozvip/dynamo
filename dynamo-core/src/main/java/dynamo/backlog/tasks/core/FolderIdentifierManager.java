package dynamo.backlog.tasks.core;

import java.util.Set;

import dynamo.core.FolderIdentifier;
import dynamo.core.manager.DynamoObjectFactory;

public class FolderIdentifierManager {

	static class SingletonHolder {
		private SingletonHolder() {
		}

		static FolderIdentifierManager instance = new FolderIdentifierManager();
	}

	public static FolderIdentifierManager getInstance() {
		return SingletonHolder.instance;
	}
	
	private FolderIdentifierManager() {
	}

	private Set<FolderIdentifier> folderIdentifiers;
	
	public synchronized Set<FolderIdentifier> getFolderIdentifiers() {
		if (folderIdentifiers == null) {
			folderIdentifiers = (Set<FolderIdentifier>) DynamoObjectFactory.getInstances( FolderIdentifier.class );
		}
		return folderIdentifiers;
	}

}
