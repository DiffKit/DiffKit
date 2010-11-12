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

import java.sql.SQLException;

import org.diffkit.common.DKUserException;
import org.diffkit.common.DKValidate;
import org.diffkit.db.DKDBColumn;
import org.diffkit.db.DKDBFlavor;
import org.diffkit.db.DKDBPrimaryKey;
import org.diffkit.db.DKDBTable;
import org.diffkit.db.DKDBType;
import org.diffkit.db.DKDBTypeInfoDataAccess;
import org.diffkit.diff.engine.DKColumnModel;
import org.diffkit.diff.engine.DKTableModel;

/**
 * @author jpanico
 */
public class DKTableModelUtil {

   private DKTableModelUtil() {
   }

   public static DKDBTable createDefaultDBTable(DKDBTypeInfoDataAccess typeInfoDataAccess_,
                                                String tableName_,
                                                DKTableModel tableModel_)
      throws SQLException {
      DKValidate.notNull(typeInfoDataAccess_);
      if (tableModel_ == null)
         return null;
      DKColumnModel[] columnsModels = tableModel_.getColumns();
      DKDBColumn[] columns = new DKDBColumn[columnsModels.length];
      for (int i = 0; i < columns.length; i++)
         columns[i] = createDefaultDBColumn(columnsModels[i]);
      DKDBPrimaryKey primaryKey = createDefaultPrimaryKey(tableName_, tableModel_);
      return new DKDBTable(null, null, tableName_, columns, primaryKey);
   }

   public static DKDBColumn createDefaultDBColumn(DKColumnModel columnModel_)
      throws SQLException {
      if (columnModel_ == null)
         return null;
      return new DKDBColumn(columnModel_._name, columnModel_._index + 1, getSqlType(
         columnModel_._type).toString(), 128, true);
   }

   public static DKDBPrimaryKey createDefaultPrimaryKey(String tableName_,
                                                        DKTableModel tableModel_) {
      if (tableModel_ == null)
         return null;
      return new DKDBPrimaryKey("pk_" + tableName_, tableModel_.getKeyColumnNames());
   }

   public static DKTableModel createDefaultTableModel(DKDBFlavor flavor_,
                                                      DKDBTable table_,
                                                      String[] keyColumnNames_) {
      if (table_ == null)
         return null;
      String tableName = table_.getSchemaQualifiedTableName();
      DKDBColumn[] columns = table_.getColumns();
      DKColumnModel[] columnModels = new DKColumnModel[columns.length];
      for (int i = 0; i < columns.length; i++)
         columnModels[i] = createDefaultColumnModel(flavor_, columns[i]);
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

   public static DKColumnModel createDefaultColumnModel(DKDBFlavor flavor_,
                                                        DKDBColumn column_) {
      if (column_ == null)
         return null;
      return new DKColumnModel(column_.getOrdinalPosition() - 1, column_.getName(),
         getModelType(DKDBType.getType(flavor_, column_.getDBTypeName())));
   }

   public static DKColumnModel.Type getModelType(DKDBType dbType_) {
      switch (dbType_) {
      case INTEGER:
         return DKColumnModel.Type.INTEGER;
      case BIGINT:
         return DKColumnModel.Type.INTEGER;
      case REAL:
         return DKColumnModel.Type.REAL;
      case FLOAT:
         return DKColumnModel.Type.REAL;
      case DOUBLE:
         return DKColumnModel.Type.REAL;
      case NUMERIC:
         return DKColumnModel.Type.DECIMAL;
      case DECIMAL:
         return DKColumnModel.Type.DECIMAL;
      case BIT:
         return DKColumnModel.Type.INTEGER;
      case TINYINT:
         return DKColumnModel.Type.INTEGER;
      case SMALLINT:
         return DKColumnModel.Type.INTEGER;
      case _MYSQL_INT:
         return DKColumnModel.Type.INTEGER;
      case _SQLSERVER_INT:
         return DKColumnModel.Type.INTEGER;
      case _ORACLE_NUMBER:
         return DKColumnModel.Type.DECIMAL;
      case CHAR:
         return DKColumnModel.Type.STRING;
      case VARCHAR:
         return DKColumnModel.Type.STRING;
      case LONGVARCHAR:
         return DKColumnModel.Type.STRING;
      case _ORACLE_VARCHAR2:
         return DKColumnModel.Type.STRING;
      case DATE:
         return DKColumnModel.Type.DATE;
      case TIME:
         return DKColumnModel.Type.TIME;
      case TIMESTAMP:
         return DKColumnModel.Type.TIMESTAMP;
      case BOOLEAN:
         return DKColumnModel.Type.BOOLEAN;
      case CLOB:
         return DKColumnModel.Type.TEXT;
      case _MYSQL_TEXT:
         return DKColumnModel.Type.TEXT;
      case _SQLSERVER_TEXT:
         return DKColumnModel.Type.TEXT;
      case _SQLSERVER_DATETIME:
         return DKColumnModel.Type.TIMESTAMP;
      default:
         throw new RuntimeException(String.format("unrecognized dbType_->%s", dbType_));
      }
   }

   public static DKDBType getSqlType(DKColumnModel.Type modelType_) {
      switch (modelType_) {
      case INTEGER:
         return DKDBType.BIGINT;
      case STRING:
         return DKDBType.VARCHAR;
      case REAL:
         return DKDBType.REAL;
      case DECIMAL:
         return DKDBType.NUMERIC;
      case DATE:
         return DKDBType.DATE;
      case TIME:
         return DKDBType.TIME;
      case TIMESTAMP:
         return DKDBType.TIMESTAMP;

      default:
         throw new RuntimeException(String.format("unrecognized modelType_->%s",
            modelType_));
      }
   }
}
