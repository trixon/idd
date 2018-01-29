/*
 * Copyright 2018 Patrik Karlsson.
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
package se.trixon.idd;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.ImageScaler;
import se.trixon.almond.util.SystemHelper;
import se.trixon.idd.db.Db;
import se.trixon.idd.db.manager.ImageManager;
import se.trixon.idl.Command;
import se.trixon.idl.FrameImage;
import se.trixon.idl.FrameImageCarrier;
import se.trixon.idl.IddHelper;

/**
 *
 * @author Patrik Karlsson
 */
class ImageServer {

    private static final Logger LOGGER = Logger.getLogger(ImageServer.class.getName());

    private Set<ClientThread> mClientThreads = new HashSet<>();
    private final Config mConfig = Config.getInstance();
    private final Db mDb = Db.getInstance();
    private boolean mDirectKill;
    private final ImageManager mImageManager = ImageManager.getInstance();
    private final ImageScaler mImageScaler = ImageScaler.getInstance();
    private boolean mKillInitiated;
    private Set<ClientThread> mRegistredFrames = new HashSet<>();
    private ServerSocket mServerSocket;
    private boolean mSuccessfulStart;

    ImageServer() throws IOException {
        initListeners();
        startServer();
//        mDb.update(mConfig.getImageDirectory().getPath());
//        System.exit(0);
        while (true) {
            try {
                Socket socket = mServerSocket.accept();
                ClientThread clientThread = new ClientThread(socket);
                mClientThreads.add(clientThread);
                clientThread.start();
                clientConnected(socket);
            } catch (IOException e) {
                LOGGER.severe(e.getMessage());
            }
        }
    }

    private void clientConnected(Socket socket) {
        LOGGER.info(String.format("Client connected: %s:%d (%d)",
                socket.getLocalAddress(),
                socket.getLocalPort(),
                socket.getPort()
        ));
    }

    private void clientDisconnected(Socket socket) {
        LOGGER.info(String.format("Client disconnected: %s:%d (%d)",
                socket.getLocalAddress(),
                socket.getLocalPort(),
                socket.getPort()
        ));
    }

    private String getImagePath(FrameImage frameImage) {
        String path = frameImage.getPath();

        if (mConfig.getCacheDirectory() != null) {
            File cacheFile = new File(mConfig.getCacheDirectory(), frameImage.getUniqueHash());
            if (cacheFile.exists()) {
                LOGGER.info(String.format("File exists in cache: %s", cacheFile.getAbsolutePath()));
            } else {
                try {
                    final File originalFile = new File(frameImage.getPath());
                    BufferedImage scaledImage = mImageScaler.getScaledImage(originalFile, new Dimension(mConfig.getCacheWidth(), mConfig.getCacheHeight()));
                    ImageIO.write(scaledImage, "jpeg", cacheFile);
                    path = cacheFile.getAbsolutePath();
                    LOGGER.info(String.format("File cache generated: %s", cacheFile.getAbsolutePath()));
                } catch (IOException ex) {
                    LOGGER.severe(ex.getMessage());
                }
            }
        }

        return path;
    }

