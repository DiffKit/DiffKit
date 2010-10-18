/**
 * Copyright 2010 Joseph Panico
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.diffkit.diff.sns;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;
import org.diffkit.db.DKDBColumn;
import org.diffkit.db.DKDBDatabase;
import org.diffkit.db.DKDBPrimaryKey;
import org.diffkit.db.DKDBTable;
import org.diffkit.db.DKDBTableDataAccess;
import org.diffkit.db.DKDBTypeInfoDataAccess;
import org.diffkit.diff.engine.DKColumnDiff;
import org.diffkit.diff.engine.DKContext;
import org.diffkit.diff.engine.DKDiff;
import org.diffkit.diff.engine.DKRowDiff;
import org.diffkit.diff.engine.DKSide;
import org.diffkit.util.DKObjectUtil;
import org.diffkit.util.DKSqlUtil;

/**
 * @author jpanico
 */
public class DKDBSink extends DKAbstractSink {

   private final DKDBDatabase _database;
   private final DKDBTableDataAccess _tableDataAccess;
   private final DKDBTable _diffContextTable;
   private final DKDBTable _diffTable;
   private transient Connection _connection;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   public DKDBSink(DKDBDatabase database_) throws SQLException {
      super(null);
      _database = database_;
      _tableDataAccess = new DKDBTableDataAccess(_database);
      _diffContextTable = this.generateDiffContextTable();
      _diffTable = this.generateDiffTable();
      DKValidate.notNull(_database, _diffContextTable, _diffTable);
   }

   // @Override
   public Kind getKind() {
      return Kind.DB;
   }

   public void open(DKContext context_) throws IOException {
      super.open(context_);
      try {
         _connection = _database.getConnection();
         this.ensureTables();
         this.saveContext(context_);
      }
      catch (SQLException e_) {
         _log.error(null, e_);
         throw new RuntimeException(e_);
      }
      _log.info("_connection->{}", _connection);
   }

   public void close(DKContext context_) throws IOException {
      DKSqlUtil.close(_connection);
      _connection = null;
      super.close(context_);
   }

   public void record(DKDiff diff_, DKContext context_) throws IOException {
      super.record(diff_, context_);
      try {
         this.saveDiff(diff_, context_);
      }
      catch (SQLException e_) {
         throw new RuntimeException(e_);
      }
   }

   public DKDBTable getDiffContextTable() {
      return _diffContextTable;
   }

   public DKDBTable getDiffTable() {
      return _diffTable;
   }

   private void saveContext(DKContext context_) throws SQLException {
      Map<String, ?> row = this.createRow(context_);
      _database.insertRow(row, _diffContextTable);
   }

   private void saveDiff(DKDiff diff_, DKContext context_) throws SQLException {
      Map<String, ?> row = this.createRow(diff_, context_);
      _database.insertRow(row, _diffTable);
   }

   // check that diff_context and diff tables exist; if not, create them
   private void ensureTables() throws SQLException {
      this.ensureDiffContextTable();
      this.ensureDiffTable();
   }

   private void ensureDiffContextTable() throws SQLException {
      if (_tableDataAccess.getTable(_diffContextTable.getCatalog(),
         _diffContextTable.getSchema(), _diffContextTable.getTableName()) == null) {
         if (!_database.createTable(_diffContextTable))
            throw new RuntimeException(String.format(
               "couldn't create _diffContextTable->%s", _diffContextTable));
      }
   }

   private void ensureDiffTable() throws SQLException {
      if (_tableDataAccess.getTable(_diffTable.getCatalog(), _diffTable.getSchema(),
         _diffTable.getTableName()) == null) {
         if (!_database.createTable(_diffTable))
            throw new RuntimeException(String.format("couldn't create _diffTable->%s",
               _diffTable));
      }
   }

