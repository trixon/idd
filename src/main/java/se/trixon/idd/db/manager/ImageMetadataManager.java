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

import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;

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
    }

    @Override
    public void create() {
        String indexName = getIndexName(new DbColumn[]{mId}, "pkey");
        DbConstraint primaryKeyConstraint = new DbConstraint(mTable, indexName, Constraint.Type.PRIMARY_KEY, mId);

        mDb.create(mTable, primaryKeyConstraint);
    }

    private static class Holder {

        private static final ImageMetadataManager INSTANCE = new ImageMetadataManager();
    }
}
