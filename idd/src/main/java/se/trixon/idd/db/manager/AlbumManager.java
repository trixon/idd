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
package se.trixon.idd.db.manager;

import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import se.trixon.idl.db.Album;

/**
 *
 * @author Patrik Karlsson
 */
public class AlbumManager extends BaseManager {

    public static final String COL_ID = "album_id";

    private final DbColumn mAlbumRootId;
    private final DbColumn mCaption;
    private final DbColumn mCollection;
    private final Columns mColumns = new Columns();
    private final DbColumn mDate;
    private final DbColumn mIcon;
    private final DbColumn mRelativePath;

    public static AlbumManager getInstance() {
        return Holder.INSTANCE;
    }

    private AlbumManager() {
        mTable = getSchema().addTable("album");

        mId = mTable.addColumn(COL_ID, SQL_IDENTITY, null);
        mAlbumRootId = mTable.addColumn(AlbumRootManager.COL_ID, SQL_BIGINT_NOT_NULL, null);
        mRelativePath = mTable.addColumn("relative_path", SQL_VARCHAR, Integer.MAX_VALUE);
        mDate = mTable.addColumn("date", SQL_DATE, null);
        mCaption = mTable.addColumn("caption", SQL_VARCHAR, Integer.MAX_VALUE);
        mCollection = mTable.addColumn("collection", SQL_VARCHAR, Integer.MAX_VALUE);
        mIcon = mTable.addColumn("icon", SQL_INT, null);

        String indexName;
        BaseManager manager;

        manager = AlbumRootManager.getInstance();
        indexName = getIndexName(new DbColumn[]{manager.getId()}, "fkey");
        mAlbumRootId.references(indexName, manager.getTable(), manager.getId());

        addNotNullConstraints(mRelativePath);
    }

    public Columns columns() {
        return mColumns;
    }

    @Override
    public void create() {
        String indexName = getIndexName(new DbColumn[]{mId}, "pkey");
        DbConstraint primaryKeyConstraint = new DbConstraint(mTable, indexName, Constraint.Type.PRIMARY_KEY, mId);

        indexName = getIndexName(new DbColumn[]{mAlbumRootId, mRelativePath}, "key");
        DbConstraint uniqueKeyConstraint = new DbConstraint(mTable, indexName, Constraint.Type.UNIQUE, mAlbumRootId, mRelativePath);

        mDb.create(mTable, primaryKeyConstraint, uniqueKeyConstraint);
    }

    public Long insert(Album album) throws ClassNotFoundException, SQLException {
        if (mInsertPreparedStatement == null) {
            prepareInsert();
        }

        mInsertPlaceHolders.get(mAlbumRootId).setLong(album.getAlbumRootId(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mCaption).setString(album.getCaption(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mCollection).setString(album.getCollection(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mDate).setObject(album.getDate(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mIcon).setInt(album.getIcon(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mRelativePath).setString(album.getRelativePath(), mInsertPreparedStatement);

        int affectedRows = mInsertPreparedStatement.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Creating album failed, no rows affected.");
        }

        try (ResultSet generatedKeys = mInsertPreparedStatement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            } else {
                throw new SQLException("Creating album failed, no ID obtained.");
            }
        }
    }

    private void prepareInsert() throws SQLException {
        mInsertPlaceHolders.init(
                mAlbumRootId,
                mCaption,
                mCollection,
                mDate,
                mIcon,
                mRelativePath
        );

        InsertQuery insertQuery = new InsertQuery(mTable)
                .addColumn(mAlbumRootId, mInsertPlaceHolders.get(mAlbumRootId))
                .addColumn(mCaption, mInsertPlaceHolders.get(mCaption))
                .addColumn(mCollection, mInsertPlaceHolders.get(mCollection))
                .addColumn(mDate, mInsertPlaceHolders.get(mDate))
                .addColumn(mIcon, mInsertPlaceHolders.get(mIcon))
                .addColumn(mRelativePath, mInsertPlaceHolders.get(mRelativePath))
                .validate();

        String sql = insertQuery.toString();
        mInsertPreparedStatement = mDb.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        //System.out.println(mInsertPreparedStatement.toString());
    }

    public class Columns extends BaseManager.Columns {

        public DbColumn getAlbumRootId() {
            return mAlbumRootId;
        }

        public DbColumn getCaption() {
            return mCaption;
        }

        public DbColumn getCollection() {
            return mCollection;
        }

        public DbColumn getDate() {
            return mDate;
        }

        public DbColumn getIcon() {
            return mIcon;
        }

        public DbColumn getRelativePath() {
            return mRelativePath;
        }
    }

    private static class Holder {

        private static final AlbumManager INSTANCE = new AlbumManager();
    }
}
