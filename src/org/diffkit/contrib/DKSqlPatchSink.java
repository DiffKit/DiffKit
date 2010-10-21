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
package org.diffkit.contrib;

import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ClassUtils;

import org.diffkit.common.DKValidate;
import org.diffkit.common.annot.ThreadSafe;
import org.diffkit.db.DKDBConnectionInfo;
import org.diffkit.db.DKDBDatabase;
import org.diffkit.db.DKDBTable;
import org.diffkit.diff.engine.DKContext;
import org.diffkit.diff.engine.DKDiff;
import org.diffkit.diff.engine.DKRowDiff;
import org.diffkit.diff.engine.DKSide;
import org.diffkit.diff.sns.DKAbstractSink;
import org.diffkit.diff.sns.DKDiffUtil;
import org.diffkit.util.DKObjectUtil;
import org.diffkit.util.DKStringUtil;

/**
 * @author jpanico
 */
@ThreadSafe
public class DKSqlPatchSink extends DKAbstractSink {

   private List<DKDiff> _diffs;
   private final DKDBDatabase _database;
   private final DKDBTable _lhsTable;
   private final DKDBTable _rhsTable;

   public DKSqlPatchSink(DKDBConnectionInfo connectionInfo_, String lhsTableName_,
                         String rhsTableName_) throws SQLException {
      super(null);
      DKValidate.notNull(connectionInfo_, lhsTableName_, rhsTableName_);
      _database = new DKDBDatabase(connectionInfo_);
      _lhsTable = _database.getTable(null, null, lhsTableName_);
      _rhsTable = _database.getTable(null, null, rhsTableName_);
      DKValidate.notNull(_lhsTable, _rhsTable);
   }

   public void record(DKDiff diff_, DKContext context_) {
      this.ensureStarted();
      this.ensureNotEnded();
      if (diff_ == null)
         return;
      if (_diffs == null)
         _diffs = new ArrayList<DKDiff>();
      synchronized (_diffs) {
         _diffs.add(diff_);
      }
      if (diff_.getKind() == org.diffkit.diff.engine.DKDiff.Kind.ROW_DIFF) {
         try {
            this.printRowDiff((DKRowDiff) diff_);
         }
         catch (SQLException e_) {
            e_.printStackTrace();
         }
      }
   }

   @SuppressWarnings("unchecked")
   private void printRowDiff(DKRowDiff rowDiff_) throws SQLException {
      DKDBTable table = rowDiff_.getSide() == DKSide.LEFT ? _lhsTable : _rhsTable;
      String insertSql = _database.generateInsertDML(rowDiff_.getRowDisplayValues(),
         table);
      System.out.println(insertSql);
   }

   // @Override
   public Kind getKind() {
      return Kind.STREAM;
   }

   public List<DKDiff> getDiffs() {
      return _diffs;
   }

   @Override
   public long getDiffCount() {
      if (_diffs == null)
         return 0;
      return _diffs.size();
   }

   @Override
   public long getRowDiffCount() {
      List<DKDiff> rowDiffs = DKDiffUtil.getRowDiffs(this.getDiffs());
      if (rowDiffs == null)
         return 0;
      return rowDiffs.size();
   }

   @Override
   public long getColumnDiffCount() {
      List<DKDiff> columnDiffs = DKDiffUtil.getColumnDiffs(this.getDiffs());
      if (columnDiffs == null)
         return 0;
      return columnDiffs.size();
   }

   public URI getURI() {
      return DKStringUtil.createURI(String.format("sql://%s",
         DKObjectUtil.getAddressHexString(this)));
   }

   public String toString() {
      return String.format("%s[%s, diffCount=%s]",
         ClassUtils.getShortClassName(this.getClass()), this.getURI(),
         this.getDiffCount());
   }

   public String generateSummary(DKContext context_) {
      return "summary";
   }
}
