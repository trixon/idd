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

import com.healthmarketscience.sqlbuilder.CustomSql;
import com.healthmarketscience.sqlbuilder.FunctionCall;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.custom.postgresql.PgLimitClause;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.trixon.idd.db.Db;
import se.trixon.idd.db.manager.AlbumManager;
import se.trixon.idd.db.manager.AlbumRootManager;
import se.trixon.idd.db.manager.ImageManager;

/**
 *
 * @author Patrik Karlsson
 */
public class Querator {

    private final AlbumManager mAlbumManager;
    private final AlbumRootManager mAlbumRootManager;
    private final DbTable mAlbumRootTable;
    private final DbTable mAlbumTable;
    private final Db mDb;

    private final ImageManager mImageManager;
    private final DbTable mImageTable;

    public static Querator getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new Querator();
    }

    private Querator() {
        Config config = Config.getInstance();
        config.load(null);
        mDb = Db.getInstance();
        mAlbumManager = AlbumManager.getInstance();
        mAlbumRootManager = AlbumRootManager.getInstance();
        mImageManager = ImageManager.getInstance();

        mImageTable = mImageManager.getTable();
        mAlbumTable = mAlbumManager.getTable();
        mAlbumRootTable = mAlbumRootManager.getTable();
    }

    public String getRandomPath() {
        FunctionCall randomFunctionCall = new FunctionCall("random");
        SelectQuery selectQuery = new SelectQuery()
                .addJoin(SelectQuery.JoinType.INNER, mImageTable, mAlbumTable, mImageManager.getAlbumId(), mAlbumManager.getId())
                .addJoin(SelectQuery.JoinType.INNER, mAlbumTable, mAlbumRootTable, mAlbumManager.getAlbumRootId(), mAlbumRootManager.getId())
                .addColumns(mAlbumRootManager.getSpecificPath())
                .addColumns(mAlbumManager.getRelativePath())
                .addColumns(mImageManager.getName())
                .addCustomColumns(randomFunctionCall)
                .addCustomOrderings(new CustomSql("random()"))
                .addCustomization(new PgLimitClause(1))
                .validate();

        String sql = selectQuery.toString();
        //System.out.println(sql);

        String path = null;
        try (Statement statement = mDb.getAutoCommitConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            ResultSet rs = statement.executeQuery(sql);
            rs.first();
            path = String.format("%s%s/%s",
                    rs.getString(1),
                    rs.getString(2),
                    rs.getString(3)
            );
        } catch (SQLException ex) {
            Logger.getLogger(Querator.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println(path);
        return path;
    }

    private static class Holder {

        private static final Querator INSTANCE = new Querator();
    }
}
