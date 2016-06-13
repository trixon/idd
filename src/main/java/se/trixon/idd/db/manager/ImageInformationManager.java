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
public class ImageInformationManager extends BaseManager {

    public static final String COL_COLOR_DEPTH = "color_depth";
    public static final String COL_COLOR_MODEL = "color_model";
    public static final String COL_CREATION_DATE = "creation_date";
    public static final String COL_DIGITIZATION_DATE = "digitization_date";
    public static final String COL_FORMAT = "format";
    public static final String COL_HEIGHT = "height";
    public static final String COL_ORIENTATION = "orientation";
    public static final String COL_RATING = "rating";
    public static final String COL_WIDTH = "width";
    public static final String TABLE_NAME = "image_information";
    private final DbColumn mColorDepth;
    private PlaceHolder mColorDepthPlaceHolder;
    private PlaceHolder mColorModePlaceHolder;
    private final DbColumn mColorModel;
    private final DbColumn mCreationDate;
    private PlaceHolder mCreationDatePlaceHolder;
    private final DbColumn mDigitizationDate;
    private PlaceHolder mDigitizationDatePlaceHolder;
    private final DbColumn mFormat;
    private PlaceHolder mFormatPlaceHolder;
    private final DbColumn mHeight;
    private PlaceHolder mHeightPlaceHolder;
    private final DbColumn mOrientation;
    private PlaceHolder mOrientationPlaceHolder;
    private final DbColumn mRating;
    private PlaceHolder mRatingPlaceHolder;
    private final DbColumn mWidth;
    private PlaceHolder mWidthPlaceHolder;

    public static ImageInformationManager getInstance() {
        return Holder.INSTANCE;
    }

    private ImageInformationManager() {
        mTable = getSchema().addTable(TABLE_NAME);

        mId = mTable.addColumn(ImageManager.COL_ID, "BIGINT", null);
        mRating = mTable.addColumn(COL_RATING, "INT", null);
        mCreationDate = mTable.addColumn(COL_CREATION_DATE, "TIMESTAMP", null);
        mDigitizationDate = mTable.addColumn(COL_DIGITIZATION_DATE, "TIMESTAMP", null);
        mOrientation = mTable.addColumn(COL_ORIENTATION, "INT", null);
        mWidth = mTable.addColumn(COL_WIDTH, "INT", null);
        mHeight = mTable.addColumn(COL_HEIGHT, "INT", null);
        mFormat = mTable.addColumn(COL_FORMAT, "VARCHAR", Integer.MAX_VALUE);
        mColorDepth = mTable.addColumn(COL_COLOR_DEPTH, "INT", null);
        mColorModel = mTable.addColumn(COL_COLOR_MODEL, "INT", null);

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

    @Override
    public void prepare() throws SQLException {
        QueryPreparer preparer = new QueryPreparer();

        mIdPlaceHolder = preparer.getNewPlaceHolder();
        mRatingPlaceHolder = preparer.getNewPlaceHolder();
        mCreationDatePlaceHolder = preparer.getNewPlaceHolder();
        mDigitizationDatePlaceHolder = preparer.getNewPlaceHolder();
        mOrientationPlaceHolder = preparer.getNewPlaceHolder();
        mWidthPlaceHolder = preparer.getNewPlaceHolder();
        mHeightPlaceHolder = preparer.getNewPlaceHolder();
        mFormatPlaceHolder = preparer.getNewPlaceHolder();
        mColorDepthPlaceHolder = preparer.getNewPlaceHolder();
        mColorModePlaceHolder = preparer.getNewPlaceHolder();

        InsertQuery insertQuery = new InsertQuery(mTable)
                .addColumn(mId, mIdPlaceHolder)
                .addColumn(mRating, mRatingPlaceHolder)
                .addColumn(mCreationDate, mCreationDatePlaceHolder)
                .addColumn(mDigitizationDate, mDigitizationDatePlaceHolder)
                .addColumn(mOrientation, mOrientationPlaceHolder)
                .addColumn(mWidth, mWidthPlaceHolder)
                .addColumn(mHeight, mHeightPlaceHolder)
                .addColumn(mFormat, mFormatPlaceHolder)
                .addColumn(mColorDepth, mColorDepthPlaceHolder)
                .addColumn(mColorModel, mColorModePlaceHolder)
                .validate();

        String sql = insertQuery.toString();
        mInsertPreparedStatement = mDb.getConnection().prepareStatement(sql);
        //System.out.println(mInsertPreparedStatement.toString());
    }

    void insert(Image.Information information) throws SQLException {
        mIdPlaceHolder.setLong(information.getId(), mInsertPreparedStatement);
        mRatingPlaceHolder.setInt(information.getRating(), mInsertPreparedStatement);
        mCreationDatePlaceHolder.setObject(information.getCreationDate(), mInsertPreparedStatement);
        mDigitizationDatePlaceHolder.setObject(information.getDigitizationDate(), mInsertPreparedStatement);
        mOrientationPlaceHolder.setInt(information.getOrientation(), mInsertPreparedStatement);
        mWidthPlaceHolder.setInt(information.getWidth(), mInsertPreparedStatement);
        mHeightPlaceHolder.setInt(information.getHeigth(), mInsertPreparedStatement);
        mFormatPlaceHolder.setString(information.getFormat(), mInsertPreparedStatement);
        mColorDepthPlaceHolder.setInt(information.getColorDepth(), mInsertPreparedStatement);
        mColorModePlaceHolder.setInt(information.getColorModel(), mInsertPreparedStatement);

        mInsertPreparedStatement.executeUpdate();
    }

    private static class Holder {

        private static final ImageInformationManager INSTANCE = new ImageInformationManager();
    }
}
