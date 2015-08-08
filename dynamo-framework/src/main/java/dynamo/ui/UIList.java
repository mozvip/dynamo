package dynamo.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.DataModel;

public class UIList<T> extends DataModel<T> implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<T> data = null;
	
	private int currentIndex = 0;
	
	public UIList( List<T> data ) {
		this.data = data;
	}

	public UIList() {
		data = new ArrayList<T>();
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public T getRowData() {
		return data.get( currentIndex );
	}

	@Override
	public int getRowIndex() {
		return currentIndex;
	}

	@Override
	public Object getWrappedData() {
		return data;
	}

	@Override
	public boolean isRowAvailable() {
		return currentIndex < getRowCount();
	}

	@Override
	public void setRowIndex(int value) {
		currentIndex = value;
	}

	@Override
	public void setWrappedData(Object arg0) {
	}

	public void add( T object ) {
		data.add( object );
	}
	
	public String remove() {
		T object = getRowData();
		data.remove( object );
		return "";
	}
	
	public void remove( int index ) {
		data.remove( index );
	}
	
	public T getSelection() {
		return getRowData();
	}

}
