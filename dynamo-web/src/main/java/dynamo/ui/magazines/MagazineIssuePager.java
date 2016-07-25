package dynamo.ui.magazines;

import java.util.List;

import dynamo.magazines.model.MagazineIssue;
import dynamo.ui.SimpleQueryPager;

public class MagazineIssuePager extends SimpleQueryPager<MagazineIssue> {

	public MagazineIssuePager( List<MagazineIssue> objects ) {
		super(100, objects);
	}
	
	public MagazineIssue remove( int id ) {
		for (MagazineIssue issue : objects) {
			if (issue.getId() == id) {
				objects.remove( issue );
				return issue;
			}
		}
		return null;
	}

}
