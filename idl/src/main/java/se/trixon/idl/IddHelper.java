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
package se.trixon.idl;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import se.trixon.almond.util.StringHelper;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
public class IddHelper {

    public static final int DEFAULT_PORT = 5705;
    public static final String FRAME_IMAGE_BEG = "::FRAME_IMAGE_BEG::";
    public static final String FRAME_IMAGE_END = "::FRAME_IMAGE_END::";
    public static final String OPT_EXT_VIEW_REPEAT = "ext-view-repeat";
    public static final String OPT_EXT_VIEW_SINGLE = "ext-view-single";
    public static final String OPT_HELP = "help";
    public static final String OPT_HOST = "host";
    public static final String OPT_PORT = "port";
    public static final String OPT_VERBOSE = "verbose";
    public static final String OPT_VERSION = "version";
    public static final String OPT_WAIT = "wait";
    public static final String PROTOCOL_VERSION = "0.0.1";

    private static final Logger LOGGER = Logger.getLogger(IddHelper.class.getName());
    private static final ResourceBundle sBundle = SystemHelper.getBundle(IddHelper.class, "Bundle");
    private static MessageDigest sMessageDigest;
    private static final SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");

    static {
        if (!System.getProperties().containsKey("java.util.logging.SimpleFormatter.format")) {
            System.getProperties().setProperty("java.util.logging.SimpleFormatter.format", "%1$tY-%1$tm-%1$td %1$tH.%1$tM.%1$tS.%1$tL: %5$s%n"
                    + "                         "
                    + "[%4$s] %2$s%n");
        }

        try {
            sMessageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
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

    public static String getMd5(File file) {
        String md5 = null;

        try {
            md5 = getMd5(FileUtils.readFileToByteArray(file));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        return md5;
    }

    public static String getMd5(byte[] bytes) {
        return StringHelper.bytesToHex(sMessageDigest.digest(bytes));
    }

    public static String millisToDateTime(long timestamp) {
        return sSimpleDateFormat.format(new Date(timestamp));
    }

    public static String nowToDateTime() {
        return millisToDateTime(System.currentTimeMillis());
    }
}
