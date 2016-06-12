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
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.trixon.idl.shared.db.Image;

/**
 *
 * @author Patrik Karlsson
 */
public class ImageMetadataManager extends BaseManager {

    public static final String COL_APERTURE = "aperture";
    public static final String COL_EXPOSURE_MODE = "exposure_mode";
    public static final String COL_EXPOSURE_PROGRAM = "exposure_program";
    public static final String COL_EXPOSURE_TIME = "exposure_time";
    public static final String COL_FLASH = "flash";
    public static final String COL_FOCAL_LENGTH = "focal_length";
    public static final String COL_FOCAL_LENGTH35 = "focal_length35";
    public static final String COL_LENS = "lens";
    public static final String COL_MAKE = "make";
    public static final String COL_METERING_MODE = "metering_mode";
    public static final String COL_MODEL = "model";
    public static final String COL_SENSITIVITY = "sensitivity";
    public static final String COL_SUBJECT_DISTANCE = "subject_distance";
    public static final String COL_SUBJECT_DISTANCE_CATEGORY = "subject_distance_category";
    public static final String COL_WHITE_BALANCE = "white_balance";
    public static final String COL_WHITE_BALANCE_COLOR_TEMPERATURE = "white_balance_color_temperature";
    public static final String TABLE_NAME = "image_metadata";
    private final DbColumn mAperture;
    private final PlaceHolder mAperturePlaceHolder;
    private final DbColumn mExposureMode;
    private final PlaceHolder mExposureModePlaceHolder;
    private final DbColumn mExposureProgram;
    private final PlaceHolder mExposureProgramPlaceHolder;
    private final DbColumn mExposureTime;
    private final PlaceHolder mExposureTimePlaceHolder;
    private final DbColumn mFlash;
    private final PlaceHolder mFlashPlaceHolder;
    private final DbColumn mFocalLength;
    private final DbColumn mFocalLength35;
    private final PlaceHolder mFocalLength35PlaceHolder;
    private final PlaceHolder mFocalLengthPlaceHolder;
    private final DbColumn mLens;
    private final PlaceHolder mLensPlaceHolder;
    private final DbColumn mMake;
    private final PlaceHolder mMakePlaceHolder;
    private final DbColumn mMeteringMode;
    private final PlaceHolder mMeteringModePlaceHolder;
    private final DbColumn mModel;
    private final PlaceHolder mModelPlaceHolder;
    private final DbColumn mSensitivity;
    private final PlaceHolder mSensitivityPlaceHolder;
    private final DbColumn mSubjectDistance;
    private final DbColumn mSubjectDistanceCategory;
    private final PlaceHolder mSubjectDistanceCategoryPlaceHolder;
    private final PlaceHolder mSubjectDistancePlaceHolder;
    private final DbColumn mWhiteBalance;
    private final DbColumn mWhiteBalanceColorTemperature;
    private final PlaceHolder mWhiteBalanceColorTemperaturePlaceHolder;
    private final PlaceHolder mWhiteBalancePlaceHolder;

    public static ImageMetadataManager getInstance() {
        return Holder.INSTANCE;
    }

    private ImageMetadataManager() {
        mTable = getSchema().addTable(TABLE_NAME);
        mId = mTable.addColumn(ImageManager.COL_ID, "BIGINT", null);
        mMake = mTable.addColumn(COL_MAKE, "VARCHAR", Integer.MAX_VALUE);
        mModel = mTable.addColumn(COL_MODEL, "VARCHAR", Integer.MAX_VALUE);
        mLens = mTable.addColumn(COL_LENS, "VARCHAR", Integer.MAX_VALUE);
        mAperture = mTable.addColumn(COL_APERTURE, "DOUBLE", null);
        mFocalLength = mTable.addColumn(COL_FOCAL_LENGTH, "DOUBLE", null);
        mFocalLength35 = mTable.addColumn(COL_FOCAL_LENGTH35, "DOUBLE", null);
        mExposureTime = mTable.addColumn(COL_EXPOSURE_TIME, "DOUBLE", null);
        mExposureProgram = mTable.addColumn(COL_EXPOSURE_PROGRAM, "INTEGER", null);
        mExposureMode = mTable.addColumn(COL_EXPOSURE_MODE, "INTEGER", null);
        mSensitivity = mTable.addColumn(COL_SENSITIVITY, "INTEGER", null);
        mFlash = mTable.addColumn(COL_FLASH, "INTEGER", null);
        mWhiteBalance = mTable.addColumn(COL_WHITE_BALANCE, "INTEGER", null);
        mWhiteBalanceColorTemperature = mTable.addColumn(COL_WHITE_BALANCE_COLOR_TEMPERATURE, "INTEGER", null);
        mMeteringMode = mTable.addColumn(COL_METERING_MODE, "INTEGER", null);
        mSubjectDistance = mTable.addColumn(COL_SUBJECT_DISTANCE, "DOUBLE", null);
        mSubjectDistanceCategory = mTable.addColumn(COL_SUBJECT_DISTANCE_CATEGORY, "INTEGER", null);

        String indexName;
        BaseManager manager;

        manager = ImageManager.getInstance();
        indexName = getIndexName(new DbColumn[]{manager.getId()}, "fkey");
        mId.references(indexName, manager.getTable().getName(), manager.getId().getName());

        QueryPreparer preparer = new QueryPreparer();

        mIdPlaceHolder = preparer.getNewPlaceHolder();
        mMakePlaceHolder = preparer.getNewPlaceHolder();
        mModelPlaceHolder = preparer.getNewPlaceHolder();
        mLensPlaceHolder = preparer.getNewPlaceHolder();
        mAperturePlaceHolder = preparer.getNewPlaceHolder();
        mFocalLengthPlaceHolder = preparer.getNewPlaceHolder();
        mFocalLength35PlaceHolder = preparer.getNewPlaceHolder();
        mExposureTimePlaceHolder = preparer.getNewPlaceHolder();
        mExposureProgramPlaceHolder = preparer.getNewPlaceHolder();
        mExposureModePlaceHolder = preparer.getNewPlaceHolder();
        mSensitivityPlaceHolder = preparer.getNewPlaceHolder();
        mFlashPlaceHolder = preparer.getNewPlaceHolder();
        mWhiteBalancePlaceHolder = preparer.getNewPlaceHolder();
        mWhiteBalanceColorTemperaturePlaceHolder = preparer.getNewPlaceHolder();
        mMeteringModePlaceHolder = preparer.getNewPlaceHolder();
        mSubjectDistancePlaceHolder = preparer.getNewPlaceHolder();
        mSubjectDistanceCategoryPlaceHolder = preparer.getNewPlaceHolder();
    }

