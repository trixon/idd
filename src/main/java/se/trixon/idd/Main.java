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
package se.trixon.idd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.almond.util.SystemHelper;
import se.trixon.idd.db.Db;
import se.trixon.idl.shared.IddHelper;

/**
 *
 * @author Patrik Karlsson
 */
public class Main {

    public static final String HELP = "help";
    public static final String VERBOSE = "verbose";
    public static final String VERSION = "version";

    private final ResourceBundle mBundle = SystemHelper.getBundle(Main.class, "Bundle");
    private final Config mConfig = Config.getInstance();
    private Options mOptions;

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        new Main(args);
    }

    public Main(String[] args) throws IOException {
        initOptions();

        try {
            CommandLineParser commandLineParser = new DefaultParser();
            CommandLine commandLine = commandLineParser.parse(mOptions, args);
            mConfig.setVerbose(commandLine.hasOption(VERBOSE));

            if (commandLine.hasOption(HELP)) {
                displayHelp();
                System.exit(0);
            } else if (commandLine.hasOption(VERSION)) {
                displayVersion();
                System.exit(0);
            } else {
                String filename = commandLine.getArgs().length > 0 ? commandLine.getArgs()[0] : null;
                if (mConfig.load(filename)) {
                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        @Override
                        public void run() {
                            try {
                                Db.getInstance().getConnection().close();
                            } catch (SQLException ex) {
                                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });

                    ImageServer imageServer = new ImageServer();
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
        sb.append(baos.toString().replace("usage: xxx" + SystemUtils.LINE_SEPARATOR, "")).append("\n")
                .append(IddHelper.getBundle().getString("help_footer"));

        System.out.println(sb.toString());
    }

    private void displayVersion() {
        System.out.println(String.format(mBundle.getString("version_info"), SystemHelper.getJarVersion(this.getClass())));
    }

    private void initOptions() {
        Option help = Option.builder("h")
                .longOpt(HELP)
                .desc(mBundle.getString("opt_help_desc"))
                .build();

        Option version = Option.builder("v")
                .longOpt(VERSION)
                .desc(mBundle.getString("opt_version_desc"))
                .build();

        Option verbose = Option.builder("v")
                .longOpt(VERBOSE)
                .desc(mBundle.getString("opt_verbose_desc"))
                .build();

        mOptions = new Options();

//        mOptions.addOption(verbose);
        mOptions.addOption(help);
        mOptions.addOption(version);
    }
}
