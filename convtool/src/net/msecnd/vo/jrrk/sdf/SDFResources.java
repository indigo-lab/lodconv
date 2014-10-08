package net.msecnd.vo.jrrk.sdf;

import com.google.gson.annotations.Expose;

public class SDFResources {
	@Expose
	private String path;
	@Expose
	private SDFSchema schema;
	
	public SDFResources(){
		
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public SDFSchema getSchema() {
		return schema;
	}

	public void setSchema(SDFSchema schema) {
		this.schema = schema;
	}

}
