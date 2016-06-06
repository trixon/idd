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
public class ImagePositionManager extends BaseManager {

    public static final String COL_ACCURACY = "accuracy";
    public static final String COL_ALTITUDE = "altitude";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_LATITUDE = "latitude";
    public static final String COL_LATITUDE_NUMBER = "latitude_number";
    public static final String COL_LONGITUDE = "longitude";
    public static final String COL_LONGITUDE_NUMBER = "longitude_number";
    public static final String COL_ORIENTATION = "orientation";
    public static final String COL_ROLL = "roll";
    public static final String COL_TILT = "tilt";
    public static final String TABLE_NAME = "image_position";
    private final DbColumn mAccuracy;
    private final DbColumn mAltitude;
    private final DbColumn mDescription;
    private final DbColumn mLatitude;
    private final DbColumn mLatitudeNumber;
    private final DbColumn mLongitude;
    private final DbColumn mLongitudeNumber;
    private final DbColumn mOrientation;
    private final DbColumn mRoll;
    private final DbColumn mTilt;

    public static ImagePositionManager getInstance() {
        return Holder.INSTANCE;
    }

    private ImagePositionManager() {
        mTable = getSchema().addTable(TABLE_NAME);

        mId = mTable.addColumn(ImageManager.COL_ID, "BIGINT", null);
        mLatitude = mTable.addColumn(COL_LATITUDE, "VARCHAR", Integer.MAX_VALUE);
        mLatitudeNumber = mTable.addColumn(COL_LATITUDE_NUMBER, "DOUBLE", null);
        mLongitude = mTable.addColumn(COL_LONGITUDE, "VARCHAR", Integer.MAX_VALUE);
        mLongitudeNumber = mTable.addColumn(COL_LONGITUDE_NUMBER, "DOUBLE", null);
        mAltitude = mTable.addColumn(COL_ALTITUDE, "DOUBLE", null);
        mOrientation = mTable.addColumn(COL_ORIENTATION, "DOUBLE", null);
        mTilt = mTable.addColumn(COL_TILT, "DOUBLE", null);
        mRoll = mTable.addColumn(COL_ROLL, "DOUBLE", null);
        mAccuracy = mTable.addColumn(COL_ACCURACY, "DOUBLE", null);
        mDescription = mTable.addColumn(COL_DESCRIPTION, "VARCHAR", Integer.MAX_VALUE);

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

        private static final ImagePositionManager INSTANCE = new ImagePositionManager();
    }
}
