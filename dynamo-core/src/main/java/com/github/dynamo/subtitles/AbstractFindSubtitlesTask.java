package com.github.dynamo.subtitles;

import com.github.dynamo.core.model.DownloadableTask;
import com.github.dynamo.model.Downloadable;

public abstract class AbstractFindSubtitlesTask extends DownloadableTask {
	
	public AbstractFindSubtitlesTask(Downloadable downloadable) {
		super(downloadable);
	}

}
