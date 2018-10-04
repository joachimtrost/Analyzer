package org.jt.poker.filewatcher;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

public class FileWatcherReader extends Reader {
    private final Logger LOGGER = Logger.getLogger(FileWatcherReader.class.getName());
    private FileWatcher fileWatcher;
    private StringBuffer availableChars = new StringBuffer();
    private final Semaphore available = new Semaphore(1, true);

    public FileWatcherReader(File file, long bytesToTail) {
        super();
        if (file == null) {
            throw new IllegalArgumentException("constructor parameter file must not be null");
        }
        fileWatcher = new FileWatcher(file, bytesToTail, this);
        Thread thread = new Thread(fileWatcher);
        thread.start();
        try {
            available.acquire();
        } catch (InterruptedException ex) {
            throw new RuntimeException("interrupted", ex);
        }
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
//        LOGGER.info(String.format("initial off %d len %d charsLen %d davailable %d",off,len,availableChars.length(),available.availablePermits()));
        if (availableChars.length() <= 0) {
//            LOGGER.info("acqu 1") ;
            try {
                available.acquire();
                available.release();
            } catch (InterruptedException ex) {
                throw new RuntimeException("interrupted", ex);
            }
//            LOGGER.info("acqu 1 done") ;
        }
//        LOGGER.info(String.format("seccond off %d len %d charsLen %d davailable %d",off,len,availableChars.length(),available.availablePermits()));
        int lenToRead;
        if (len <= availableChars.length()) {
            lenToRead = len;
        } else {
            lenToRead = availableChars.length();
        }
        availableChars.getChars(0, lenToRead, cbuf, off);
        availableChars.delete(0, lenToRead);
        if (availableChars.length() <= 0) {
//            LOGGER.info("acqu 2") ;
            try {
                available.acquire();
            } catch (InterruptedException ex) {
                throw new RuntimeException("interrupted", ex);
            }
//            LOGGER.info("acqu 2 done") ;
        }
        return lenToRead;
    }

    public void write(String input) {
        availableChars.append(input);
//        LOGGER.info(String.format("availableChars %s", availableChars));
        available.release();
    }

    @Override
    public void close() throws IOException {

    }
}
