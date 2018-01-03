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
package se.trixon.idl.shared;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlsson
 */
public class IddHelper {

    public static final int DEFAULT_PORT = 5705;
    public static final String OPT_COMMAND = "command";
    public static final String OPT_COMMAND_ONCE = "start-once";
    public static final String OPT_HELP = "help";
    public static final String OPT_HOST = "host";
    public static final String OPT_PORT = "port";
    public static final String OPT_VERBOSE = "verbose";
    public static final String OPT_VERSION = "version";
    public static final String OPT_WAIT = "wait";
    public static final String PROTOCOL_VERSION = "0.0.1";
    private static final ResourceBundle sBundle = SystemHelper.getBundle(IddHelper.class, "Bundle");

    static {
        if (!System.getProperties().containsKey("java.util.logging.SimpleFormatter.format")) {
            System.getProperties().setProperty("java.util.logging.SimpleFormatter.format", "%1$tY-%1$tm-%1$td %1$tH.%1$tM.%1$tS.%1$tL: %5$s%n"
                    + "                         "
                    + "[%4$s] %2$s%n");
        }
    }

    public static void exit() {
        exit(0);
    }

    public static void exit(int status) {
        System.exit(status);
    }

    public static ResourceBundle getBundle() {
        return sBundle;
    }

    public static String millisToDateTime(long timestamp) {
        Date date = new Date(timestamp);
        return new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(date);
    }

    public static String nowToDateTime() {
        return millisToDateTime(System.currentTimeMillis());
    }
}
