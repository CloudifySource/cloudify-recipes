package org.openspaces.bigdata.processor;

import java.io.IOException;

/**
 * A non-checked wrapper for persisting exceptions from {@link FileExternalPersistence}
 * 
 * @author Itai Frenkel
 */
public class FileExternalPersistenceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FileExternalPersistenceException(IOException cause) {
		super("Failed persisting to file", cause);
	}
	
}
