package net.msecnd.vo.jrrk.sdf;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

public class SDFSchema {
	@Expose
	private List<SDFField> fields = new ArrayList<SDFField>();
	
	public SDFSchema(){
		
	}

	public List<SDFField> getFields() {
		return fields;
	}

	public void setFields(List<SDFField> fields) {
		this.fields = fields;
	}

}
