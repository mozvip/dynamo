package dynamo.core.configuration.items;

public class ConfigurationItemRow<E> {
	
	private int index;
	private E value;
	private boolean enabled;

	public ConfigurationItemRow(int index, E value) {
		super();
		this.index = index;
		this.value = value;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public E getValue() {
		return value;
	}
	public void setValue(E wrappedObject) {
		this.value = wrappedObject;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
