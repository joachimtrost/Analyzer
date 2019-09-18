package org.jt.poker.filewatcher;

/**
 * Copyright 2013 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
//       import org.eclipse.jetty.websocket.api.RemoteEndpoint;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import static java.lang.Math.max;
import static java.lang.Thread.sleep;
import static java.nio.ByteBuffer.allocate;
import static java.nio.file.Files.newByteChannel;
import static java.nio.file.StandardOpenOption.READ;
import static org.apache.commons.io.IOUtils.closeQuietly;


/**
 * A non-blocking tail implementation allowing to read an arbitrary number of bytes from the end of a file
 * and follow changes to it.
 *
 * @author Olaf Otto
 */
public class FileWatcher implements Runnable {
    private static final int AWAIT_FILE_ROTATION_MILLIS = 1000;
    private static final int TAIL_CHECK_INTERVAL_MILLIS = 100;

    private final Logger LOGGER = Logger.getLogger(FileWatcher.class.getName());

    //    private final RemoteEndpoint remoteEndpoint;
    private final File file;
    private final long bytesToTail;

    private boolean stopped = false;
    private FileWatcherReader target;

    //    Tail(RemoteEndpoint remoteEndpoint, File file, long bytesToTail) {
    public FileWatcher(File file, long bytesToTail, FileWatcherReader target) {
        if (file == null) {
            throw new IllegalArgumentException("constructor parameter file must not be null");
        }
        this.bytesToTail = bytesToTail;
        this.file = file;
        this.target = target;
    }

    @Override
    public void run() {
        SeekableByteChannel channel = null;

        try {
            channel = newByteChannel(this.file.toPath(), READ);

            long availableInByte = this.file.length();
            long startingFromInByte = max(availableInByte - this.bytesToTail, 0);

            channel.position(startingFromInByte);

            long position = startingFromInByte;

            // Read up to this amount of data from the file at once.
            ByteBuffer readBuffer = allocate(4096);
            while (!this.stopped) {

                // The file might be temporarily gone during rotation. Wait, then decide
                // whether the file is considered gone permanently or whether a rotation has occurred.
                if (!this.file.exists()) {
                    sleep(AWAIT_FILE_ROTATION_MILLIS);
                }
                if (!this.file.exists()) {
                    LOGGER.severe("file not found");
                    return;
                }

                if (position > this.file.length()) {
                    LOGGER.info("file rotated");
                    position = 0;
                    closeQuietly(channel);
                    channel = newByteChannel(this.file.toPath(), READ);
                }

                int read = channel.read(readBuffer);

                if (read == -1) {
                    sleep(TAIL_CHECK_INTERVAL_MILLIS);
                    continue;
                }

                position = channel.position();

                readBuffer.flip();

                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < (read); i++) {
                    sb.append((char) (readBuffer.get()));
//                    sb.append(" ") ;
                }

//                LOGGER.info(String.format("next chars /%s/", sb.toString()));
//                LOGGER.info("before write");
                target.write(sb.toString());
//                LOGGER.info("after write");
                readBuffer.clear();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to tail " + this.file.getAbsolutePath() + ".", e);
        } catch (InterruptedException e) {
            if (!this.stopped) {
                LOGGER.log(Level.SEVERE, "Stopped tailing " + this.file.getAbsolutePath() + ", got interrupted.", e);
            }
        } finally {
            closeQuietly(channel);
        }
    }

    public void stop() {
        this.stopped = true;
    }
}





