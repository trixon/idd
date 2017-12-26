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

import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import java.sql.PreparedStatement;
import se.trixon.idd.db.Db;
import se.trixon.idd.db.PlaceHolderController;

/**
 *
 * @author Patrik Karlsson
 */
public abstract class BaseManager {

    protected static final String SQL_BIGINT = "BIGINT";
    protected static final String SQL_DATE = "DATE";
    protected static final String SQL_DOUBLE = "DOUBLE";
    protected static final String SQL_IDENTITY = "IDENTITY";
    protected static final String SQL_INT = "INT";
    protected static final String SQL_INTEGER = "INTEGER";
    protected static final String SQL_TIMESTAMP = "TIMESTAMP";
    protected static final String SQL_VARCHAR = "VARCHAR";

    protected final Db mDb;
    protected DbColumn mId;
    protected PlaceHolderController mInsertPlaceHolders = new PlaceHolderController();
    protected PreparedStatement mInsertPreparedStatement;
    protected DbTable mTable;

    public BaseManager() {
        mDb = Db.getInstance();
    }

    public void addNotNullConstraints(DbColumn... columns) {
        for (DbColumn column : columns) {
            column.addConstraint(new DbConstraint(column, null, Constraint.Type.NOT_NULL));
        }
    }

    public abstract void create();

    public DbColumn getId() {
        return mId;
    }

    public String getIndexName(DbColumn[] dbColumns, String suffix) {
        StringBuilder builder = new StringBuilder(mTable.getName()).append("_");

        if (dbColumns != null) {
            for (DbColumn dbColumn : dbColumns) {
                builder.append(dbColumn.getName()).append("_");
            }
        }

        if (builder.lastIndexOf("_") != builder.length() - 1) {
            builder.append("_");
        }

        builder.append(suffix);

        return builder.toString();
    }

    public DbSchema getSchema() {
        return mDb.getSpec().getDefaultSchema();

    }

    public DbTable getTable() {
        return mTable;
    }

    public class Columns {

        public DbColumn getId() {
            return mId;
        }
    }
}
