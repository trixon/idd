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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.trixon.idl.shared.db.Image;

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
    private final PlaceHolder mAccuracyPlaceHolder;
    private final DbColumn mAltitude;
    private final PlaceHolder mAltitudePlaceHolder;
    private final DbColumn mDescription;
    private final PlaceHolder mDescriptionPlaceHolder;
    private PreparedStatement mInsertPreparedStatement;
    private final DbColumn mLatitude;
    private final DbColumn mLatitudeNumber;
    private final PlaceHolder mLatitudeNumberPlaceHolder;
    private final PlaceHolder mLatitudePlaceHolder;
    private final DbColumn mLongitude;
    private final DbColumn mLongitudeNumber;
    private final PlaceHolder mLongitudeNumberPlaceHolder;
    private final PlaceHolder mLongitudePlaceHolder;
    private final DbColumn mOrientation;
    private final PlaceHolder mOrientationPlaceHolder;
    private final DbColumn mRoll;
    private final PlaceHolder mRollPlaceHolder;
    private final DbColumn mTilt;
    private final PlaceHolder mTiltPlaceHolder;

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

        QueryPreparer preparer = new QueryPreparer();

        mAccuracyPlaceHolder = preparer.getNewPlaceHolder();
        mAltitudePlaceHolder = preparer.getNewPlaceHolder();
        mDescriptionPlaceHolder = preparer.getNewPlaceHolder();
        mIdPlaceHolder = preparer.getNewPlaceHolder();
        mLatitudeNumberPlaceHolder = preparer.getNewPlaceHolder();
        mLatitudePlaceHolder = preparer.getNewPlaceHolder();
        mLongitudeNumberPlaceHolder = preparer.getNewPlaceHolder();
        mLongitudePlaceHolder = preparer.getNewPlaceHolder();
        mOrientationPlaceHolder = preparer.getNewPlaceHolder();
        mRollPlaceHolder = preparer.getNewPlaceHolder();
        mTiltPlaceHolder = preparer.getNewPlaceHolder();
    }

    @Override
    public void create() {
        String indexName = getIndexName(new DbColumn[]{mId}, "pkey");
        DbConstraint primaryKeyConstraint = new DbConstraint(mTable, indexName, Constraint.Type.PRIMARY_KEY, mId);

        mDb.create(mTable, primaryKeyConstraint);
    }

    void insert(Image.Position position) throws SQLException {
        if (mInsertPreparedStatement == null) {
            InsertQuery insertQuery = new InsertQuery(mTable)
                    .addColumn(mId, mIdPlaceHolder)
                    .addColumn(mAccuracy, mAccuracyPlaceHolder)
                    .addColumn(mAltitude, mAltitudePlaceHolder)
                    .addColumn(mDescription, mDescriptionPlaceHolder)
                    .addColumn(mLatitude, mLatitudePlaceHolder)
                    .addColumn(mLatitudeNumber, mLatitudeNumberPlaceHolder)
                    .addColumn(mLongitude, mLongitudePlaceHolder)
                    .addColumn(mLongitudeNumber, mLongitudeNumberPlaceHolder)
                    .addColumn(mOrientation, mOrientationPlaceHolder)
                    .addColumn(mRoll, mRollPlaceHolder)
                    .addColumn(mTilt, mTiltPlaceHolder)
                    .validate();

            String sql = insertQuery.toString();
            try {
                mInsertPreparedStatement = mDb.getConnection().prepareStatement(sql);
            } catch (SQLException ex) {
                Logger.getLogger(ImagePositionManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        mIdPlaceHolder.setLong(position.getId(), mInsertPreparedStatement);
        mAccuracyPlaceHolder.setObject(position.getAccuracy(), mInsertPreparedStatement);
        mAltitudePlaceHolder.setObject(position.getAltitude(), mInsertPreparedStatement);
        mDescriptionPlaceHolder.setString(position.getDescription(), mInsertPreparedStatement);
        mLatitudePlaceHolder.setString(position.getLatitude(), mInsertPreparedStatement);
        mLatitudeNumberPlaceHolder.setObject(position.getLatitudeNumber(), mInsertPreparedStatement);
        mLongitudePlaceHolder.setString(position.getLongitude(), mInsertPreparedStatement);
        mLongitudeNumberPlaceHolder.setObject(position.getLongitudeNumber(), mInsertPreparedStatement);
        mOrientationPlaceHolder.setObject(position.getOrientation(), mInsertPreparedStatement);
        mRollPlaceHolder.setObject(position.getRoll(), mInsertPreparedStatement);
        mTiltPlaceHolder.setObject(position.getTilt(), mInsertPreparedStatement);

        mInsertPreparedStatement.executeUpdate();
    }

    private static class Holder {

        private static final ImagePositionManager INSTANCE = new ImagePositionManager();
    }
}
