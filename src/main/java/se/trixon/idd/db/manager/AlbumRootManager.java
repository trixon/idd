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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.trixon.idl.shared.db.AlbumRoot;

/**
 *
 * @author Patrik Karlsson
 */
public class AlbumRootManager extends BaseManager {

    public static final String COL_ID = "album_root_id";
    public static final String COL_IDENTIFIER = "identifier";
    public static final String COL_LABEL = "label";
    public static final String COL_SPECIFIC_PATH = "specific_path";
    public static final String COL_STATUS = "status";
    public static final String COL_TYPE = "type";
    public static final String TABLE_NAME = "album_root";
    private final DbColumn mIdentifier;
    private final PlaceHolder mIdentifierPlaceHolder;
    private final DbColumn mLabel;
    private final PlaceHolder mLabelPlaceHolder;
    private final DbColumn mSpecificPath;
    private final PlaceHolder mSpecificPathPlaceHolder;
    private final DbColumn mStatus;
    private final PlaceHolder mStatusPlaceHolder;
    private final DbColumn mType;
    private final PlaceHolder mTypePlaceHolder;

    public static AlbumRootManager getInstance() {
        return Holder.INSTANCE;
    }

    private AlbumRootManager() {
        mTable = getSchema().addTable(TABLE_NAME);

        mId = mTable.addColumn(COL_ID, "IDENTITY", null);
        mLabel = mTable.addColumn(COL_LABEL, "VARCHAR", Integer.MAX_VALUE);
        mStatus = mTable.addColumn(COL_STATUS, "INT", null);
        mType = mTable.addColumn(COL_TYPE, "INT", null);
        mIdentifier = mTable.addColumn(COL_IDENTIFIER, "VARCHAR", Integer.MAX_VALUE);
        mSpecificPath = mTable.addColumn(COL_SPECIFIC_PATH, "VARCHAR", Integer.MAX_VALUE);

        addNotNullConstraint(mStatus);
        addNotNullConstraint(mType);

        QueryPreparer preparer = new QueryPreparer();

        mIdPlaceHolder = preparer.getNewPlaceHolder();
        mLabelPlaceHolder = preparer.getNewPlaceHolder();
        mStatusPlaceHolder = preparer.getNewPlaceHolder();
        mTypePlaceHolder = preparer.getNewPlaceHolder();
        mIdentifierPlaceHolder = preparer.getNewPlaceHolder();
        mSpecificPathPlaceHolder = preparer.getNewPlaceHolder();
    }

    @Override
    public void create() {
        String indexName = getIndexName(new DbColumn[]{mId}, "pkey");
        DbConstraint primaryKeyConstraint = new DbConstraint(mTable, indexName, Constraint.Type.PRIMARY_KEY, mId);
        DbConstraint uniqueKeyConstraint = new DbConstraint(mTable, indexName, Constraint.Type.UNIQUE, mIdentifier, mSpecificPath);
        mDb.create(mTable, primaryKeyConstraint, uniqueKeyConstraint);
    }

    public long insert(AlbumRoot albumRoot) throws SQLException, ClassNotFoundException {
        if (mInsertPreparedStatement == null) {
            InsertQuery insertQuery = new InsertQuery(mTable)
                    .addColumn(mId, mIdPlaceHolder)
                    .addColumn(mLabel, mLabelPlaceHolder)
                    .addColumn(mStatus, mStatusPlaceHolder)
                    .addColumn(mType, mTypePlaceHolder)
                    .addColumn(mIdentifier, mIdentifierPlaceHolder)
                    .addColumn(mSpecificPath, mSpecificPathPlaceHolder)
                    .validate();

            String sql = insertQuery.toString();
            try {
                mInsertPreparedStatement = mDb.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                System.out.println(mInsertPreparedStatement.toString());
            } catch (SQLException ex) {
                Logger.getLogger(ImagePositionManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        mIdPlaceHolder.setLong(albumRoot.getId(), mInsertPreparedStatement);
        mLabelPlaceHolder.setString(albumRoot.getLabel(), mInsertPreparedStatement);
        mStatusPlaceHolder.setInt(albumRoot.getStatus(), mInsertPreparedStatement);
        mTypePlaceHolder.setInt(albumRoot.getType(), mInsertPreparedStatement);
        mSpecificPathPlaceHolder.setString(albumRoot.getSpecificPath(), mInsertPreparedStatement);
        mIdentifierPlaceHolder.setString(albumRoot.getIdentifier(), mInsertPreparedStatement);

        int affectedRows = mInsertPreparedStatement.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Creating album root failed, no rows affected.");
        }

        try (ResultSet generatedKeys = mInsertPreparedStatement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            } else {
                throw new SQLException("Creating album root failed, no ID obtained.");
            }
        }
    }

    private static class Holder {

        private static final AlbumRootManager INSTANCE = new AlbumRootManager();
    }
}
