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

import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;
import java.sql.SQLException;
import java.sql.Statement;
import se.trixon.idl.shared.db.Image;

/**
 *
 * @author Patrik Karlsson
 */
public class ImageInformationManager extends BaseManager {

    private final DbColumn mColorDepth;
    private final DbColumn mColorModel;
    private final Columns mColumns = new Columns();
    private final DbColumn mCreationDate;
    private final DbColumn mDigitizationDate;
    private final DbColumn mFormat;
    private final DbColumn mHeight;
    private final DbColumn mOrientation;
    private final DbColumn mRating;
    private final DbColumn mWidth;

    public static ImageInformationManager getInstance() {
        return Holder.INSTANCE;
    }

    private ImageInformationManager() {
        mTable = getSchema().addTable("image_information");

        mId = mTable.addColumn(ImageManager.COL_ID, SQL_BIGINT, null);
        mRating = mTable.addColumn("rating", SQL_INT, null);
        mCreationDate = mTable.addColumn("creation_date", SQL_TIMESTAMP, null);
        mDigitizationDate = mTable.addColumn("digitization_date", SQL_TIMESTAMP, null);
        mOrientation = mTable.addColumn("orientation", SQL_INT, null);
        mWidth = mTable.addColumn("width", SQL_INT, null);
        mHeight = mTable.addColumn("height", SQL_INT, null);
        mFormat = mTable.addColumn("format", SQL_VARCHAR, Integer.MAX_VALUE);
        mColorDepth = mTable.addColumn("color_depth", SQL_INT, null);
        mColorModel = mTable.addColumn("color_model", SQL_INT, null);

        String indexName;
        BaseManager manager;

        manager = ImageManager.getInstance();
        indexName = getIndexName(new DbColumn[]{manager.getId()}, "fkey");
        mId.references(indexName, manager.getTable().getName(), manager.getId().getName());
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

    public void insert(Image.Information information) throws SQLException {
        if (mInsertPreparedStatement == null) {
            prepareInsert();
        }

        mInsertPlaceHolders.get(mId).setLong(information.getId(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mRating).setInt(information.getRating(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mCreationDate).setObject(information.getCreationDate(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mDigitizationDate).setObject(information.getDigitizationDate(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mOrientation).setInt(information.getOrientation(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mWidth).setInt(information.getWidth(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mHeight).setInt(information.getHeigth(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mFormat).setString(information.getFormat(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mColorDepth).setInt(information.getColorDepth(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mColorModel).setInt(information.getColorModel(), mInsertPreparedStatement);

        mInsertPreparedStatement.executeUpdate();
    }

    private void prepareInsert() throws SQLException {
        mInsertPlaceHolders.init(
                mId,
                mRating,
                mCreationDate,
                mDigitizationDate,
                mOrientation,
                mWidth,
                mHeight,
                mFormat,
                mColorDepth,
                mColorModel
        );

        InsertQuery insertQuery = new InsertQuery(mTable)
                .addColumn(mId, mInsertPlaceHolders.get(mId))
                .addColumn(mRating, mInsertPlaceHolders.get(mRating))
                .addColumn(mCreationDate, mInsertPlaceHolders.get(mCreationDate))
                .addColumn(mDigitizationDate, mInsertPlaceHolders.get(mDigitizationDate))
                .addColumn(mOrientation, mInsertPlaceHolders.get(mOrientation))
                .addColumn(mWidth, mInsertPlaceHolders.get(mWidth))
                .addColumn(mHeight, mInsertPlaceHolders.get(mHeight))
                .addColumn(mFormat, mInsertPlaceHolders.get(mFormat))
                .addColumn(mColorDepth, mInsertPlaceHolders.get(mColorDepth))
                .addColumn(mColorModel, mInsertPlaceHolders.get(mColorModel))
                .validate();

        String sql = insertQuery.toString();
        mInsertPreparedStatement = mDb.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        //System.out.println(mInsertPreparedStatement.toString());
    }

    public class Columns extends BaseManager.Columns {

        public DbColumn getColorDepth() {
            return mColorDepth;
        }

        public DbColumn getColorModel() {
            return mColorModel;
        }

        public DbColumn getCreationDate() {
            return mCreationDate;
        }

        public DbColumn getDigitizationDate() {
            return mDigitizationDate;
        }

        public DbColumn getFormat() {
            return mFormat;
        }

        public DbColumn getHeight() {
            return mHeight;
        }

        public DbColumn getOrientation() {
            return mOrientation;
        }

        public DbColumn getRating() {
            return mRating;
        }

        public DbColumn getWidth() {
            return mWidth;
        }

    }

    private static class Holder {

        private static final ImageInformationManager INSTANCE = new ImageInformationManager();
    }
}
