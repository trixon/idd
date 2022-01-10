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
package se.trixon.idd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import se.trixon.almond.util.SystemHelper;
import se.trixon.idd.db.Db;
import se.trixon.idl.IddHelper;

/**
 *
 * @author Patrik Karlström
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private final ResourceBundle mBundle = SystemHelper.getBundle(Main.class, "Bundle");
    private final ResourceBundle mBundleLib = IddHelper.getBundle();
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
            var commandLineParser = new DefaultParser();
            var commandLine = commandLineParser.parse(mOptions, args);
            mConfig.setVerbose(commandLine.hasOption(IddHelper.OPT_VERBOSE));

            if (commandLine.hasOption(IddHelper.OPT_HELP)) {
                displayHelp();
                System.exit(0);
            } else if (commandLine.hasOption(IddHelper.OPT_VERSION)) {
                displayVersion();
                System.exit(0);
            } else {
                String filename = commandLine.getArgs().length > 0 ? commandLine.getArgs()[0] : null;
                if (mConfig.load(filename)) {
                    LOGGER.info(Db.getInstance().getConnString());
                    org.h2.tools.Server.createTcpServer("-tcpAllowOthers", "-ifNotExists").start();
                    if (Db.getInstance().getAutoCommitConnection() == null) {
                        LOGGER.info("Shutting down");
                        System.exit(1);
                    } else {
                        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                            try {
                                Db.getInstance().getConnection().close();
                            } catch (NullPointerException | SQLException ex) {
                                //nvm
                            }
                            try {
                                Db.getInstance().getAutoCommitConnection().close();
                            } catch (NullPointerException | SQLException ex) {
                                //nvm
                            }
                        }));

                        ImageServer.enterLoop();
                        //Unreachable statement
                    }
                }
            }
        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
            System.out.println(mBundle.getString("parse_help"));
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void displayHelp() {
        var defaultStdOut = System.out;
        var sb = new StringBuilder().append(mBundle.getString("usage")).append("\n\n");

        var baos = new ByteArrayOutputStream();
        var ps = new PrintStream(baos);
        System.setOut(ps);

        var helpFormatter = new HelpFormatter();
        helpFormatter.setWidth(79);
        helpFormatter.setOptionComparator(null);
        helpFormatter.printHelp("xxx", mOptions, false);
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
        var helpOption = Option.builder("h")
                .longOpt(IddHelper.OPT_HELP)
                .desc(mBundleLib.getString("opt_help_desc"))
                .build();

        var versionOption = Option.builder("v")
                .longOpt(IddHelper.OPT_VERSION)
                .desc(mBundleLib.getString("opt_version_desc"))
                .build();

        var verboseOption = Option.builder("v")
                .longOpt(IddHelper.OPT_VERBOSE)
                .desc(mBundleLib.getString("opt_verbose_desc"))
                .build();

        mOptions = new Options();

//        mOptions.addOption(verboseOption);
        mOptions.addOption(helpOption);
        mOptions.addOption(versionOption);
    }
}
