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
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import se.trixon.almond.util.ImageScaler;
import se.trixon.almond.util.SystemHelper;
import se.trixon.idd.db.Db;
import se.trixon.idd.db.manager.ImageManager;
import se.trixon.idl.shared.Command;
import se.trixon.idl.shared.IddHelper;
import se.trixon.idl.shared.ImageDescriptor;
import se.trixon.idl.shared.db.Image;

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
    private final Querator mQuerator = Querator.getInstance();
    private ServerSocket mServerSocket;
    private boolean mSuccessfulStart;

    ImageServer() throws IOException {
        intiListeners();
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

    private String getImagePath(Image image) {
        String path = image.getPath();

        if (mConfig.getCacheDirectory() != null) {
            File cacheFile = new File(mConfig.getCacheDirectory(), image.getUniqueHash());
            if (cacheFile.exists()) {
                LOGGER.info(String.format("File exists in cache: %s", cacheFile.getAbsolutePath()));
            } else {
                try {
                    final File originalFile = new File(image.getPath());
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

    private void intiListeners() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (mSuccessfulStart && !mKillInitiated) {
                mDirectKill = true;
                shutdown();
            }
            LOGGER.info("shut down!");
        }));
    }

    private String sendFile(String path) {
        String result = null;

//        if (mImageClientCommanders.isEmpty()) {
//            result = "no recievers connected - not sending";
//        } else if (path != null) {
//            result = "recievers connected - try sending file";
//            for (ImageClientCommander clientCommander : mImageClientCommanders) {
//                Thread thread = new Thread(() -> {
//////                        clientCommander.sendFile(remoteInputStreamServer.export());
//                });
//
//                thread.start();
//            }
//        } else {
//            result = "No file to send";
//        }
        return result;
    }

    private synchronized void shutdown() {
        LOGGER.info("shutting down...");
        mKillInitiated = true;
        mClientThreads.forEach((clientThread) -> {
            try {
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
            setName(String.format("%s [%s]", getClass().getCanonicalName(), clientSocket.getInetAddress()));
            mSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                is = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                os = new PrintStream(mSocket.getOutputStream());

                os.printf("OK IDD %s\n", IddHelper.PROTOCOL_VERSION);

                while (mKeepReading) {
                    String line = is.readLine();
                    if (line != null) {
                        if (!StringUtils.isBlank(line)) {
                            parseCommand(line);
                        }

//                        synchronized (this) {
//                            for (ClientThread clientThread : mClientThreads) {
//                                if (clientThread.clientName != null) {
//                                    clientThread.os.println("<" + name + "> " + line);
//                                }
//                            }
//                        }
                    }
                }

                clientDisconnected(mSocket);

                synchronized (this) {
                    mClientThreads.remove(this);
                }

                kill();
            } catch (IOException e) {
                LOGGER.severe(e.getMessage());
            }
        }

        private void parseCommand(String commandString) {
            LOGGER.info("parse: " + commandString);
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

                        case KILL:
                            shutdown();
                            break;

                        case PING:
                            send(OK);
                            break;

                        case RANDOM:
                            sendImage(mImageManager.getRandomImage());
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

        private void sendImage(Image image) {
            ImageDescriptor imageDescriptor = new ImageDescriptor();
            imageDescriptor.setImage(image);
            final String imagePath = getImagePath(image);
            imageDescriptor.setPath(imagePath);
            imageDescriptor.setBase64FromPath(imagePath);

//            send("IMAGE PACKET");
            send(imageDescriptor.toJson());
            send(OK);

        }

        private void sendImage(String path) {
            ImageDescriptor imageDescriptor = new ImageDescriptor();
            imageDescriptor.setPath(path);
            imageDescriptor.setBase64FromPath(path);

//            send("IMAGE PACKET");
            send(imageDescriptor.toJson());
            send(OK);

        }

        void kill() throws IOException {
//            os.println("*** Bye");
            is.close();
            os.close();
            mSocket.close();
        }
    }
}
