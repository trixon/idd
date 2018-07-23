/* 
 * Copyright 2018 Patrik Karlström.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.idl.IddHelper;

/**
 *
 * @author Patrik Karlström
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private final ResourceBundle mBundle = SystemHelper.getBundle(Main.class, "Bundle");
    private final ResourceBundle mBundleLib = IddHelper.getBundle();
    private Options mOptions;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new Main(args);
    }

    public Main(String[] args) {
        initOptions();

        try {
            CommandLineParser commandLineParser = new DefaultParser();
            CommandLine commandLine = commandLineParser.parse(mOptions, args);

            if (commandLine.hasOption(IddHelper.OPT_HELP)) {
                displayHelp();
                System.exit(0);
            } else if (commandLine.hasOption(IddHelper.OPT_VERSION)) {
                displayVersion();
                System.exit(0);
            } else {
                try {
                    Idc idc = new Idc(commandLine);
                    if (!commandLine.hasOption(IddHelper.OPT_WAIT)) {
                        System.exit(0);
                    }
                } catch (MalformedURLException | SocketException ex) {
                    //LOGGER.log(Level.SEVERE, null, ex);
                    System.err.println(mBundleLib.getString("error_not_connected"));
                    System.exit(0);
                } catch (IOException ex) {
                    System.err.println(mBundleLib.getString("error_not_connected"));
                    System.exit(0);
                }
            }
        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
            System.out.println(mBundle.getString("parse_help"));
        }
    }

    private void displayHelp() {
        PrintStream defaultStdOut = System.out;
        StringBuilder sb = new StringBuilder().append(mBundle.getString("usage")).append("\n\n");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        System.setOut(ps);

        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(79);
        formatter.setOptionComparator(null);
        formatter.printHelp("xxx", mOptions, false);
        System.out.flush();
        System.setOut(defaultStdOut);
        sb.append(baos.toString().replace("usage: xxx" + System.lineSeparator(), "")).append("\n")
                .append(mBundleLib.getString("help_footer"));

        System.out.println(sb.toString());
    }

    private void displayVersion() {
        System.out.println(String.format(mBundle.getString("version_info"), SystemHelper.getJarVersion(this.getClass())));
    }

    private void initOptions() {
        String hostString = Dict.HOST.toString().toLowerCase();
        String portString = Dict.PORT.toString().toLowerCase();

        Option help = Option.builder("?")
                .longOpt(IddHelper.OPT_HELP)
                .desc(mBundleLib.getString("opt_help_desc"))
                .build();

        Option version = Option.builder("v")
                .longOpt(IddHelper.OPT_VERSION)
                .desc(mBundleLib.getString("opt_version_desc"))
                .build();

        Option host = Option.builder("h")
                .longOpt(IddHelper.OPT_HOST)
                .argName(hostString)
                .hasArg(true)
                .desc(mBundleLib.getString("opt_host_desc"))
                .build();

        Option portHost = Option.builder("p")
                .longOpt(IddHelper.OPT_PORT)
                .argName(portString)
                .hasArg(true)
                .desc(mBundleLib.getString("opt_port_desc"))
                .build();

        Option wait = Option.builder("w")
                .longOpt(IddHelper.OPT_WAIT)
                .desc(mBundleLib.getString("opt_wait_desc")).build();

        mOptions = new Options();
        mOptions.addOption(host);
        mOptions.addOption(portHost);
        mOptions.addOption(wait);
        mOptions.addOption(help);
        mOptions.addOption(version);
    }
}
