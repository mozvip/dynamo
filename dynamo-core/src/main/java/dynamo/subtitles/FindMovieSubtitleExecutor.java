package dynamo.subtitles;

import dynamo.core.model.TaskExecutor;
import dynamo.model.backlog.subtitles.FindMovieSubtitleTask;

public class FindMovieSubtitleExecutor extends TaskExecutor<FindMovieSubtitleTask> {

	public FindMovieSubtitleExecutor(FindMovieSubtitleTask item) {
		super(item);
	}

	@Override
	public void execute() throws Exception {
		// TODO
	}

}
