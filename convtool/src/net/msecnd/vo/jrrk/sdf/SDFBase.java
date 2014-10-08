package net.msecnd.vo.jrrk.sdf;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

public class SDFBase {
	

	
	@Expose
	private String name;
	
	@Expose
	private String title;
	
	@Expose
	private List<SDFResources> resources = new ArrayList<SDFResources>();
	
	public SDFBase(){
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<SDFResources> getResources() {
		return resources;
	}

	public void setResources(List<SDFResources> resources) {
		this.resources = resources;
	}
	

}
