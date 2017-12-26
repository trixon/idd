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

    public static final String OPT_COMMAND = "command";
    public static final String OPT_COMMAND_ONCE = "start-once";
    public static final String OPT_HELP = "help";
    public static final String OPT_HOST = "host";
    public static final String OPT_PORT = "port";
    public static final String OPT_VERBOSE = "verbose";
    public static final String OPT_VERSION = "version";
    public static final String OPT_WAIT = "wait";
    public static final int DEFAULT_PORT = 5705;
    public static final int PROTOCOL_VERSION = 1;
    private static final ResourceBundle sBundle = SystemHelper.getBundle(IddHelper.class, "Bundle");

    public static void exit() {
        exit(0);
    }

    public static void exit(int status) {
        System.exit(status);
    }

    public static ResourceBundle getBundle() {
        return sBundle;
    }

    public static String getRmiName(String host, int port, Class c) {
        return String.format("//%s:%d/%s", host, port, c.getCanonicalName());
    }

    public static String millisToDateTime(long timestamp) {
        Date date = new Date(timestamp);
        return new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(date);
    }

    public static String nowToDateTime() {
        return millisToDateTime(System.currentTimeMillis());
    }
}
