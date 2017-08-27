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

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.Xlog;
import se.trixon.idd.db.Db;
import se.trixon.idd.db.DbCreator;
import se.trixon.idd.db.FileVisitor;
import se.trixon.idl.shared.Commands;
import se.trixon.idl.shared.IddHelper;
import se.trixon.idl.shared.ImageClientCommander;
import se.trixon.idl.shared.ImageServerCommander;
import se.trixon.idl.shared.ImageServerEvent;

/**
 *
 * @author Patrik Karlsson
 */
class ImageServer implements ImageServerCommander {

    private Set<ClientThread> mClientThreads = new HashSet<>();
    private final Config mConfig = Config.getInstance();
    private final Set<ImageClientCommander> mImageClientCommanders = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private ServerSocket mServerSocket;

    ImageServer() throws IOException {
        intiListeners();
        startServer();
        //initTimer();

        while (true) {
            try {
                Socket socket = mServerSocket.accept();
                ClientThread clientThread = new ClientThread(socket);
                mClientThreads.add(clientThread);
                clientThread.start();
                Xlog.timedOut(String.format("Client connected: %s:%d (%d)",
                        socket.getLocalAddress(),
                        socket.getLocalPort(),
                        socket.getPort()
                ));
            } catch (IOException e) {
                Xlog.timedErr(e.getMessage());
            }
        }
    }

    @Override
    public void execute(String command, String... args) {
        Executor executor = new Executor(command, args);
        if (command.equalsIgnoreCase(Commands.UPDATE)) {
            Thread t = new Thread(() -> {
                String resultMessage = executor.execute();
                notifyClientsExecutor(command, resultMessage);
            });

            t.start();
        } else {
            String resultMessage = executor.execute();
            notifyClientsExecutor(command, resultMessage);
        }
    }

    @Override
    public String getStatus() {
        return "status";
    }

    @Override
    public void registerClientCommander(ImageClientCommander imageClientCommander, String hostname) {
        Xlog.timedOut("client connected: " + hostname);
        mImageClientCommanders.add(imageClientCommander);
    }

    @Override
    public void removeClientCommander(ImageClientCommander imageClientCommander, String hostname) {
        if (mImageClientCommanders.contains(imageClientCommander)) {
            Xlog.timedOut("client disconnected: " + hostname);
            mImageClientCommanders.remove(imageClientCommander);
        }
    }

    @Override
    public void shutdown() {
        Xlog.timedOut("shutdown");

        notifyClientsShutdown();

        System.exit(0);
    }

    private void initTimer() {
        Timer timer = new Timer(2000, (ActionEvent e) -> {
//            try {
            execute("random");
//            } catch (RemoteException ex) {
//                Logger.getLogger(ImageServer.class.getName()).log(Level.SEVERE, null, ex);
//            }
        });
        timer.start();
    }

    private void intiListeners() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            notifyClientsShutdown();
        }));
    }

    private void notifyClientsExecutor(String command, String... strings) {
        mImageClientCommanders.stream().forEach((imageClientCommander) -> {
            imageClientCommander.notifyExecutor(command, strings);
        });
    }

    private void notifyClientsShutdown() {
        mImageClientCommanders.stream().forEach((imageClientCommander) -> {
            imageClientCommander.notifyServer(ImageServerEvent.SHUTDOWN);
        });
    }

    private String sendFile(String path) {
        String result;

        if (mImageClientCommanders.isEmpty()) {
            result = "no recievers connected - not sending";
        } else if (path != null) {
            result = "recievers connected - try sending file";
            for (ImageClientCommander clientCommander : mImageClientCommanders) {
                Thread thread = new Thread(() -> {
////                        clientCommander.sendFile(remoteInputStreamServer.export());
                });

                thread.start();
            }
        } else {
            result = "No file to send";
        }

        return result;
    }

    private void startServer() {
        try {
            final int port = mConfig.getPort();
            String message = String.format("Starting server on port %d", port);
            Xlog.timedOut(message);

            mServerSocket = new ServerSocket(port);
            message = String.format("Listening for connections on port %d", port);
            Xlog.timedOut(message);
        } catch (IOException e) {
            Xlog.timedErr(e.getMessage());
            IddHelper.exit();
        }
    }

    class ClientThread extends Thread {

        private String clientName = null;
        private BufferedReader is = null;
        private final Socket mSocket;
        private PrintStream os = null;

        public ClientThread(Socket clientSocket) {
            mSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                // Create input and output streams for this client.
                is = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                os = new PrintStream(mSocket.getOutputStream());
                String name;
                name = "sd" + 2;
                while (true) {
                    os.println("Enter your name.");
                    name = is.readLine().trim();
                    if (name.indexOf('@') == -1) {
                        break;
                    } else {
                        os.println("The name should not contain '@' character.");
                    }
                }

                /* Welcome the new the client. */
                os.println("Welcome " + name + " to our chat room.\nTo leave enter /quit in a new line.");
                synchronized (this) {
                    for (ClientThread clientThread : mClientThreads) {
                        if (clientThread == this) {
                            clientName = "@" + name;
                        } else {
                            clientThread.os.println("*** A new user " + name + " entered the chat room !!! ***");
                        }
                    }
                }

                /* Start the conversation. */
                while (true) {
                    String line = is.readLine();
                    if (line != null) {
                        if (line.startsWith("/quit")) {
                            break;
                        }

                        synchronized (this) {
                            for (ClientThread clientThread : mClientThreads) {
                                if (clientThread.clientName != null) {
                                    clientThread.os.println("<" + name + "> " + line);
                                }
                            }
                        }
                    }
                }

                synchronized (this) {
                    for (ClientThread clientThread : mClientThreads) {
                        if (clientThread != this && clientThread.clientName != null) {
                            clientThread.os.println("*** The user " + name + " is leaving the chat room !!! ***");
                        }
                    }
                }
                os.println("*** Bye " + name + " ***");

                synchronized (this) {
                    mClientThreads.remove(this);
                }

                // Close the output stream, close the input stream, close the socket.
                is.close();
                os.close();
                mSocket.close();
            } catch (IOException e) {
                Xlog.timedErr(e.getMessage());
            }
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
