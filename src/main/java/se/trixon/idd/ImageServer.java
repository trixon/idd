/*
 * Copyright 2016 Patrik Karlsson.
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

import com.healthmarketscience.rmiio.GZIPRemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.dgc.VMID;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.Collections;
import java.util.EnumSet;
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
import se.trixon.idl.shared.IDServer;
import se.trixon.idl.shared.IddHelper;
import se.trixon.idl.shared.ImageClientCommander;
import se.trixon.idl.shared.ImageServerCommander;
import se.trixon.idl.shared.ImageServerEvent;

/**
 *
 * @author Patrik Karlsson
 */
class ImageServer extends UnicastRemoteObject implements ImageServerCommander {

    private final Config mConfig = Config.getInstance();
    private final Set<ImageClientCommander> mImageClientCommanders = Collections.newSetFromMap(new ConcurrentHashMap<ImageClientCommander, Boolean>());
    private String mRmiNameServer;
    private VMID mServerVmid;

    ImageServer() throws RemoteException, IOException {
        super(0);

        intiListeners();
        startServer();
        initTimer();
    }

    @Override
    public void execute(String command, String... args) throws RemoteException {
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
    public String getStatus() throws RemoteException {
        return "status";
    }

    @Override
    public VMID getVMID() throws RemoteException {
        return mServerVmid;
    }

    @Override
    public void registerClientCommander(ImageClientCommander imageClientCommander, String hostname) throws RemoteException {
        Xlog.timedOut("client connected: " + hostname);
        mImageClientCommanders.add(imageClientCommander);
    }

    @Override
    public void removeClientCommander(ImageClientCommander imageClientCommander, String hostname) throws RemoteException {
        if (mImageClientCommanders.contains(imageClientCommander)) {
            Xlog.timedOut("client disconnected: " + hostname);
            mImageClientCommanders.remove(imageClientCommander);
        }
    }

    @Override
    public void shutdown() throws RemoteException {
        Xlog.timedOut("shutdown");

        notifyClientsShutdown();

        try {
            Naming.unbind(mRmiNameServer);
            System.exit(0);
        } catch (NotBoundException | MalformedURLException ex) {
            Logger.getLogger(ImageServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initTimer() {
        Timer t = new Timer(5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    execute("random", null);
                } catch (RemoteException ex) {
                    Logger.getLogger(ImageServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        t.start();

    }

    private void intiListeners() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            notifyClientsShutdown();
        }));
    }

    private void notifyClientsExecutor(String command, String... strings) {
        mImageClientCommanders.stream().forEach((imageClientCommander) -> {
            try {
                imageClientCommander.notifyExecutor(command, strings);
            } catch (RemoteException ex) {
                // nvm
            }
        });
    }

    private void notifyClientsShutdown() {
        mImageClientCommanders.stream().forEach((imageClientCommander) -> {
            try {
                imageClientCommander.notifyServer(ImageServerEvent.SHUTDOWN);
            } catch (RemoteException ex) {
                // nvm
            }
        });
    }

    private String sendFile(String path) {
        RemoteInputStreamServer remoteInputStreamServer = null;
        String result = null;

        if (mImageClientCommanders.isEmpty()) {
            result = "no recievers connected - not sending";

        } else {
            result = "sendFile OK";
            try {
                remoteInputStreamServer = new GZIPRemoteInputStream(new BufferedInputStream(new FileInputStream(path)));
                for (ImageClientCommander clientCommander : mImageClientCommanders) {
                    try {
                        clientCommander.sendFile(remoteInputStreamServer.export());
                    } catch (RemoteException ex) {
                        Xlog.timedErr(ex.getMessage());
                        result = ex.getMessage();
                    }

                }

            } catch (IOException ex) {
                result = ex.getMessage();
                Logger.getLogger(ImageServer.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (remoteInputStreamServer != null) {
                    remoteInputStreamServer.close();
                }
            }
        }

        return result;
    }

    private void startServer() {
        mRmiNameServer = IddHelper.getRmiName(SystemHelper.getHostname(), mConfig.getPort(), IDServer.class);

        try {
            LocateRegistry.createRegistry(mConfig.getPort());
            mServerVmid = new VMID();
            Naming.rebind(mRmiNameServer, this);
            String message = String.format("started: %s (%s)", mRmiNameServer, mServerVmid.toString());
            Xlog.timedOut(message);
        } catch (IllegalArgumentException e) {
            Xlog.timedErr(e.getMessage());
            IddHelper.exit();
        } catch (RemoteException e) {
            //nvm - server was running
            Xlog.timedErr(e.getMessage());
            IddHelper.exit();
        } catch (MalformedURLException ex) {
            Xlog.timedErr(ex.getMessage());
            IddHelper.exit();
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
                    resultMessage = sendFile(Querator.getInstance().getRandomPath());
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
