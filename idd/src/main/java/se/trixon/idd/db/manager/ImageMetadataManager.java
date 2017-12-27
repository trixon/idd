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
package se.trixon.idd.db.manager;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import se.trixon.almond.util.Xlog;
import se.trixon.idl.shared.db.Image;

/**
 *
 * @author Patrik Karlsson
 */
public class ImageMetadataManager extends BaseManager {

    private final DbColumn mAperture;
    private final Columns mColumns = new Columns();
    private final DbColumn mExposureMode;
    private final DbColumn mExposureProgram;
    private final DbColumn mExposureTime;
    private final DbColumn mFlash;
    private final DbColumn mFocalLength;
    private final DbColumn mFocalLength35;
    private final DbColumn mLens;
    private final DbColumn mMake;
    private final DbColumn mMeteringMode;
    private final DbColumn mModel;
    private final DbColumn mSensitivity;
    private final DbColumn mSubjectDistance;
    private final DbColumn mSubjectDistanceCategory;
    private final DbColumn mWhiteBalance;
    private final DbColumn mWhiteBalanceColorTemperature;

    public static ImageMetadataManager getInstance() {
        return Holder.INSTANCE;
    }

    private ImageMetadataManager() {
        mTable = getSchema().addTable("image_metadata");

        mId = mTable.addColumn(ImageManager.COL_ID, SQL_BIGINT, null);
        mMake = mTable.addColumn("make", SQL_VARCHAR, Integer.MAX_VALUE);
        mModel = mTable.addColumn("model", SQL_VARCHAR, Integer.MAX_VALUE);
        mLens = mTable.addColumn("lens", SQL_VARCHAR, Integer.MAX_VALUE);
        mAperture = mTable.addColumn("aperture", SQL_DOUBLE, null);
        mFocalLength = mTable.addColumn("focal_length", SQL_DOUBLE, null);
        mFocalLength35 = mTable.addColumn("focal_length35", SQL_DOUBLE, null);
        mExposureTime = mTable.addColumn("exposure_time", SQL_DOUBLE, null);
        mExposureProgram = mTable.addColumn("exposure_program", SQL_INTEGER, null);
        mExposureMode = mTable.addColumn("exposure_mode", SQL_INTEGER, null);
        mSensitivity = mTable.addColumn("sensitivity", SQL_INTEGER, null);
        mFlash = mTable.addColumn("flash", SQL_INTEGER, null);
        mWhiteBalance = mTable.addColumn("white_balance", SQL_INTEGER, null);
        mWhiteBalanceColorTemperature = mTable.addColumn("white_balance_color_temperature", SQL_INTEGER, null);
        mMeteringMode = mTable.addColumn("metering_mode", SQL_INTEGER, null);
        mSubjectDistance = mTable.addColumn("subject_distance", SQL_DOUBLE, null);
        mSubjectDistanceCategory = mTable.addColumn("subject_distance_category", SQL_INTEGER, null);

        String indexName;
        BaseManager manager;

        manager = ImageManager.getInstance();
        indexName = getIndexName(new DbColumn[]{manager.getId()}, "fkey");
        mId.references(indexName, manager.getTable(), manager.getId());
    }

    public Columns columns() {
        return mColumns;
    }

    @Override
    public void create() {
        String indexName = getIndexName(new DbColumn[]{mId}, "pkey");
        DbConstraint primaryKeyConstraint = new DbConstraint(mTable, indexName, Constraint.Type.PRIMARY_KEY, mId);

        mDb.create(mTable, primaryKeyConstraint);
    }

    public Image.Metadata getImageMetadata(final Long imageId) {
        Image.Metadata metadata;

        SelectQuery query = new SelectQuery()
                .addAllTableColumns(mTable)
                .addCondition(BinaryCondition.equalTo(mId, imageId))
                .validate();

        String sql = query.toString();
        try (Statement statement = mDb.getAutoCommitConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            ResultSet rs = statement.executeQuery(sql);
            rs.first();
            metadata = new Image.Metadata();
            metadata.setAperture(getDouble(rs, mAperture));
            metadata.setExposureMode(getInteger(rs, mExposureMode));
            metadata.setExposureProgram(getInteger(rs, mExposureProgram));
            metadata.setExposureTime(getDouble(rs, mExposureTime));
            metadata.setFlash(getInteger(rs, mFlash));
            metadata.setFocalLength(getDouble(rs, mFocalLength));
            metadata.setFocalLength35(getDouble(rs, mFocalLength35));
            metadata.setImageId(getLong(rs, mId));
            metadata.setLens(getString(rs, mLens));
            metadata.setMake(getString(rs, mMake));
            metadata.setMeteringMode(getInteger(rs, mMeteringMode));
            metadata.setModel(getString(rs, mModel));
            metadata.setSensitivity(getInteger(rs, mSensitivity));
            metadata.setSubjectDistance(getString(rs, mSubjectDistance));
            metadata.setSubjectDistanceCategory(getInteger(rs, mSubjectDistanceCategory));
            metadata.setWhiteBalance(getInteger(rs, mWhiteBalance));
            metadata.setWhiteBalanceColorTemperature(getInteger(rs, mWhiteBalanceColorTemperature));
        } catch (NullPointerException | SQLException ex) {
            Xlog.timedErr("dbError: getImageMetadata" + ex);
            metadata = null;
        }

        return metadata;
    }