    private void initListeners() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (mSuccessfulStart && !mKillInitiated) {
                mDirectKill = true;
                shutdown();
            }
            LOGGER.info("shut down!");
        }));
    }

    private synchronized void shutdown() {
        LOGGER.info("shutting down...");
        mKillInitiated = true;
        mClientThreads.forEach((clientThread) -> {
            try {
                clientThread.mSocket.close();
                clientThread.kill();
            } catch (IOException ex) {
                LOGGER.severe(ex.getMessage());
            }
        });

        if (!mDirectKill) {
            IddHelper.exit();
        }
    }

    private void startServer() {
        try {
            final int port = mConfig.getPort();
            String message = String.format("Starting server on port %d", port);
            LOGGER.info(message);

            mServerSocket = new ServerSocket(port);
            message = String.format("Listening for connections on port %d", port);
            LOGGER.info(message);
            mSuccessfulStart = true;
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
            IddHelper.exit();
        }
    }

    class ClientThread extends Thread {

        private static final String OK = "OK";

        private BufferedReader is = null;
        private boolean mKeepReading = true;
        private final Socket mSocket;
        private PrintStream os = null;

        public ClientThread(Socket clientSocket) {
            setName(String.format("%s [%s:%d]",
                    getClass().getSimpleName(),
                    clientSocket.getInetAddress(),
                    clientSocket.getPort()
            ));

            mSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                is = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                os = new PrintStream(mSocket.getOutputStream());

                os.printf("OK IDD %s\n", IddHelper.PROTOCOL_VERSION);

                while (mKeepReading) {
                    String line = StringUtils.defaultIfEmpty(is.readLine(), "close");
                    parseCommand(line);
                }

                clientDisconnected(mSocket);

                synchronized (this) {
                    mClientThreads.remove(this);
                    mRegistredFrames.remove(this);
                }

                kill();
            } catch (IOException e) {
                LOGGER.severe(e.getMessage());
            }
        }

        private void parseCommand(String commandString) {
            LOGGER.log(Level.INFO, "parse: {0}", commandString);
            String[] elements = StringUtils.split(commandString, " ");

            String cmd = elements[0];
            String[] args = ArrayUtils.remove(elements, 0);

            try {
                Command command = Command.valueOf(cmd.toUpperCase(Locale.ROOT));
                System.out.println(command);

                if (command.validateArgs(args)) {
                    String path;
                    switch (command) {
                        case CLOSE:
                            mKeepReading = false;
                            break;

                        case DEREGISTER:
                            if (mRegistredFrames.contains(this)) {
                                if (mRegistredFrames.remove(this)) {
                                    send("deregistered");
                                }
                            } else {
                                send("Nothing to do, not registered");
                            }

                            send(OK);
                            break;

                        case KILL:
                            shutdown();
                            break;

                        case PING:
                            send(OK);
                            break;

                        case RANDOM:
                            try {
                                sendImage(mImageManager.getRandomImage());
                            } catch (NullPointerException | SQLException ex) {
                                send("ex.getMessage()");
                                LOGGER.log(Level.SEVERE, null, ex);
                            }
                            break;

                        case REGISTER:
                            if (!mRegistredFrames.contains(this)) {
                                if (mRegistredFrames.add(this)) {
                                    send("registered");
                                }
                            } else {
                                send("Nothing to do, already registered");
                            }
                            send(OK);
                            break;

                        case UPDATE:
                            if (args.length > 0) {
                                path = args[0];
                            } else {
                                path = mConfig.getImageDirectory().getPath();
                            }
                            String response = mDb.update(path);
                            send(response);
                            System.out.println(response);
                            break;

                        case VERSION:
                            send(String.format("idd version: %s\n%s", SystemHelper.getJarVersion(getClass()), OK));
                            break;

                        default:
                            throw new AssertionError();
                    }
                } else {
                    send(String.format("ACK [2@0] {%s} wrong number of arguments for \"%s\"", cmd, cmd));
                }
            } catch (IllegalArgumentException e) {
                send(String.format("ACK [5@0] {} unknown command \"%s\"", cmd));
            }
        }

        private void send(String s) {
            os.println(s);
        }

        private void sendImage(FrameImage frameImage) {
            if (mRegistredFrames.isEmpty()) {
                String s = "Nothing to do, no registered frames";
                System.out.println(s);
                send(s);
            } else {
                //System.out.println(frameImage);

                FrameImageCarrier frameImageCarrier = new FrameImageCarrier(frameImage, getImagePath(frameImage));
                String json = frameImageCarrier.toJson();

                for (ClientThread frameThread : mRegistredFrames) {
                    frameThread.send(IddHelper.FRAME_IMAGE_BEG);
                    frameThread.send(json);
                    frameThread.send(IddHelper.FRAME_IMAGE_END);
                }
            }

            send(OK); //Send OK to Commander
        }

        void kill() throws IOException {
            is.close();
            os.close();
            mSocket.close();
        }
    }
}
