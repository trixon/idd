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
package se.trixon.idd.db;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.trixon.idd.db.manager.AlbumManager;
import se.trixon.idd.db.manager.AlbumRootManager;
import se.trixon.idd.db.manager.BaseManager;
import se.trixon.idd.db.manager.ImageInformationManager;
import se.trixon.idd.db.manager.ImageManager;
import se.trixon.idd.db.manager.ImageMetadataManager;
import se.trixon.idd.db.manager.ImagePositionManager;

/**
 *
 * @author Patrik Karlström
 */
public class DbCreator {

    private final Db mDb = Db.getInstance();

    public static DbCreator getInstance() {
        return Holder.INSTANCE;
    }

    private DbCreator() {
    }

    private void init(BaseManager manager) {
        try {
            mDb.drop(manager.getTable(), true);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(DbCreator.class.getName()).log(Level.SEVERE, null, ex);
        }

        manager.create();
    }

    public void initDb() {
        init(AlbumRootManager.getInstance());
        init(AlbumManager.getInstance());
        init(ImageManager.getInstance());
        init(ImageInformationManager.getInstance());
        init(ImageMetadataManager.getInstance());
        init(ImagePositionManager.getInstance());
    }

    private static class Holder {

        private static final DbCreator INSTANCE = new DbCreator();
    }
}
