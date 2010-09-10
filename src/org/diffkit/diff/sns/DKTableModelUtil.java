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

import java.sql.Types;

import org.diffkit.common.DKUserException;
import org.diffkit.db.DKDBColumn;
import org.diffkit.db.DKDBPrimaryKey;
import org.diffkit.db.DKDBTable;
import org.diffkit.diff.engine.DKColumnModel;
import org.diffkit.diff.engine.DKTableModel;
import org.diffkit.util.DKSqlUtil;

/**
 * @author jpanico
 */
public class DKTableModelUtil {

   private DKTableModelUtil() {
   }

   public static DKDBTable createDefaultDBTable(String tableName_,
                                                DKTableModel tableModel_) {
      if (tableModel_ == null)
         return null;
      DKColumnModel[] columnsModels = tableModel_.getColumns();
      DKDBColumn[] columns = new DKDBColumn[columnsModels.length];
      for (int i = 0; i < columns.length; i++)
         columns[i] = createDefaultDBColumn(columnsModels[i]);
      DKDBPrimaryKey primaryKey = createDefaultPrimaryKey(tableName_, tableModel_);
      return new DKDBTable(null, null, tableName_, columns, primaryKey);
   }

   public static DKDBColumn createDefaultDBColumn(DKColumnModel columnModel_) {
      if (columnModel_ == null)
         return null;
      return new DKDBColumn(columnModel_._name, columnModel_._index + 1,
         DKSqlUtil.getNameForSqlType(getSqlType(columnModel_._type)), 128, true);
   }

   public static DKDBPrimaryKey createDefaultPrimaryKey(String tableName_,
                                                        DKTableModel tableModel_) {
      if (tableModel_ == null)
         return null;
      return new DKDBPrimaryKey("pk_" + tableName_, tableModel_.getKeyColumnNames());
   }

   public static DKTableModel createDefaultTableModel(DKDBTable table_,
                                                      String[] keyColumnNames_) {
      if (table_ == null)
         return null;
      String tableName = table_.getSchemaQualifiedTableName();
      DKDBColumn[] columns = table_.getColumns();
      DKColumnModel[] columnModels = new DKColumnModel[columns.length];
      for (int i = 0; i < columns.length; i++)
         columnModels[i] = createDefaultColumnModel(columns[i]);
      int[] keyIndices = null;
      if (keyColumnNames_ != null)
         keyIndices = table_.getIndicesOfColumns(keyColumnNames_);
      else
         keyIndices = table_.getPrimaryKeyColumnIndices();
      if ((keyIndices == null) && (keyColumnNames_ == null))
         throw new DKUserException(
            String.format(
               "Cannot create model for table [%s]. The table does not have a PK defined in the database and no keyColumnNames were specified.",
               table_));

      return new DKTableModel(tableName, columnModels, keyIndices);
   }

   public static DKColumnModel createDefaultColumnModel(DKDBColumn column_) {
      if (column_ == null)
         return null;
      return new DKColumnModel(column_.getOrdinalPosition() - 1, column_.getName(),
         getModelType(DKSqlUtil.getSqlTypeForName(column_.getDataTypeName())));
   }

   public static DKColumnModel.Type getModelType(int sqlType_) {
      switch (sqlType_) {
      case Types.INTEGER:
         return DKColumnModel.Type.NUMBER;
      case Types.BIGINT:
         return DKColumnModel.Type.NUMBER;
      case Types.REAL:
         return DKColumnModel.Type.NUMBER;
      case Types.FLOAT:
         return DKColumnModel.Type.NUMBER;
      case Types.DOUBLE:
         return DKColumnModel.Type.NUMBER;
      case Types.NUMERIC:
         return DKColumnModel.Type.NUMBER;
      case Types.DECIMAL:
         return DKColumnModel.Type.NUMBER;
      case Types.TINYINT:
         return DKColumnModel.Type.NUMBER;
      case Types.SMALLINT:
         return DKColumnModel.Type.NUMBER;
      case Types.CHAR:
         return DKColumnModel.Type.STRING;
      case Types.VARCHAR:
         return DKColumnModel.Type.STRING;
      case Types.LONGVARCHAR:
         return DKColumnModel.Type.STRING;
      case Types.DATE:
         return DKColumnModel.Type.DATE;
      case Types.TIME:
         return DKColumnModel.Type.TIME;
      case Types.TIMESTAMP:
         return DKColumnModel.Type.TIMESTAMP;
      case Types.BOOLEAN:
         return DKColumnModel.Type.BOOLEAN;
      case Types.CLOB:
         return DKColumnModel.Type.FORMATTED_STRING;

      default:
         throw new RuntimeException(String.format("unrecognized sqlType_->%s", sqlType_));
      }
   }

   public static int getSqlType(DKColumnModel.Type modelType_) {
      switch (modelType_) {
      case NUMBER:
         return Types.BIGINT;
      case STRING:
         return Types.VARCHAR;
      case DATE:
         return Types.DATE;
      case TIME:
         return Types.TIME;
      case TIMESTAMP:
         return Types.TIMESTAMP;

      default:
         throw new RuntimeException(String.format("unrecognized modelType_->%s",
            modelType_));
      }
   }
}
