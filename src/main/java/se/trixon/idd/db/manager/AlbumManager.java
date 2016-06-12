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
package se.trixon.idd.db.manager;

import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.QueryPreparer;
import com.healthmarketscience.sqlbuilder.QueryPreparer.PlaceHolder;
import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.trixon.idl.shared.db.Album;

/**
 *
 * @author Patrik Karlsson
 */
public class AlbumManager extends BaseManager {

    public static final String COL_CAPTION = "caption";
    public static final String COL_COLLECTION = "collection";
    public static final String COL_DATE = "date";
    public static final String COL_ICON = "icon";
    public static final String COL_ID = "album_id";
    public static final String COL_RELATIVE_PATH = "relative_path";
    public static final String TABLE_NAME = "album";
    private final DbColumn mAlbumRootId;
    private final PlaceHolder mAlbumRootIdPlaceHolder;
    private final DbColumn mCaption;
    private final PlaceHolder mCaptionPlaceHolder;
    private final DbColumn mCollection;
    private final PlaceHolder mCollectionPlaceHolder;
    private final DbColumn mDate;
    private final PlaceHolder mDatePlaceHolder;
    private final DbColumn mIcon;
    private final PlaceHolder mIconPlaceHolder;
    private final DbColumn mRelativePath;
    private final PlaceHolder mRelativePathPlaceHolder;

    public static AlbumManager getInstance() {
        return Holder.INSTANCE;
    }

    private AlbumManager() {
        mTable = getSchema().addTable(TABLE_NAME);

        mId = mTable.addColumn(COL_ID, "IDENTITY", null);
        mAlbumRootId = mTable.addColumn(AlbumRootManager.COL_ID, "BIGINT", null);
        mRelativePath = mTable.addColumn(COL_RELATIVE_PATH, "VARCHAR", Integer.MAX_VALUE);
        mDate = mTable.addColumn(COL_DATE, "DATE", null);
        mCaption = mTable.addColumn(COL_CAPTION, "VARCHAR", Integer.MAX_VALUE);
        mCollection = mTable.addColumn(COL_COLLECTION, "VARCHAR", Integer.MAX_VALUE);
        mIcon = mTable.addColumn(COL_ICON, "INT", null);

        addNotNullConstraint(mAlbumRootId);
        addNotNullConstraint(mRelativePath);

        String indexName;
        BaseManager manager;

        manager = AlbumRootManager.getInstance();
        indexName = getIndexName(new DbColumn[]{manager.getId()}, "fkey");
        mAlbumRootId.references(indexName, manager.getTable(), manager.getId());

        QueryPreparer preparer = new QueryPreparer();

        mIdPlaceHolder = preparer.getNewPlaceHolder();
        mAlbumRootIdPlaceHolder = preparer.getNewPlaceHolder();
        mRelativePathPlaceHolder = preparer.getNewPlaceHolder();
        mDatePlaceHolder = preparer.getNewPlaceHolder();
        mCaptionPlaceHolder = preparer.getNewPlaceHolder();
        mCollectionPlaceHolder = preparer.getNewPlaceHolder();
        mIconPlaceHolder = preparer.getNewPlaceHolder();
    }

    @Override
    public void create() {
        String indexName = getIndexName(new DbColumn[]{mId}, "pkey");
        DbConstraint primaryKeyConstraint = new DbConstraint(mTable, indexName, Constraint.Type.PRIMARY_KEY, mId);

        indexName = getIndexName(new DbColumn[]{mAlbumRootId, mRelativePath}, "key");
        DbConstraint uniqueKeyConstraint = new DbConstraint(mTable, indexName, Constraint.Type.UNIQUE, mAlbumRootId, mRelativePath);
        mDb.create(mTable, primaryKeyConstraint, uniqueKeyConstraint);
    }

    public long insert(Album album) throws ClassNotFoundException, SQLException {
        if (mInsertPreparedStatement == null) {
            InsertQuery insertQuery = new InsertQuery(mTable)
                    .addColumn(mAlbumRootId, mAlbumRootIdPlaceHolder)
                    .addColumn(mCaption, mCaptionPlaceHolder)
                    .addColumn(mCollection, mCollectionPlaceHolder)
                    .addColumn(mDate, mDatePlaceHolder)
                    .addColumn(mIcon, mIconPlaceHolder)
                    .addColumn(mRelativePath, mRelativePathPlaceHolder)
                    .validate();

            String sql = insertQuery.toString();
            try {
                mInsertPreparedStatement = mDb.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                //System.out.println(mInsertPreparedStatement.toString());
            } catch (SQLException ex) {
                Logger.getLogger(ImagePositionManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        mAlbumRootIdPlaceHolder.setLong(album.getAlbumRootId(), mInsertPreparedStatement);
        mCaptionPlaceHolder.setString(album.getCaption(), mInsertPreparedStatement);
        mCollectionPlaceHolder.setString(album.getCollection(), mInsertPreparedStatement);
        mDatePlaceHolder.setObject(album.getDate(), mInsertPreparedStatement);
        mIconPlaceHolder.setInt(album.getIcon(), mInsertPreparedStatement);
        mRelativePathPlaceHolder.setString(album.getRelativePath(), mInsertPreparedStatement);

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

    private static class Holder {

        private static final AlbumManager INSTANCE = new AlbumManager();
    }
}
