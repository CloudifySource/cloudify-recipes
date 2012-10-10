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

/**
 * This is an {@link ExternalPersistence} implementation to a local file system.
 * 
 * @author Dotan Horovits
 */
public class FileExternalPersistence implements ExternalPersistence {

    private static final Logger log = Logger.getLogger(FileExternalPersistence.class.getName());

    private File file;

    public FileExternalPersistence(String fileName) throws IOException {
        this(new File(fileName));
    }

    public FileExternalPersistence(File file) throws IOException {
        this.file = file;
        log.info("using file persistence: " + file.getAbsolutePath());
        if (file.exists()) {
            file.delete();
        }

        log.info("creating file for file persistence: " + file.getAbsolutePath());
        file.createNewFile();
    }

    @Override
    public void write(Object data) throws IOException {
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
    public void writeBulk(Object[] dataArray) throws IOException {
        if (dataArray.length < 1) {
            return;
        }
        StringBuilder lines = new StringBuilder();
        for (Object obj : dataArray) {
            lines.append(obj).append("\n");
        }
        write(lines);
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
}
