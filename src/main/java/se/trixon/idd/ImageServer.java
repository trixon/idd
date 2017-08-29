/*
 * Copyright 2017 Patrik Karlsson.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.Xlog;
import se.trixon.idd.db.Db;
import se.trixon.idd.db.DbCreator;
import se.trixon.idd.db.FileVisitor;
import se.trixon.idl.shared.Commands;
import se.trixon.idl.shared.IddHelper;

/**
 *
 * @author Patrik Karlsson
 */
class ImageServer {

    private static final Logger LOGGER = Logger.getLogger(ImageServer.class.getName());

    private Set<ClientThread> mClientThreads = new HashSet<>();
    private final Config mConfig = Config.getInstance();
    private boolean mDirectKill;
    private boolean mKillInitiated;
    private ServerSocket mServerSocket;
    private boolean mSuccessfulStart;

    ImageServer() throws IOException {
        intiListeners();
        startServer();

        while (true) {
            try {
                Socket socket = mServerSocket.accept();
                ClientThread clientThread = new ClientThread(socket);
                mClientThreads.add(clientThread);
                clientThread.start();
                clientConnected(socket);
            } catch (IOException e) {
                Xlog.timedErr(e.getMessage());
            }
        }
    }

    private void clientConnected(Socket socket) {
        Xlog.timedOut(String.format("Client connected: %s:%d (%d)",
                socket.getLocalAddress(),
                socket.getLocalPort(),
                socket.getPort()
        ));
    }

    private void clientDisconnected(Socket socket) {
        Xlog.timedOut(String.format("Client disconnected: %s:%d (%d)",
                socket.getLocalAddress(),
                socket.getLocalPort(),
                socket.getPort()
        ));
    }

    private void intiListeners() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (mSuccessfulStart && !mKillInitiated) {
                mDirectKill = true;
                shutdown();
            }
            Xlog.timedOut("shut down!");
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
        Xlog.timedOut("shutting down...");
        mKillInitiated = true;
        mClientThreads.forEach((clientThread) -> {
            try {
                clientThread.kill();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
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
            Xlog.timedOut(message);

            mServerSocket = new ServerSocket(port);
            message = String.format("Listening for connections on port %d", port);
            Xlog.timedOut(message);
            mSuccessfulStart = true;
        } catch (IOException e) {
            Xlog.timedErr(e.getMessage());
            IddHelper.exit();
        }
    }

    class ClientThread extends Thread {

        private BufferedReader is = null;
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

                os.printf("OK IDD %d\n", IddHelper.PROTOCOL_VERSION);

                while (true) {
                    String line = is.readLine();
                    if (line != null) {
                        if (StringUtils.startsWith(line, "close")) {
                            break;
                        }

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
                Xlog.timedErr(e.getMessage());
            }
        }

        private void parseCommand(String command) {
            Xlog.timedOut("parse: " + command);
            String[] elements = StringUtils.split(command, " ");

            String cmd = elements[0];
            String[] args = ArrayUtils.remove(elements, 0);

            if (StringUtils.equals(cmd, "kill")) {
                shutdown();
            } else {
                os.printf("ACK [5@0] {} unknown command \"%s\"\n", cmd);
            }
        }

        void kill() throws IOException {
            os.println("*** Bye");
            is.close();
            os.close();
            mSocket.close();
        }
    }

    class Executor {

        private final String[] mArgs;
        private final String mCommand;
        private final Config mConfig = Config.getInstance();
        private final Db mDb = Db.getInstance();

        public Executor(String command, String... args) {
            mCommand = command;
            mArgs = args;
        }

        private String update() {
            String resultMessage = null;

            if (mDb.isUpdating()) {
                resultMessage = "Update already in progress";
            } else {
                try {
                    mDb.setUpdating(true);
                    mDb.connectionOpen();
                    DbCreator.getInstance().initDb();
                    EnumSet<FileVisitOption> fileVisitOptions = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
                    FileVisitor fileVisitor = new FileVisitor();
                    Files.walkFileTree(mConfig.getImageDirectory().toPath(), fileVisitOptions, Integer.MAX_VALUE, fileVisitor);
                    mDb.connectionCommit();
                    resultMessage = "Update done";
                } catch (ClassNotFoundException | SQLException | IOException ex) {
                    Logger.getLogger(Executor.class.getName()).log(Level.SEVERE, null, ex);
                    resultMessage = "Update failed";
                } finally {
                    mDb.setUpdating(false);
                }
            }

            return resultMessage;
        }

        String execute() {
            Xlog.timedOut(String.format("execute: %s", mCommand));
            String resultMessage = null;

            switch (mCommand) {
                case Commands.RANDOM:
                    String path = Querator.getInstance().getRandomPath();
                    if (path != null) {
                        resultMessage = sendFile(path);
                    } else {
                        resultMessage = "No file to send";
                    }
                    break;

                case Commands.STATS:
                    break;

                case Commands.UPDATE:
                    resultMessage = update();
                    break;

                case Commands.VERSION:
                    resultMessage = String.format("idd version: %s", SystemHelper.getJarVersion(getClass()));
                    break;
            }

            Xlog.timedOut(resultMessage);
            return resultMessage;
        }
    }
}
