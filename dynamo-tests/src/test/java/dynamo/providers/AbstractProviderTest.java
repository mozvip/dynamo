package dynamo.providers;

import dynamo.core.Language;
import dynamo.tests.AbstractDynamoTest;
import model.ManagedSeries;

public abstract class AbstractProviderTest extends AbstractDynamoTest {
	
	public ManagedSeries createMockedSeries( String name, Language audioLanguage) {
		return new ManagedSeries(null, name, null, null, null, null, null, null, null, audioLanguage, null, false, false, false, null, null, null);
	}

}
