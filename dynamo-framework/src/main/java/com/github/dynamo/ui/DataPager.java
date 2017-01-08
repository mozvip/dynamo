package com.github.dynamo.ui;

import java.util.List;

public interface DataPager<T> {
	
	public int getTotalCount();
	
	public List<T> getItems(int start, int end);
	

}
