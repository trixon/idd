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
package se.trixon.idc;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.Locale;
import org.apache.commons.cli.CommandLine;
import se.trixon.idl.client.Client;
import se.trixon.idl.shared.Command;
import se.trixon.idl.shared.IddHelper;
import se.trixon.idl.shared.ImageServerEvent;
import se.trixon.idl.shared.ImageServerEventRelay;
import se.trixon.idl.shared.ProcessEvent;

/**
 *
 * @author Patrik Karlsson
 */
public class Idc implements ImageServerEventRelay {

    private final Client mClient;

    public Idc(CommandLine cmd) throws MalformedURLException, SocketException, IOException {
        String command = parseCommand(cmd);
        mClient = new Client(cmd.getOptionValue(IddHelper.OPT_HOST), cmd.getOptionValue(IddHelper.OPT_PORT));
        mClient.connect();
        if (command != null) {
//            mManager.connect(cmd.getOptionValue(Main.OPT_HOST), cmd.getOptionValue(Main.OPT_PORT));
//            mManager.addImageServerEventRelay(this);
//            mManager.getImageServerCommander().execute(command, null);
//            mManager.disconnect();
        }
    }

    @Override
    public void onExecutorEvent(String command, String... strings) {
        System.out.println(strings.length > 0 ? strings[0] : "");
        IddHelper.exit(0);
    }

    @Override
    public void onProcessEvent(ProcessEvent processEvent, Object object) {
        System.out.println("onProcessEvent");
        System.out.println(processEvent);
        System.out.println(object);
    }

    @Override
    public void onReceiveStreamEvent(InputStream inputStream) {
        // nvm
    }

    @Override
    public void onServerEvent(ImageServerEvent imageServerEvent) {
        System.out.println("onServerEvent");
        System.out.println(imageServerEvent);

        if (imageServerEvent == ImageServerEvent.SHUTDOWN) {
            IddHelper.exit(1);
        }
    }

    private String parseCommand(CommandLine cmd) {
        String result = null;
        if (cmd.getArgList().isEmpty()) {
            System.err.println("no command given");
            IddHelper.exit();
        } else {
            String c = cmd.getArgList().get(0);
            int hitCounter = 0;
            for (String command : Command.getSet()) {
                if (command.equalsIgnoreCase(c)) {
                    result = command;
                    hitCounter = 1;
                    break;
                } else if (command.toLowerCase(Locale.ROOT).startsWith(c.toLowerCase(Locale.ROOT))) {
                    hitCounter++;
                    result = command;
                }
            }

            if (hitCounter == 0) {
                System.err.format("unknown command: %s\n", c);
            } else if (hitCounter > 1) {
                System.err.format("ambiguous command: %s\n", c);
                result = null;
            }
        }

        return result;
    }
}