   private DKDBTable generateDiffContextTable() throws SQLException {
      DKDBTypeInfoDataAccess typeInfoDataAccess = _database.getTypeInfoDataAccess();
      DKDBColumn idColumn = new DKDBColumn("ID", 1, "BIGINT", -1, false);
      DKDBColumn lhsColumn = new DKDBColumn("LHS", 2, "VARCHAR", 128, true);
      DKDBColumn rhsColumn = new DKDBColumn("RHS", 3, "VARCHAR", 128, true);
      DKDBColumn sinkColumn = new DKDBColumn("SINK", 4, "VARCHAR", 128, true);
      DKDBColumn startTimeColumn = new DKDBColumn("START_TIME", 5, "TIMESTAMP", -1, true);
      DKDBColumn endTimeColumn = new DKDBColumn("END_TIME", 6, "TIMESTAMP", -1, true);
      String[] pkColNames = { "ID" };
      DKDBPrimaryKey pk = new DKDBPrimaryKey("PK_DIFF_CONTEXT", pkColNames);
      DKDBColumn[] columns = { idColumn, lhsColumn, rhsColumn, sinkColumn,
         startTimeColumn, endTimeColumn };
      return new DKDBTable(null, null, "DIFF_CONTEXT", columns, pk);
   }

   private DKDBTable generateDiffTable() throws SQLException {
      DKDBTypeInfoDataAccess typeInfoDataAccess = _database.getTypeInfoDataAccess();
      DKDBColumn contextIdColumn = new DKDBColumn("CONTEXT_ID", 1, "BIGINT", -1, true);
      DKDBColumn rowStepColumn = new DKDBColumn("ROW_STEP", 2, "INTEGER", -1, true);
      DKDBColumn columnStepColumn = new DKDBColumn("COLUMN_STEP", 3, "INTEGER", -1, true);
      DKDBColumn kindColumn = new DKDBColumn("KIND", 4, "TINYINT", -1, true);
      DKDBColumn rowDisplayValuesColumn = new DKDBColumn("ROW_DISPLAY_VALUES", 5,
         "VARCHAR", 256, true);
      DKDBColumn lhsColumn = new DKDBColumn("LHS", 6, "VARCHAR", 128, true);
      DKDBColumn rhsColumn = new DKDBColumn("RHS", 7, "VARCHAR", 128, true);
      String[] pkColNames = { "CONTEXT_ID", "ROW_STEP", "COLUMN_STEP" };
      DKDBPrimaryKey pk = new DKDBPrimaryKey("PK_DIFF", pkColNames);
      DKDBColumn[] columns = { contextIdColumn, rowStepColumn, columnStepColumn,
         kindColumn, rowDisplayValuesColumn, lhsColumn, rhsColumn };
      return new DKDBTable(null, null, "DIFF", columns, pk);
   }

   private Map<String, ?> createRow(DKContext context_) {
      Map<String, Object> row = new HashMap<String, Object>();
      row.put("ID", new Long(context_._id));
      row.put("LHS", context_._lhs.toString());
      row.put("RHS", context_._rhs.toString());
      row.put("SINK", context_._sink.toString());
      row.put("START_TIME", new Timestamp(System.currentTimeMillis()));
      return row;
   }

   private Map<String, ?> createRow(DKDiff diff_, DKContext context_) {
      Map<String, Object> row = new HashMap<String, Object>();
      row.put("CONTEXT_ID", new Long(context_._id));
      row.put("ROW_STEP", diff_.getRowStep());
      row.put("COLUMN_STEP", diff_.getColumnStep());
      row.put("KIND", new Integer(diff_.getKind().ordinal()));
      row.put("LHS", this.format(diff_, DKSide.LEFT));
      row.put("RHS", this.format(diff_, DKSide.RIGHT));
      return row;
   }

   private String format(DKDiff diff_, DKSide side_) {
      if (diff_.getKind() == DKDiff.Kind.COLUMN_DIFF) {
         DKColumnDiff columnDiff = (DKColumnDiff) diff_;
         if (side_ == DKSide.LEFT)
            return DKObjectUtil.toString(columnDiff.getLhs());
         return DKObjectUtil.toString(columnDiff.getRhs());
      }
      else if (diff_.getKind() == DKDiff.Kind.ROW_DIFF) {
         DKRowDiff rowDiff = (DKRowDiff) diff_;
         if (side_ == DKSide.LEFT) {
            if (rowDiff.getSide() == DKSide.LEFT)
               return "<";
            return null;
         }
         else {
            if (rowDiff.getSide() == DKSide.RIGHT)
               return ">";
            return null;
         }
      }
      else
         throw new RuntimeException(String.format("unrecognized diff Kind->%s",
            diff_.getKind()));
   }

   public String generateSummary(DKContext context_) {
      return "summary";
   }
}
