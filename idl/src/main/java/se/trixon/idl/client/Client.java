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
package se.trixon.idl.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.SystemHelper;
import se.trixon.idl.shared.Command;
import se.trixon.idl.shared.IddHelper;

/**
 *
 * @author Patrik Karlsson
 */
public final class Client {

    public static final String FRAME_IMAGE_BEG = "::IDD_IMAGE_BEG::";
    public static final String FRAME_IMAGE_END = "::IDD_IMAGE_END::";
    private static final String ENV_IDD_HOST = "IDD_HOST";
    private static final String ENV_IDD_PORT = "IDD_PORT";
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private final HashSet<ConnectionListener> mConnectionListeners = new HashSet<>();
    private String mHost;
    private BufferedReader mIn;
    private PrintStream mOut;
    private int mPort;
    private Socket mSocket;

    public Client(String host, int port) {
        setHost(host);
        setPort(port);

        init();
    }

    public Client(String host, String port) {
        setHost(host);
        setPort(port);

        init();
    }

    public Client() {
        setHost(null);
        setPort(null);

        init();
    }

    public boolean addConnectionListener(ConnectionListener connectionListener) {
        return mConnectionListeners.add(connectionListener);
    }

    public void connect() throws MalformedURLException, SocketException, IOException, UnknownHostException {
        mSocket = new Socket(mHost, mPort);
        mIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
        mOut = new PrintStream(mSocket.getOutputStream());

        mConnectionListeners.stream().forEach((connectionListener) -> {
            connectionListener.onConnectionConnect();
        });
    }

    public void disconnect() {
        try {
            mOut.println("close");
            mOut.close();
            mIn.close();
            mSocket.close();
        } catch (NullPointerException ex) {
            //nvm
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        mConnectionListeners.stream().forEach((connectionListener) -> {
            connectionListener.onConnectionDisconnect();
        });
    }

    public String getHost() {
        return mHost;
    }

    public int getPort() {
        return mPort;
    }

    public boolean isConnected() {
        return mSocket != null && mSocket.isConnected();
    }

    public LinkedList<String> send(Command command, String... strings) throws IOException {
        String cmd = command.name();
        String args = StringUtils.join(strings, " ");

        if (StringUtils.isNotBlank(args)) {
            cmd = String.format("%s %s", cmd, args);
        }

        return send(cmd);
    }

    public LinkedList<String> send(String string) throws IOException {
        mOut.println(string.trim());
        LinkedList<String> lines = new LinkedList<>();
        String responseLine;

        while ((responseLine = mIn.readLine()) != null) {
            lines.add(responseLine);

            if (StringUtils.equals(responseLine, "OK") || StringUtils.startsWith(responseLine, "ACK")) {
                break;
            }
        }

        return lines;
    }

    public void setHost(String aHost) {
        String host;

        if (aHost != null) {
            host = aHost;
        } else if (System.getenv().containsKey(ENV_IDD_HOST)) {
            host = System.getenv(ENV_IDD_HOST);
        } else {
            host = SystemHelper.getHostname();
        }

        mHost = host;
    }

    public void setPort(int port) {
        mPort = port;
    }

    public void setPort(String aPort) {
        int port;
        String portString;

        if (StringUtils.isNotBlank(aPort)) {
            portString = aPort;
        } else if (System.getenv().containsKey(ENV_IDD_PORT)) {
            portString = System.getenv(ENV_IDD_PORT);
        } else {
            portString = String.valueOf(IddHelper.DEFAULT_PORT);
        }

        try {
            port = Integer.valueOf(portString);
        } catch (NumberFormatException e) {
            port = IddHelper.DEFAULT_PORT;
            LOGGER.severe(String.format(IddHelper.getBundle().getString("invalid_port"), portString, IddHelper.DEFAULT_PORT));
        }

        mPort = port;
    }

    private void init() {
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            try {
//                disconnect();
//            } catch (Exception e) {
//            }
//        }));
    }
}
