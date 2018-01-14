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
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.trixon.idl.FrameImage;

/**
 *
 * @author Patrik Karlsson
 */
public class ImagePositionManager extends BaseManager {

    private static final Logger LOGGER = Logger.getLogger(ImagePositionManager.class.getName());

    private final DbColumn mAccuracy;
    private final DbColumn mAltitude;
    private final Columns mColumns = new Columns();
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
        mTable = getSchema().addTable("image_position");

        mId = mTable.addColumn(ImageManager.COL_ID, SQL_BIGINT, null);
        mLatitude = mTable.addColumn("latitude", SQL_VARCHAR, Integer.MAX_VALUE);
        mLatitudeNumber = mTable.addColumn("latitude_number", SQL_DOUBLE, null);
        mLongitude = mTable.addColumn("longitude", SQL_VARCHAR, Integer.MAX_VALUE);
        mLongitudeNumber = mTable.addColumn("longitude_number", SQL_DOUBLE, null);
        mAltitude = mTable.addColumn("altitude", SQL_DOUBLE, null);
        mOrientation = mTable.addColumn("orientation", SQL_DOUBLE, null);
        mTilt = mTable.addColumn("tilt", SQL_DOUBLE, null);
        mRoll = mTable.addColumn("roll", SQL_DOUBLE, null);
        mAccuracy = mTable.addColumn("accuracy", SQL_DOUBLE, null);
        mDescription = mTable.addColumn("description", SQL_VARCHAR, Integer.MAX_VALUE);

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

    public FrameImage.Position getImagePosition(final Long imageId) {
        FrameImage.Position position;

        SelectQuery query = new SelectQuery()
                .addAllTableColumns(mTable)
                .addCondition(BinaryCondition.equalTo(mId, imageId))
                .validate();

        String sql = query.toString();
        try (Statement statement = mDb.getAutoCommitConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            ResultSet rs = statement.executeQuery(sql);
            if (rs.first()) {
                position = new FrameImage.Position();
                position.setAccuracy(getDouble(rs, mAccuracy));
                position.setAltitude(getDouble(rs, mAltitude));
                position.setDescription(getString(rs, mDescription));
                position.setImageId(getLong(rs, mId));
                position.setLatitude(getString(rs, mLatitude));
                position.setLatitudeNumber(getDouble(rs, mLatitudeNumber));
                position.setLongitude(getString(rs, mLongitude));
                position.setLongitudeNumber(getDouble(rs, mLongitudeNumber));
                position.setOrientation(getDouble(rs, mOrientation));
                position.setRoll(getDouble(rs, mRoll));
                position.setTilt(getDouble(rs, mTilt));
            } else {
                position = null;
            }
        } catch (NullPointerException | SQLException ex) {
            LOGGER.log(Level.SEVERE, "dbError: getImagePosition{0}", ex);
            position = null;
        }

        return position;
    }

    public void insert(FrameImage.Position position) throws SQLException {
        if (mInsertPreparedStatement == null) {
            prepareInsert();
        }

        mInsertPlaceHolders.get(mId).setLong(position.getImageId(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mAccuracy).setObject(position.getAccuracy(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mAltitude).setObject(position.getAltitude(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mDescription).setString(position.getDescription(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mLatitude).setString(position.getLatitude(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mLatitudeNumber).setObject(position.getLatitudeNumber(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mLongitude).setString(position.getLongitude(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mLongitudeNumber).setObject(position.getLongitudeNumber(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mOrientation).setObject(position.getOrientation(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mRoll).setObject(position.getRoll(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mTilt).setObject(position.getTilt(), mInsertPreparedStatement);

        mInsertPreparedStatement.executeUpdate();
    }

    private void prepareInsert() throws SQLException {
        mInsertPlaceHolders.init(
                mId,
                mAccuracy,
                mAltitude,
                mDescription,
                mLatitude,
                mLatitudeNumber,
                mLongitude,
                mLongitudeNumber,
                mOrientation,
                mRoll,
                mTilt
        );

        InsertQuery insertQuery = new InsertQuery(mTable)
                .addColumn(mId, mInsertPlaceHolders.get(mId))
                .addColumn(mAccuracy, mInsertPlaceHolders.get(mAccuracy))
                .addColumn(mAltitude, mInsertPlaceHolders.get(mAltitude))
                .addColumn(mDescription, mInsertPlaceHolders.get(mDescription))
                .addColumn(mLatitude, mInsertPlaceHolders.get(mLatitude))
                .addColumn(mLatitudeNumber, mInsertPlaceHolders.get(mLatitudeNumber))
                .addColumn(mLongitude, mInsertPlaceHolders.get(mLongitude))
                .addColumn(mLongitudeNumber, mInsertPlaceHolders.get(mLongitudeNumber))
                .addColumn(mOrientation, mInsertPlaceHolders.get(mOrientation))
                .addColumn(mRoll, mInsertPlaceHolders.get(mRoll))
                .addColumn(mTilt, mInsertPlaceHolders.get(mTilt))
                .validate();

        String sql = insertQuery.toString();
        mInsertPreparedStatement = mDb.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        //System.out.println(mInsertPreparedStatement.toString());
    }

    public class Columns extends BaseManager.Columns {

        public DbColumn getAccuracy() {
            return mAccuracy;
        }

        public DbColumn getAltitude() {
            return mAltitude;
        }

        public DbColumn getDescription() {
            return mDescription;
        }

        public DbColumn getLatitude() {
            return mLatitude;
        }

        public DbColumn getLatitudeNumber() {
            return mLatitudeNumber;
        }

        public DbColumn getLongitude() {
            return mLongitude;
        }

        public DbColumn getLongitudeNumber() {
            return mLongitudeNumber;
        }

        public DbColumn getOrientation() {
            return mOrientation;
        }

        public DbColumn getRoll() {
            return mRoll;
        }

        public DbColumn getTilt() {
            return mTilt;
        }
    }

    private static class Holder {

        private static final ImagePositionManager INSTANCE = new ImagePositionManager();
    }
}
