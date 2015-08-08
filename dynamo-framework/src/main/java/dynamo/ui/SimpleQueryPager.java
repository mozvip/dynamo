package dynamo.ui;

import java.util.ArrayList;
import java.util.List;

public class SimpleQueryPager<T> implements DataPager<T> {

	public final static int MAX_PAGES = 10;

	private int currentPage = 0;
	protected int pageSize;
	protected int totalCount = -1;

	private List<T> items = null;
	protected List<T> objects = null;

	public SimpleQueryPager( int pageSize, List<T> objects ) {
		this.pageSize = pageSize;
		if (objects != null) {
			this.objects = objects;
		}
		totalCount = getTotalCount();
	}

	public void refresh() {
		int min = currentPage * pageSize;

		if (min > totalCount) {
			goToPage(0);
		} else {
			int count = pageSize;
			if ( min + count > totalCount) {
				count = totalCount - min;
			}
			items = getItems( min, count );
		}
	}
	
	@Override
	public List<T> getItems(int start, int count) {
		List<T> copy = new ArrayList<T>( count );
		copy.addAll( objects.subList( start, start + count ) );
		return copy;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	@Override
	public int getTotalCount() {
		return objects.size();
	}

	public void goToPage( int page ) {
		if (page >= 0) {
			this.currentPage = page;
			refresh();
		}
	}

	public void nextPage() {
		goToPage( currentPage + 1 );
	}
	
	public void previousPage() {
		goToPage( currentPage - 1 );
	}

	public long getPagesCount() {
		return totalCount / pageSize;
	}
	
	public int getFirstResult() {
		return currentPage * pageSize;
	}
	
	public long getLastPage() {
		return totalCount / pageSize;
	}
	
	public List<T> getItems() {
		if (items == null) {
			goToPage(0);
		}
		return items;
	}

}
