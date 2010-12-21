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

import java.io.File;
import java.net.URI;
import java.sql.SQLException;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.NotImplementedException;

import org.diffkit.common.DKUserException;
import org.diffkit.common.DKValidate;
import org.diffkit.common.annot.ThreadSafe;
import org.diffkit.db.DKDBConnectionInfo;
import org.diffkit.db.DKDBTable;
import org.diffkit.db.DKDatabase;
import org.diffkit.diff.engine.DKContext;
import org.diffkit.diff.engine.DKDiff;
import org.diffkit.diff.engine.DKRowDiff;
import org.diffkit.diff.engine.DKSide;
import org.diffkit.util.DKObjectUtil;
import org.diffkit.util.DKStringUtil;

/**
 * @author jpanico
 */
@ThreadSafe
public class DKSqlPatchSink extends DKWriterSink {

   private long _rowDiffCount;
   private long _columnDiffCount;
   private final DKDatabase _database;
   private final DKDBTable _lhsTable;
   private final DKDBTable _rhsTable;
   private final File _file;

   public DKSqlPatchSink(DKDBConnectionInfo connectionInfo_, String lhsTableName_,
                         String rhsTableName_, String patchFilePath_) throws SQLException {
      super(null);
      DKValidate.notNull(connectionInfo_, lhsTableName_, rhsTableName_, patchFilePath_);
      _file = new File(patchFilePath_);
      if (_file.exists())
         throw new DKUserException(String.format(
            "sink file [%s] already exists! please remove it and try again.", _file));
      _database = new DKDatabase(connectionInfo_);
      _lhsTable = _database.getTable(null, null, lhsTableName_);
      _rhsTable = _database.getTable(null, null, rhsTableName_);
      DKValidate.notNull(_lhsTable, _rhsTable);
   }

   public void record(DKDiff diff_, DKContext context_) {
      this.ensureStarted();
      this.ensureNotEnded();
      if (diff_ == null)
         return;
      DKDiff.Kind diffKind = diff_.getKind();
      if (diffKind == DKDiff.Kind.ROW_DIFF)
         _rowDiffCount++;
      else if (diffKind == DKDiff.Kind.COLUMN_DIFF)
         _columnDiffCount++;

      if (diffKind == DKDiff.Kind.ROW_DIFF) {

      }
      else if (diffKind == DKDiff.Kind.COLUMN_DIFF)
         _columnDiffCount++;
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
      return Kind.FILE;
   }

   @Override
   public long getDiffCount() {
      return (_rowDiffCount + _columnDiffCount);
   }

   @Override
   public long getRowDiffCount() {
      return _rowDiffCount;
   }

   @Override
   public long getColumnDiffCount() {
      return _columnDiffCount;
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

}
