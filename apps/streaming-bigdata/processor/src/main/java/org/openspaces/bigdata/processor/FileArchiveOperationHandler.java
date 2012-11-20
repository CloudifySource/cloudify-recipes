/*
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.bigdata.processor;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import org.openspaces.archive.ArchiveOperationHandler;

/**
 * This is an {@link ExternalPersistence} implementation to a local file system.
 * 
 * @author Dotan Horovits
 */
public class FileArchiveOperationHandler implements ArchiveOperationHandler {

    private static final Logger log = Logger.getLogger(FileArchiveOperationHandler.class.getName());

    private File file;

    public FileArchiveOperationHandler(String fileName) throws IOException {
        this(new File(fileName));
    }

    public FileArchiveOperationHandler(File file) throws IOException {
        this.file = file;
        log.info("using file persistence: " + file.getAbsolutePath());
        if (file.exists()) {
            file.delete();
        }

        log.info("creating file for file persistence: " + file.getAbsolutePath());
        file.createNewFile();
    }

    private void write(Object data) throws IOException {
        FileWriter fileWritter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileWritter = new FileWriter(file, true);
            bufferedWriter = new BufferedWriter(fileWritter);
            bufferedWriter.write(data.toString());
        } finally {
            closeQuietly(bufferedWriter);
            closeQuietly(fileWritter);
        }
    }

    @Override
    public void archive(Object... dataArray) {
    	log.info("Writing " + dataArray.length + " object(s) to File");
    	
        if (dataArray.length < 1) {
            return;
        }
        
        StringBuilder lines = new StringBuilder();
        for (Object obj : dataArray) {
            lines.append(obj).append("\n");
        }
        try {
			write(lines);
		} catch (IOException e) {
			throw new FileExternalPersistenceException(e);
		}
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                log.warning(e.getMessage());
            }
        }
    }

	@Override
	public boolean supportsBatchArchiving() {
		return false;
	}
}
