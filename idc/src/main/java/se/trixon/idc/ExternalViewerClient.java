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
package se.trixon.idc;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import se.trixon.idl.FrameImageCarrier;
import se.trixon.idl.IddHelper;
import se.trixon.idl.client.Client;
import se.trixon.idl.client.ClientListener;

/**
 *
 * @author Patrik Karlström
 */
public class ExternalViewerClient implements ClientListener {

    private static final Logger LOGGER = Logger.getLogger(ExternalViewerClient.class.getName());

    private final Client mClient;
    private final ArrayList<String> mCommand;
    private Process mCurrentProcess;
    private final File mFile;
    private final ProcessBuilder mProcessBuilder;
    private final boolean mStartOnce;

    public ExternalViewerClient(CommandLine commandLine) throws MalformedURLException, SocketException, IOException {
        mClient = new Client(commandLine.getOptionValue(IddHelper.OPT_HOST), commandLine.getOptionValue(IddHelper.OPT_PORT));
        mClient.addClientListener(this);
        mClient.connect();
        mClient.register();

        mStartOnce = commandLine.hasOption(IddHelper.OPT_EXT_VIEW_SINGLE);
        mFile = File.createTempFile("idfb", null);
        mFile.deleteOnExit();

        String extCommand;
        if (commandLine.hasOption(IddHelper.OPT_EXT_VIEW_SINGLE)) {
            extCommand = commandLine.getOptionValue(IddHelper.OPT_EXT_VIEW_SINGLE);
        } else {
            extCommand = commandLine.getOptionValue(IddHelper.OPT_EXT_VIEW_REPEAT);
        }

        mCommand = new ArrayList<>(Arrays.asList(extCommand.split("\\s")));
        mCommand.add(mFile.getAbsolutePath());

        System.out.println("command:");
        mCommand.forEach((string) -> {
            System.err.println(string);
        });

        mProcessBuilder = new ProcessBuilder(mCommand);
    }

    @Override
    public void onClientConnect() {
    }

    @Override
    public void onClientDisconnect() {
    }

    @Override
    public void onClientReceive(FrameImageCarrier frameImageCarrier) {
        try {
            frameImageCarrier.save(mFile);

//            if (mCurrentProcess != null) {
//                mCurrentProcess.destroy();
//            }
            if (mStartOnce) {
                if (mCurrentProcess == null) {
                    new Thread(() -> {
                        try {
                            mCurrentProcess = mProcessBuilder.start();
                            mCurrentProcess.waitFor();
                            IddHelper.exit();
                        } catch (IOException | InterruptedException ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                        }
                    }).start();
                }
            } else {
                mCurrentProcess = mProcessBuilder.start();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onClientRegister() {
    }
}
