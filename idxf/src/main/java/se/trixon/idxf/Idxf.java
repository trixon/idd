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
package se.trixon.idxf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import se.trixon.idl.client.Client;
import se.trixon.idl.shared.IddHelper;
import se.trixon.idl.shared.ImageServerEvent;
import se.trixon.idl.shared.ImageServerEventRelay;
import se.trixon.idl.shared.ProcessEvent;

/**
 *
 * @author Patrik Karlsson
 */
public class Idxf implements ImageServerEventRelay {

    private final Client mClient;
    private final ArrayList<String> mCommand;
    private Process mCurrentProcess;
    private final File mFile;
    private final ProcessBuilder mProcessBuilder;
    private final boolean mStartOnce;

    public Idxf(CommandLine cmd) throws MalformedURLException, SocketException, IOException {
        mClient = new Client(cmd.getOptionValue(IddHelper.OPT_HOST), cmd.getOptionValue(IddHelper.OPT_PORT));
        mClient.connect();
        mClient.addImageServerEventRelay(this);
        mStartOnce = cmd.hasOption(IddHelper.OPT_COMMAND_ONCE);
        mFile = File.createTempFile("idfb", null);
        mFile.deleteOnExit();

        mCommand = new ArrayList(Arrays.asList(cmd.getOptionValue(IddHelper.OPT_COMMAND).split("\\s")));
        mCommand.add(mFile.getAbsolutePath());

        System.out.println("command:");
        mCommand.forEach((string) -> {
            System.err.println(string);
        });

        mProcessBuilder = new ProcessBuilder(mCommand);
    }

    @Override
    public void onExecutorEvent(String command, String... strings) {
    }

    @Override
    public void onProcessEvent(ProcessEvent processEvent, Object object) {
    }

    @Override
    public void onReceiveStreamEvent(InputStream inputStream) {
        try {
            FileUtils.copyInputStreamToFile(inputStream, mFile);
            inputStream.close();

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
                            Logger.getLogger(Idxf.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }).start();
                }
            } else {
                mCurrentProcess = mProcessBuilder.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(Idxf.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onServerEvent(ImageServerEvent imageServerEvent) {
        if (imageServerEvent == ImageServerEvent.SHUTDOWN) {
            System.err.println(imageServerEvent);
            IddHelper.exit(1);
        }
    }
}
