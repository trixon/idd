/* 
 * Copyright 2022 Patrik Karlström.
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

import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.SystemHelper;
import se.trixon.idl.Command;
import se.trixon.idl.FrameImageCarrier;
import se.trixon.idl.IddHelper;

/**
 *
 * @author Patrik Karlström
 */
public final class Client {

    private static final String ENV_IDD_HOST = "IDD_HOST";
    private static final String ENV_IDD_PORT = "IDD_PORT";
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private final HashSet<ClientListener> mClientListeners = new HashSet<>();
    private BufferedReader mCommandIn;
    private PrintStream mCommandOut;
    private Socket mCommandSocket;
    private BufferedReader mFrameIn;
    private PrintStream mFrameOut;
    private Socket mFrameSocket;
    private String mHost;
    private int mPort;

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

    public boolean addClientListener(ClientListener clientListener) {
        return mClientListeners.add(clientListener);
    }

    public void connect() throws MalformedURLException, SocketException, IOException, UnknownHostException {
        final int timeout = 5000;

        mCommandSocket = new Socket();
        mCommandSocket.connect(new InetSocketAddress(mHost, mPort), timeout);
        mCommandSocket.setSoTimeout(timeout);
        mCommandIn = new BufferedReader(new InputStreamReader(mCommandSocket.getInputStream()));
        mCommandOut = new PrintStream(mCommandSocket.getOutputStream());

        mClientListeners.stream().forEach((clientListener) -> {
            clientListener.onClientConnect();
        });
    }

    public void deregister() throws IOException {
        if (mFrameIn != null) {
            send("deregister");
            mFrameSocket.close();
            mFrameSocket = null;
        }
    }

    public void disconnect() {
        try {
            if (mCommandOut != null) {
                mCommandOut.println("close");
            }
            if (mCommandSocket != null) {
                mCommandSocket.close();
            }
            if (mFrameSocket != null) {
                mFrameSocket.close();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        mClientListeners.stream().forEach((clientListener) -> {
            clientListener.onClientDisconnect();
        });
    }

    public String getHost() {
        return mHost;
    }

    public int getPort() {
        return mPort;
    }

    public boolean isConnected() {
        return mCommandSocket != null && !mCommandSocket.isClosed();
    }

    public boolean isFrameConnected() {
        return mFrameSocket != null && mFrameSocket.isConnected();
    }

    public void register() throws IOException {
        if (mFrameSocket == null) {
            connectFrame();
            sendFrame("register");

            Thread thread = new Thread(() -> {
                String responseLine;
                StringBuilder sb = new StringBuilder();

                try {
                    while ((responseLine = mFrameIn.readLine()) != null) {
                        //System.out.println("RECE >>> " + responseLine);
                        if (StringUtils.equalsIgnoreCase(responseLine, IddHelper.FRAME_IMAGE_BEG)) {
                            sb = new StringBuilder();
                        } else if (!StringUtils.equalsIgnoreCase(responseLine, IddHelper.FRAME_IMAGE_END)) {
                            sb.append(responseLine).append("\n");
                        } else {
                            restoreFrameImageCarrier(sb.toString());
                        }
                    }
                } catch (SocketException ex) {
                    //
                } catch (IOException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            });

            thread.setName(String.format("%s FrameImageReader[%s:%d]",
                    getClass().getSimpleName(),
                    mFrameSocket.getInetAddress(),
                    mFrameSocket.getPort()
            ));

            thread.start();
        }
    }

    public String send(Command command, String... strings) throws IOException {
        String cmd = command.name();
        String args = StringUtils.join(strings, " ");

        if (StringUtils.isNotBlank(args)) {
            cmd = String.format("%s %s", cmd, args);
        }

        return send(cmd);
    }

    public String send(String string) throws IOException {
        return send(mCommandOut, mCommandIn, string);
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

    private void connectFrame() throws MalformedURLException, SocketException, IOException, UnknownHostException {
        mFrameSocket = new Socket(mHost, mPort);
        mFrameIn = new BufferedReader(new InputStreamReader(mFrameSocket.getInputStream()));
        mFrameOut = new PrintStream(mFrameSocket.getOutputStream());

        mClientListeners.stream().forEach((clientListener) -> {
            clientListener.onClientRegister();
        });
    }

    private void init() {
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            try {
//                disconnect();
//            } catch (Exception e) {
//            }
//        }));
    }

    private void restoreFrameImageCarrier(String json) throws JsonSyntaxException, IOException {
        //FileUtils.write(new File("/home/pata/frameImageCarrier.json"), json, "utf-8");
        FrameImageCarrier frameImageCarrier = FrameImageCarrier.fromJson(json);
        mClientListeners.stream().forEach((clientListener) -> {
            clientListener.onClientReceive(frameImageCarrier);
        });
    }

    private String send(PrintStream out, BufferedReader in, String string) throws IOException {
        out.println(string.trim());
        StringBuilder sb = new StringBuilder();
        String responseLine;

        while ((responseLine = in.readLine()) != null) {
            sb.append(responseLine).append("\n");

            if (StringUtils.equals(responseLine, "OK") || StringUtils.startsWith(responseLine, "ACK")) {
                break;
            }
        }

        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    private String sendFrame(String string) throws IOException {
        return send(mFrameOut, mFrameIn, string);
    }
}
