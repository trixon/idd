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

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.dgc.VMID;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.trixon.idl.shared.ClientCallbacks;
import se.trixon.idl.shared.IDServer;
import se.trixon.idl.shared.ImageServerCommander;
import se.trixon.idl.shared.IddHelper;
import se.trixon.idl.shared.ImageServerEvent;
import se.trixon.util.SystemHelper;
import se.trixon.util.Xlog;

/**
 *
 * @author Patrik Karlsson
 */
class ImageServer extends UnicastRemoteObject implements ImageServerCommander {

    private final Set<ClientCallbacks> mClientCallbacks = Collections.newSetFromMap(new ConcurrentHashMap<ClientCallbacks, Boolean>());
    private final Config mConfig = Config.getInstance();
    private String mRmiNameServer;
    private VMID mServerVmid;

    ImageServer() throws RemoteException, IOException {
        super(0);

        intiListeners();
        startServer();
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
    public void registerClient(ClientCallbacks clientCallback, String hostname) throws RemoteException {
        Xlog.timedOut("client connected: " + hostname);
        mClientCallbacks.add(clientCallback);
    }

    @Override
    public void removeClient(ClientCallbacks clientCallback, String hostname) throws RemoteException {
        if (mClientCallbacks.contains(clientCallback)) {
            Xlog.timedOut("client disconnected: " + hostname);
            mClientCallbacks.remove(clientCallback);
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

    private void intiListeners() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            notifyClientsShutdown();
        }));
    }

    private void notifyClientsShutdown() {
        mClientCallbacks.stream().forEach((clientCallback) -> {
            try {
                clientCallback.onServerEvent(ImageServerEvent.SHUTDOWN);
            } catch (RemoteException ex) {
                // nvm
            }
        });
    }

    private void startServer() {
        SystemHelper.enableRmiServer();
        mRmiNameServer = IddHelper.getRmiName(SystemHelper.getHostname(), mConfig.getPort(), IDServer.class);

        try {
            LocateRegistry.createRegistry(mConfig.getPort());
            mServerVmid = new VMID();
            Naming.rebind(mRmiNameServer, this);
            String message = String.format("started: %s (%s)", mRmiNameServer, mServerVmid.toString());
            Xlog.timedOut(message);
        } catch (IllegalArgumentException e) {
            Xlog.timedErr(e.getLocalizedMessage());
            IddHelper.exit();
        } catch (RemoteException e) {
            //nvm - server was running
            Xlog.timedErr(e.getLocalizedMessage());
            IddHelper.exit();
        } catch (MalformedURLException ex) {
            Xlog.timedErr(ex.getLocalizedMessage());
            IddHelper.exit();
        }
    }

    Set<ClientCallbacks> getClientCallbacks() {
        return mClientCallbacks;
    }
}
