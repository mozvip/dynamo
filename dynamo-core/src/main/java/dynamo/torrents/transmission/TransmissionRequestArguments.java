package dynamo.torrents.transmission;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class TransmissionRequestArguments {
	
	private String[] fields;
	private List<Integer> ids;
	private String metainfo;
	private String filename;
	@SerializedName("delete-local-data") private boolean deleteLocalData;

	public String[] getFields() {
		return fields;
	}
	public void setFields(String[] fields) {
		this.fields = fields;
	}
	public List<Integer> getIds() {
		return ids;
	}
	public void setIds(List<Integer> ids) {
		this.ids = ids;
	}
	public String getMetainfo() {
		return metainfo;
	}
	public void setMetainfo(String metainfo) {
		this.metainfo = metainfo;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public boolean isDeleteLocalData() {
		return deleteLocalData;
	}
	public void setDeleteLocalData(boolean deleteLocalData) {
		this.deleteLocalData = deleteLocalData;
	}

}
