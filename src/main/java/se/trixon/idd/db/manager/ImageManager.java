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
import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;
import java.sql.Connection;
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
    private final DbColumn mCategory;
    private final DbColumn mFileSize;
    private final DbColumn mModificationDate;
    private final DbColumn mName;
    private final DbColumn mStatus;
    private final DbColumn mUniqueHash;

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
        String indexName = getIndexName(new DbColumn[]{mId}, "pkey");
        DbConstraint primaryKeyConstraint = new DbConstraint(mTable, indexName, Constraint.Type.PRIMARY_KEY, mId);

        indexName = getIndexName(new DbColumn[]{mAlbumId, mName}, "key");
        DbConstraint uniqueKeyConstraint = new DbConstraint(mTable, indexName, Constraint.Type.UNIQUE, mAlbumId, mName);

        mDb.create(mTable, primaryKeyConstraint, uniqueKeyConstraint);
    }

    public long insert(Image image) throws ClassNotFoundException, SQLException {
        InsertQuery insertQuery = new InsertQuery(mTable)
                .addColumn(mAlbumId, image.getAlbumId())
                .addColumn(mCategory, image.getCategory())
                .addColumn(mFileSize, image.getFileSize())
                .addColumn(mModificationDate, image.getModificationDate())
                .addColumn(mName, image.getName())
                .addColumn(mStatus, image.getStatus())
                .addColumn(mUniqueHash, image.getUniqueHash())
                .validate();

        String sql = insertQuery.toString();

        try (Statement statement = mDb.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            int affectedRows = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            if (affectedRows == 0) {
                throw new SQLException("Creating image failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long imageId = generatedKeys.getLong(1);
                    
                    if (image.getPosition() != null) {
                        image.getPosition().setId(imageId);
                        ImagePositionManager.getInstance().insert(image.getPosition());
                    }
                    return imageId;
                } else {
                    throw new SQLException("Creating image failed, no ID obtained.");
                }
            }
        }
    }

    private static class Holder {

        private static final ImageManager INSTANCE = new ImageManager();
    }
}
