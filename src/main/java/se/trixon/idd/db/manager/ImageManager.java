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
import se.trixon.idl.shared.db.Image;

/**
 *
 * @author Patrik Karlsson
 */
public class ImageManager extends BaseManager {

    public static final String COL_CATEGORY = "category";
    public static final String COL_FILE_SIZE = "file_size";
    public static final String COL_ID = "image_id";
    public static final String COL_MODIFICATION_DATE = "modification_date";
    public static final String COL_NAME = "name";
    public static final String COL_STATUS = "status";
    public static final String COL_UNIQUE_HASH = "unique_hash";
    public static final String TABLE_NAME = "image";
    private final DbColumn mAlbumId;
    private PlaceHolder mAlbumIdPlaceHolder;
    private final DbColumn mCategory;
    private PlaceHolder mCategoryPlaceHolder;
    private final DbColumn mFileSize;
    private PlaceHolder mFileSizePlaceHolder;
    private final DbColumn mModificationDate;
    private PlaceHolder mModificationDatePlaceHolder;
    private final DbColumn mName;
    private PlaceHolder mNamePlaceHolder;
    private final DbColumn mStatus;
    private PlaceHolder mStatusPlaceHolder;
    private final DbColumn mUniqueHash;
    private PlaceHolder mUniqueHashPlaceHolder;

    public static ImageManager getInstance() {
        return Holder.INSTANCE;
    }

    private ImageManager() {
        mTable = getSchema().addTable(TABLE_NAME);

        mId = mTable.addColumn(COL_ID, "IDENTITY", null);
        mAlbumId = mTable.addColumn(AlbumManager.COL_ID, "BIGINT", null);
        mName = mTable.addColumn(COL_NAME, "VARCHAR", Integer.MAX_VALUE);
        mStatus = mTable.addColumn(COL_STATUS, "INT", null);
        mCategory = mTable.addColumn(COL_CATEGORY, "INT", null);
        mModificationDate = mTable.addColumn(COL_MODIFICATION_DATE, "TIMESTAMP", null);
        mFileSize = mTable.addColumn(COL_FILE_SIZE, "BIGINT", null);
        mUniqueHash = mTable.addColumn(COL_UNIQUE_HASH, "VARCHAR", 32);

        addNotNullConstraint(mAlbumId);
        addNotNullConstraint(mName);
        addNotNullConstraint(mStatus);
        addNotNullConstraint(mCategory);

        String indexName;
        BaseManager manager;

        manager = AlbumManager.getInstance();
        indexName = getIndexName(new DbColumn[]{manager.getId()}, "fkey");
        mAlbumId.references(indexName, manager.getTable(), manager.getId());
    }

    @Override
    public void create() {
        String indexName;
        indexName = getIndexName(new DbColumn[]{mId}, "pkey");
        DbConstraint primaryKeyConstraint = new DbConstraint(mTable, indexName, Constraint.Type.PRIMARY_KEY, mId);

        indexName = getIndexName(new DbColumn[]{mAlbumId, mName}, "key");
        DbConstraint uniqueKeyConstraint = new DbConstraint(mTable, indexName, Constraint.Type.UNIQUE, mAlbumId, mName);

        mDb.create(mTable, primaryKeyConstraint, uniqueKeyConstraint);
    }

    public long insert(Image image) throws ClassNotFoundException, SQLException {
        mAlbumIdPlaceHolder.setLong(image.getAlbumId(), mInsertPreparedStatement);
        mCategoryPlaceHolder.setInt(image.getCategory(), mInsertPreparedStatement);
        mFileSizePlaceHolder.setLong(image.getFileSize(), mInsertPreparedStatement);
        mModificationDatePlaceHolder.setObject(image.getModificationDate(), mInsertPreparedStatement);
        mNamePlaceHolder.setString(image.getName(), mInsertPreparedStatement);
        mStatusPlaceHolder.setInt(image.getStatus(), mInsertPreparedStatement);
        mUniqueHashPlaceHolder.setString(image.getUniqueHash(), mInsertPreparedStatement);

        int affectedRows = mInsertPreparedStatement.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Creating image failed, no rows affected.");
        }

        try (ResultSet generatedKeys = mInsertPreparedStatement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                long imageId = generatedKeys.getLong(1);

                if (image.getPosition().hasData()) {
                    image.getPosition().setId(imageId);
                    ImagePositionManager.getInstance().insert(image.getPosition());
                }

                if (image.getInformation().hasData()) {
                    image.getInformation().setId(imageId);
                    ImageInformationManager.getInstance().insert(image.getInformation());
                }

                if (image.getMetadata().hasData()) {
                    image.getMetadata().setId(imageId);
                    ImageMetadataManager.getInstance().insert(image.getMetadata());
                }
                return imageId;
            } else {
                throw new SQLException("Creating image failed, no ID obtained.");
            }
        }
    }

    @Override
    public void prepare() throws SQLException {
        QueryPreparer preparer = new QueryPreparer();

        mIdPlaceHolder = preparer.getNewPlaceHolder();
        mAlbumIdPlaceHolder = preparer.getNewPlaceHolder();
        mNamePlaceHolder = preparer.getNewPlaceHolder();
        mStatusPlaceHolder = preparer.getNewPlaceHolder();
        mCategoryPlaceHolder = preparer.getNewPlaceHolder();
        mModificationDatePlaceHolder = preparer.getNewPlaceHolder();
        mFileSizePlaceHolder = preparer.getNewPlaceHolder();
        mUniqueHashPlaceHolder = preparer.getNewPlaceHolder();

        InsertQuery insertQuery = new InsertQuery(mTable)
                .addColumn(mAlbumId, mAlbumIdPlaceHolder)
                .addColumn(mCategory, mCategoryPlaceHolder)
                .addColumn(mFileSize, mFileSizePlaceHolder)
                .addColumn(mModificationDate, mModificationDatePlaceHolder)
                .addColumn(mName, mNamePlaceHolder)
                .addColumn(mStatus, mStatusPlaceHolder)
                .addColumn(mUniqueHash, mUniqueHashPlaceHolder)
                .validate();

        String sql = insertQuery.toString();
        mInsertPreparedStatement = mDb.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        //System.out.println(mInsertPreparedStatement.toString());
    }

    private static class Holder {

        private static final ImageManager INSTANCE = new ImageManager();
    }
}