    public void insert(Image.Metadata metadata) throws SQLException {
        if (mInsertPreparedStatement == null) {
            prepareInsert();
        }

        mInsertPlaceHolders.get(mId).setLong(metadata.getImageId(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mMake).setString(metadata.getMake(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mModel).setString(metadata.getModel(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mLens).setString(metadata.getLens(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mAperture).setObject(metadata.getAperture(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mFocalLength).setObject(metadata.getFocalLength(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mFocalLength35).setObject(metadata.getFocalLength35(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mExposureTime).setObject(metadata.getExposureTime(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mExposureProgram).setInt(metadata.getExposureProgram(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mExposureMode).setInt(metadata.getExposureMode(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mSensitivity).setInt(metadata.getSensitivity(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mFlash).setInt(metadata.getFlash(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mWhiteBalance).setInt(metadata.getWhiteBalance(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mWhiteBalanceColorTemperature).setInt(metadata.getWhiteBalanceColorTemperature(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mMeteringMode).setInt(metadata.getMeteringMode(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mSubjectDistance).setString(metadata.getSubjectDistance(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mSubjectDistanceCategory).setInt(metadata.getSubjectDistanceCategory(), mInsertPreparedStatement);

        mInsertPreparedStatement.executeUpdate();
    }

    private void prepareInsert() throws SQLException {
        mInsertPlaceHolders.init(
                mId,
                mMake,
                mModel,
                mLens,
                mAperture,
                mFocalLength,
                mFocalLength35,
                mExposureTime,
                mExposureProgram,
                mExposureMode,
                mSensitivity,
                mFlash,
                mWhiteBalance,
                mWhiteBalanceColorTemperature,
                mMeteringMode,
                mSubjectDistance,
                mSubjectDistanceCategory
        );

        InsertQuery insertQuery = new InsertQuery(mTable)
                .addColumn(mId, mInsertPlaceHolders.get(mId))
                .addColumn(mMake, mInsertPlaceHolders.get(mMake))
                .addColumn(mModel, mInsertPlaceHolders.get(mModel))
                .addColumn(mLens, mInsertPlaceHolders.get(mLens))
                .addColumn(mAperture, mInsertPlaceHolders.get(mAperture))
                .addColumn(mFocalLength, mInsertPlaceHolders.get(mFocalLength))
                .addColumn(mFocalLength35, mInsertPlaceHolders.get(mFocalLength35))
                .addColumn(mExposureTime, mInsertPlaceHolders.get(mExposureTime))
                .addColumn(mExposureProgram, mInsertPlaceHolders.get(mExposureProgram))
                .addColumn(mExposureMode, mInsertPlaceHolders.get(mExposureMode))
                .addColumn(mSensitivity, mInsertPlaceHolders.get(mSensitivity))
                .addColumn(mFlash, mInsertPlaceHolders.get(mFlash))
                .addColumn(mWhiteBalance, mInsertPlaceHolders.get(mWhiteBalance))
                .addColumn(mWhiteBalanceColorTemperature, mInsertPlaceHolders.get(mWhiteBalanceColorTemperature))
                .addColumn(mMeteringMode, mInsertPlaceHolders.get(mMeteringMode))
                .addColumn(mSubjectDistance, mInsertPlaceHolders.get(mSubjectDistance))
                .addColumn(mSubjectDistanceCategory, mInsertPlaceHolders.get(mSubjectDistanceCategory))
                .validate();

        String sql = insertQuery.toString();
        mInsertPreparedStatement = mDb.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        //System.out.println(mInsertPreparedStatement.toString());
    }

    public class Columns extends BaseManager.Columns {

        public DbColumn getAperture() {
            return mAperture;
        }

        public DbColumn getExposureMode() {
            return mExposureMode;
        }

        public DbColumn getExposureProgram() {
            return mExposureProgram;
        }

        public DbColumn getExposureTime() {
            return mExposureTime;
        }

        public DbColumn getFlash() {
            return mFlash;
        }

        public DbColumn getFocalLength() {
            return mFocalLength;
        }

        public DbColumn getFocalLength35() {
            return mFocalLength35;
        }

        public DbColumn getLens() {
            return mLens;
        }

        public DbColumn getMake() {
            return mMake;
        }

        public DbColumn getMeteringMode() {
            return mMeteringMode;
        }

        public DbColumn getModel() {
            return mModel;
        }

        public DbColumn getSensitivity() {
            return mSensitivity;
        }

        public DbColumn getSubjectDistance() {
            return mSubjectDistance;
        }

        public DbColumn getSubjectDistanceCategory() {
            return mSubjectDistanceCategory;
        }

        public DbColumn getWhiteBalance() {
            return mWhiteBalance;
        }

        public DbColumn getWhiteBalanceColorTemperature() {
            return mWhiteBalanceColorTemperature;
        }
    }

    private static class Holder {

        private static final ImageMetadataManager INSTANCE = new ImageMetadataManager();
    }
}
