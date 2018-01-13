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

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.CustomSql;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.custom.postgresql.PgLimitClause;
import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.trixon.idl.shared.FrameImage;

/**
 *
 * @author Patrik Karlsson
 */
public class ImageManager extends BaseManager {

    public static final String COL_ID = "image_id";
    private static final Logger LOGGER = Logger.getLogger(ImageManager.class.getName());

    private final DbColumn mAlbumId;
    private final AlbumManager mAlbumManager = AlbumManager.getInstance();
    private final AlbumRootManager mAlbumRootManager = AlbumRootManager.getInstance();
    private final DbColumn mCategory;
    private final Columns mColumns = new Columns();
    private final DbColumn mFileSize;
    private final DbColumn mModificationDate;
    private final DbColumn mName;
    private final DbColumn mStatus;
    private final DbColumn mUniqueHash;

    public static ImageManager getInstance() {
        return Holder.INSTANCE;
    }

    private ImageManager() {
        mTable = getSchema().addTable("image");

        mId = mTable.addColumn(COL_ID, SQL_IDENTITY, null);
        mAlbumId = mTable.addColumn(AlbumManager.COL_ID, SQL_BIGINT_NOT_NULL, null);
        mName = mTable.addColumn("name", SQL_VARCHAR, Integer.MAX_VALUE);
        mStatus = mTable.addColumn("status", SQL_INT, null);
        mCategory = mTable.addColumn("category", SQL_INT, null);
        mModificationDate = mTable.addColumn("modification_date", SQL_TIMESTAMP, null);
        mFileSize = mTable.addColumn("file_size", SQL_BIGINT, null);
        mUniqueHash = mTable.addColumn("unique_hash", SQL_VARCHAR, 32);

        String indexName;
        BaseManager manager;

        manager = AlbumManager.getInstance();
        indexName = getIndexName(new DbColumn[]{manager.getId()}, "fkey");
        mAlbumId.references(indexName, manager.getTable(), manager.getId());

        addNotNullConstraints(mName, mStatus, mCategory);
    }

