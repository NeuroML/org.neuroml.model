package org.neuroml.model.util;

public class NeuroMLException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NeuroMLException(Throwable cause) {
		super(cause);
	}
    
	public NeuroMLException(String message, Throwable cause) {
		super(message, cause);
	}

}
