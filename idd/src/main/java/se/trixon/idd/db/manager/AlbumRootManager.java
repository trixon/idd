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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import se.trixon.idl.shared.db.AlbumRoot;

/**
 *
 * @author Patrik Karlsson
 */
public class AlbumRootManager extends BaseManager {

    public static final String COL_ID = "album_root_id";

    private final Columns mColumns = new Columns();
    private final DbColumn mIdentifier;
    private final DbColumn mLabel;
    private final DbColumn mSpecificPath;
    private final DbColumn mStatus;
    private final DbColumn mType;

    public static AlbumRootManager getInstance() {
        return Holder.INSTANCE;
    }

    private AlbumRootManager() {
        mTable = getSchema().addTable("album_root");

        mId = mTable.addColumn(COL_ID, SQL_IDENTITY, null);
        mLabel = mTable.addColumn("label", SQL_VARCHAR, Integer.MAX_VALUE);
        mStatus = mTable.addColumn("status", SQL_INT, null);
        mType = mTable.addColumn("type", SQL_INT, null);
        mIdentifier = mTable.addColumn("identifier", SQL_VARCHAR, Integer.MAX_VALUE);
        mSpecificPath = mTable.addColumn("specific_path", SQL_VARCHAR, Integer.MAX_VALUE);
    }

    public Columns columns() {
        return mColumns;
    }

    @Override
    public void create() {
        String indexName;
        indexName = getIndexName(new DbColumn[]{mId}, "pkey");
        DbConstraint primaryKeyConstraint = new DbConstraint(mTable, indexName, Constraint.Type.PRIMARY_KEY, mId);

        indexName = getIndexName(new DbColumn[]{mIdentifier, mSpecificPath}, "key");
        DbConstraint uniqueKeyConstraint = new DbConstraint(mTable, indexName, Constraint.Type.UNIQUE, mIdentifier, mSpecificPath);

        addNotNullConstraints(mStatus, mType);

        mDb.create(mTable, primaryKeyConstraint, uniqueKeyConstraint);
    }

    public Long insert(AlbumRoot albumRoot) throws SQLException, ClassNotFoundException {
        if (mInsertPreparedStatement == null) {
            prepareInsert();
        }

        mInsertPlaceHolders.get(mId).setLong(albumRoot.getId(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mLabel).setString(albumRoot.getLabel(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mStatus).setInt(albumRoot.getStatus(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mType).setInt(albumRoot.getType(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mSpecificPath).setString(albumRoot.getSpecificPath(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mIdentifier).setString(albumRoot.getIdentifier(), mInsertPreparedStatement);

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

    private void prepareInsert() throws SQLException {
        mInsertPlaceHolders.init(
                mId,
                mIdentifier,
                mLabel,
                mSpecificPath,
                mStatus,
                mType
        );

        InsertQuery insertQuery = new InsertQuery(mTable)
                .addColumn(mId, mInsertPlaceHolders.get(mId))
                .addColumn(mIdentifier, mInsertPlaceHolders.get(mIdentifier))
                .addColumn(mLabel, mInsertPlaceHolders.get(mLabel))
                .addColumn(mSpecificPath, mInsertPlaceHolders.get(mSpecificPath))
                .addColumn(mStatus, mInsertPlaceHolders.get(mStatus))
                .addColumn(mType, mInsertPlaceHolders.get(mType))
                .validate();

        String sql = insertQuery.toString();
        mInsertPreparedStatement = mDb.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        //System.out.println(mInsertPreparedStatement.toString());
    }

    public class Columns extends BaseManager.Columns {

        public DbColumn getIdentifier() {
            return mIdentifier;
        }

        public DbColumn getLabel() {
            return mLabel;
        }

        public DbColumn getSpecificPath() {
            return mSpecificPath;
        }

        public DbColumn getStatus() {
            return mStatus;
        }

        public DbColumn getType() {
            return mType;
        }
    }

    private static class Holder {

        private static final AlbumRootManager INSTANCE = new AlbumRootManager();
    }
}
