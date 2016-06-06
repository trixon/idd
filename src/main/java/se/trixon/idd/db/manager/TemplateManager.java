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
public class TemplateManager extends BaseManager {

    public static final String COL_ID = "test_id";
    public static final String TABLE_NAME = "test";

    public static TemplateManager getInstance() {
        return Holder.INSTANCE;
    }

    private TemplateManager() {
        mTable = getSchema().addTable(TABLE_NAME);

        mId = mTable.addColumn(COL_ID, "IDENTITY", null);
    }

    @Override
    public void create() {
        String indexName = getIndexName(new DbColumn[]{mId}, "pkey");
        DbConstraint primaryKeyConstraint = new DbConstraint(mTable, indexName, Constraint.Type.PRIMARY_KEY, mId);

        mDb.create(mTable, primaryKeyConstraint);
    }

    private static class Holder {

        private static final TemplateManager INSTANCE = new TemplateManager();
    }
}
