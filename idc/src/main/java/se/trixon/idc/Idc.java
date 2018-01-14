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
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TreeSet;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import se.trixon.idl.client.Client;
import se.trixon.idl.Command;
import se.trixon.idl.IddHelper;

/**
 *
 * @author Patrik Karlsson
 */
public class Idc {

    private static final Logger LOGGER = Logger.getLogger(Idc.class.getName());
    private Client mClient;

    public Idc(CommandLine cmd) throws MalformedURLException, SocketException, IOException {
        String command = parseCommand(cmd);

        if (command != null) {
            mClient = new Client(cmd.getOptionValue(IddHelper.OPT_HOST), cmd.getOptionValue(IddHelper.OPT_PORT));
            mClient.connect();

            LinkedList<String> lines = mClient.send(command);
            lines.forEach((line) -> {
                System.out.println(line);
            });
            mClient.disconnect();
        }
    }

    private String parseCommand(CommandLine cmd) {
        String result = null;

        if (cmd.getArgList().isEmpty()) {
            System.err.println("no command given");
            for (String command : Command.getSet()) {
                System.err.format(" - %s\n", command);
            }

            IddHelper.exit();
        } else {
            String c = cmd.getArgList().get(0);
            int hitCounter = 0;
            TreeSet<String> ambiguousCommands = new TreeSet<>();
            for (String command : Command.getSet()) {
                if (command.equalsIgnoreCase(c)) {
                    result = command;
                    hitCounter = 1;
                    break;
                } else if (command.toLowerCase(Locale.ROOT).startsWith(c.toLowerCase(Locale.ROOT))) {
                    hitCounter++;
                    result = command;
                    ambiguousCommands.add(command);
                }
            }

            if (hitCounter == 0) {
                System.err.format("unknown command: %s\n", c);
            } else if (hitCounter > 1) {
                System.err.format("ambiguous command: %s\n", c);
                for (String ambiguousCommand : ambiguousCommands) {
                    System.err.format(" - %s\n", ambiguousCommand);
                }

                result = null;
            }
        }

        return result;
    }
}
