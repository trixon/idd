/*
 * Copyright 2016 Patrik Karlsson.
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

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.trixon.idd.db.Db;
import se.trixon.idd.db.DbCreator;
import se.trixon.idd.db.FileVisitor;
import se.trixon.idl.shared.Commands;
import se.trixon.util.SystemHelper;
import se.trixon.util.Xlog;

/**
 *
 * @author Patrik Karlsson
 */
public class Executor {

    private final String[] mArgs;
    private final String mCommand;
    private final Config mConfig = Config.getInstance();
    private final Db mDb = Db.getInstance();

    public Executor(String command, String... args) {
        mCommand = command;
        mArgs = args;
    }

    private void update() {
        try {
            mDb.connectionOpen();
            mDb.dropAllObjects();
            DbCreator.getInstance().initDb();
            EnumSet<FileVisitOption> fileVisitOptions = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
            FileVisitor fileVisitor = new FileVisitor();
            Files.walkFileTree(mConfig.getImageDirectory().toPath(), fileVisitOptions, Integer.MAX_VALUE, fileVisitor);
            mDb.connectionCommit();
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Executor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Executor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    String execute() {
        Xlog.timedOut(String.format("execute: %s", mCommand));
        String result = null;

        switch (mCommand) {
            case Commands.STATS:
                result = "stats";
                break;
            case Commands.UPDATE:
                update();
                result = "update";
                break;
            case Commands.VERSION:
                result = String.format("idd version: %s", SystemHelper.getJarVersion(getClass()));
                break;

        }

        return result;
    }

}
