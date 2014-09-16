package org.neuroml.model.util;

public class StandardTest {
	
	public enum LEVEL {ERROR, WARNING};
	public int id;
	public String description;
	public LEVEL level;

	public StandardTest(int id, String description) {
		this.id = id;
		this.description = description;
		this.level = LEVEL.ERROR;
	}
	
	public StandardTest(int id, String description, LEVEL level) {
		this.id = id;
		this.description = description;
		this.level = level;
	}
	
	public boolean isWarning() {
		return level==LEVEL.WARNING;
	}


}