    public Columns columns() {
        return mColumns;
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

    public FrameImage getImage(final Long imageId) {
        SelectQuery query = new SelectQuery()
                .addAllTableColumns(mTable)
                .addColumns(
                        mAlbumRootManager.columns().getSpecificPath(),
                        mAlbumManager.columns().getRelativePath()
                )
                .addJoin(SelectQuery.JoinType.INNER,
                        mTable,
                        mAlbumManager.mTable,
                        mAlbumId,
                        mAlbumManager.getId()
                )
                .addJoin(SelectQuery.JoinType.INNER,
                        mAlbumManager.mTable,
                        mAlbumRootManager.getTable(),
                        mAlbumManager.columns().getAlbumRootId(),
                        mAlbumRootManager.getId()
                )
                .addCondition(BinaryCondition.equalTo(mId, imageId))
                .validate();

        String sql = query.toString();

        FrameImage frameImage;
        try (Statement statement = mDb.getAutoCommitConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            ResultSet rs = statement.executeQuery(sql);
            rs.first();
            frameImage = new FrameImage();
            frameImage.setAlbumId(getLong(rs, mAlbumId));
            frameImage.setCategory(getInteger(rs, mCategory));
            frameImage.setFileSize(getLong(rs, mFileSize));
            frameImage.setId(getLong(rs, mId));
            frameImage.setModificationDate(rs.getTimestamp(mModificationDate.getName()));
            frameImage.setName(getString(rs, mName));
            frameImage.setStatus(getInteger(rs, mStatus));
            frameImage.setUniqueHash(getString(rs, mUniqueHash));

            frameImage.setInformation(ImageInformationManager.getInstance().getImageInformation(imageId));
            frameImage.setMetadata(ImageMetadataManager.getInstance().getImageMetadata(imageId));
            frameImage.setPosition(ImagePositionManager.getInstance().getImagePosition(imageId));

            String path = String.format("%s%s/%s",
                    getString(rs, mAlbumRootManager.columns().getSpecificPath()),
                    getString(rs, mAlbumManager.columns().getRelativePath()),
                    getString(rs, mName));
            frameImage.setPath(path);
        } catch (NullPointerException | SQLException ex) {
            LOGGER.log(Level.SEVERE, "dbError: getImage{0}", ex);
            frameImage = null;
        }

        return frameImage;
    }

    public FrameImage getRandomImage() throws NullPointerException, SQLException {
        return getImage(getRandomImageId());
    }

    public Long getRandomImageId() throws NullPointerException, SQLException {
        SelectQuery selectQuery = new SelectQuery()
                .addColumns(mId)
                .addCustomOrderings(new CustomSql("random()"))
                .addCustomization(new PgLimitClause(1))
                .validate();

        String sql = selectQuery.toString();

        try (Statement statement = mDb.getAutoCommitConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            ResultSet rs = statement.executeQuery(sql);
            rs.first();
            return rs.getLong(mId.getName());
        } catch (NullPointerException | SQLException ex) {
            LOGGER.severe("dbError: getRandomImageId");
            throw ex;
        }
    }

    public Long insert(FrameImage image) throws ClassNotFoundException, SQLException {
        if (mInsertPreparedStatement == null) {
            prepareInsert();
        }

        mInsertPlaceHolders.get(mAlbumId).setLong(image.getAlbumId(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mCategory).setInt(image.getCategory(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mFileSize).setLong(image.getFileSize(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mModificationDate).setObject(image.getModificationDate(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mName).setString(image.getName(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mStatus).setInt(image.getStatus(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mUniqueHash).setString(image.getUniqueHash(), mInsertPreparedStatement);

        int affectedRows = mInsertPreparedStatement.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Creating image failed, no rows affected.");
        }

        try (ResultSet generatedKeys = mInsertPreparedStatement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                Long imageId = generatedKeys.getLong(1);

                if (image.getPosition().hasData()) {
                    image.getPosition().setImageId(imageId);
                    ImagePositionManager.getInstance().insert(image.getPosition());
                }

                if (image.getInformation().hasData()) {
                    image.getInformation().setImageId(imageId);
                    ImageInformationManager.getInstance().insert(image.getInformation());
                }

                if (image.getMetadata().hasData()) {
                    image.getMetadata().setImageId(imageId);
                    ImageMetadataManager.getInstance().insert(image.getMetadata());
                }
                return imageId;
            } else {
                throw new SQLException("Creating image failed, no ID obtained.");
            }
        }
    }

    private void prepareInsert() throws SQLException {
        mInsertPlaceHolders.init(
                mAlbumId,
                mCategory,
                mFileSize,
                mModificationDate,
                mName,
                mStatus,
                mUniqueHash
        );

        InsertQuery insertQuery = new InsertQuery(mTable)
                .addColumn(mAlbumId, mInsertPlaceHolders.get(mAlbumId))
                .addColumn(mCategory, mInsertPlaceHolders.get(mCategory))
                .addColumn(mFileSize, mInsertPlaceHolders.get(mFileSize))
                .addColumn(mModificationDate, mInsertPlaceHolders.get(mModificationDate))
                .addColumn(mName, mInsertPlaceHolders.get(mName))
                .addColumn(mStatus, mInsertPlaceHolders.get(mStatus))
                .addColumn(mUniqueHash, mInsertPlaceHolders.get(mUniqueHash))
                .validate();

        String sql = insertQuery.toString();
        mInsertPreparedStatement = mDb.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        //System.out.println(mInsertPreparedStatement.toString());
    }

    public class Columns extends BaseManager.Columns {

        public DbColumn getAlbumId() {
            return mAlbumId;
        }

        public DbColumn getCategory() {
            return mCategory;
        }

        public DbColumn getFileSize() {
            return mFileSize;
        }

        public DbColumn getModificationDate() {
            return mModificationDate;
        }

        public DbColumn getName() {
            return mName;
        }

        public DbColumn getStatus() {
            return mStatus;
        }

        public DbColumn getUniqueHash() {
            return mUniqueHash;
        }
    }

    private static class Holder {

        private static final ImageManager INSTANCE = new ImageManager();
    }
}