    @Override
    public void create() {
        String indexName = getIndexName(new DbColumn[]{mId}, "pkey");
        DbConstraint primaryKeyConstraint = new DbConstraint(mTable, indexName, Constraint.Type.PRIMARY_KEY, mId);

        mDb.create(mTable, primaryKeyConstraint);
    }
    void insert(Image.Metadata metadata) throws SQLException {
        if (mInsertPreparedStatement == null) {
            InsertQuery insertQuery = new InsertQuery(mTable)
                    .addColumn(mId, mIdPlaceHolder)
                    .addColumn(mMake, mMakePlaceHolder)
                    .addColumn(mModel, mModelPlaceHolder)
                    .addColumn(mLens, mLensPlaceHolder)
                    .addColumn(mAperture, mAperturePlaceHolder)
                    .addColumn(mFocalLength, mFocalLengthPlaceHolder)
                    .addColumn(mFocalLength35, mFocalLength35PlaceHolder)
                    .addColumn(mExposureTime, mExposureTimePlaceHolder)
                    .addColumn(mExposureProgram, mExposureProgramPlaceHolder)
                    .addColumn(mExposureMode, mExposureModePlaceHolder)
                    .addColumn(mSensitivity, mSensitivityPlaceHolder)
                    .addColumn(mFlash, mFlashPlaceHolder)
                    .addColumn(mWhiteBalance, mWhiteBalancePlaceHolder)
                    .addColumn(mWhiteBalanceColorTemperature, mWhiteBalanceColorTemperaturePlaceHolder)
                    .addColumn(mMeteringMode, mMeteringModePlaceHolder)
                    .addColumn(mSubjectDistance, mSubjectDistancePlaceHolder)
                    .addColumn(mSubjectDistanceCategory, mSubjectDistanceCategoryPlaceHolder)
                    .validate();

            String sql = insertQuery.toString();
            try {
                mInsertPreparedStatement = mDb.getConnection().prepareStatement(sql);
                //System.out.println(mInsertPreparedStatement.toString());
            } catch (SQLException ex) {
                Logger.getLogger(ImagePositionManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        mIdPlaceHolder.setLong(metadata.getId(), mInsertPreparedStatement);
        mMakePlaceHolder.setString(metadata.getMake(), mInsertPreparedStatement);
        mModelPlaceHolder.setString(metadata.getModel(), mInsertPreparedStatement);
        mLensPlaceHolder.setString(metadata.getLens(), mInsertPreparedStatement);
        mAperturePlaceHolder.setObject(metadata.getAperture(), mInsertPreparedStatement);
        mFocalLengthPlaceHolder.setObject(metadata.getFocalLength(), mInsertPreparedStatement);
        mFocalLength35PlaceHolder.setObject(metadata.getFocalLength35(), mInsertPreparedStatement);
        mExposureTimePlaceHolder.setObject(metadata.getExposureTime(), mInsertPreparedStatement);
        mExposureProgramPlaceHolder.setInt(metadata.getExposureProgram(), mInsertPreparedStatement);
        mExposureModePlaceHolder.setInt(metadata.getExposureMode(), mInsertPreparedStatement);
        mSensitivityPlaceHolder.setInt(metadata.getSensitivity(), mInsertPreparedStatement);
        mFlashPlaceHolder.setInt(metadata.getFlash(), mInsertPreparedStatement);
        mWhiteBalancePlaceHolder.setInt(metadata.getWhiteBalance(), mInsertPreparedStatement);
        mWhiteBalanceColorTemperaturePlaceHolder.setInt(metadata.getWhiteBalanceColorTemperature(), mInsertPreparedStatement);
        mMeteringModePlaceHolder.setInt(metadata.getMeteringMode(), mInsertPreparedStatement);
        mSubjectDistancePlaceHolder.setString(metadata.getSubjectDistance(), mInsertPreparedStatement);
        mSubjectDistanceCategoryPlaceHolder.setInt(metadata.getSubjectDistanceCategory(), mInsertPreparedStatement);

        mInsertPreparedStatement.executeUpdate();
    }

    private static class Holder {

        private static final ImageMetadataManager INSTANCE = new ImageMetadataManager();
    }
}
