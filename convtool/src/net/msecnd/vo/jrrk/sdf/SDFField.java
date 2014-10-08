package net.msecnd.vo.jrrk.sdf;

import com.google.gson.annotations.Expose;

public class SDFField {
	@Expose
	private String name;
	@Expose
	private String type;
	@Expose
	private String uri;
	@Expose
	private String id;
	
	
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public SDFField(){
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
