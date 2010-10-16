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
package org.diffkit.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;
import org.diffkit.util.DKSqlUtil;
import org.diffkit.util.DKStringUtil;

/**
 * @author jpanico
 */
public class DKSqlGenerator {

   private final DKDBDatabase _database;
   private final Logger _log = LoggerFactory.getLogger(DKDBTable.class);

   public DKSqlGenerator(DKDBDatabase database_) {
      _database = database_;
      DKValidate.notNull(database_);
   }

   public String generateCreateDDL(DKDBColumn column_) throws SQLException {
      StringBuilder builder = new StringBuilder();
      String notNullSpecifier = column_.isPartOfPrimaryKey() ? " NOT NULL" : "";
      builder.append(String.format("%s\t\t%s%s%s", column_.getName(),
         column_.getDBTypeName(), this.generateSizeSpecifier(column_), notNullSpecifier));
      return builder.toString();
   }

   public String generateSizeSpecifier(DKDBColumn column_) throws SQLException {
      if (column_ == null)
         return null;
      DKDBType colType = _database.getType(column_.getDBTypeName());
      if (colType == null)
         _log.warn("no colType for column_->{}", column_);
      else if (colType.ignoresLengthSpecifier()) {
         return "";
      }
      int size = column_.getSize();
      int scale = column_.getScale();
      if (size <= 0)
         return "";
      if (scale <= 0)
         return String.format("(%s)", size);
      return String.format("(%s,%s)", size, scale);
   }

   public String generateDropDDL(DKDBTable table_) {
      return String.format("DROP TABLE %s", table_.getSchemaQualifiedTableName());
   }

   public String generateCreateDDL(DKDBTable table_) throws SQLException {
      StringBuilder builder = new StringBuilder();
      builder.append(String.format("CREATE TABLE %s\n(\n",
         table_.getSchemaQualifiedTableName()));
      DKDBColumn[] columns = table_.getColumns();
      DKDBPrimaryKey primaryKey = table_.getPrimaryKey();
      for (int i = 0; i < columns.length; i++) {
         builder.append(String.format("\t\t%s", this.generateCreateDDL(columns[i])));
         if ((i < (columns.length - 1)) || (primaryKey != null))
            builder.append(",");
         builder.append("\n");
      }
      if (primaryKey != null)
         builder.append(String.format("\t\t%s", this.generateCreateDDL(primaryKey)));

      builder.append(")\n");
      return builder.toString();
   }

   public String generateCreateDDL(DKDBPrimaryKey primaryKey_) {
      if (primaryKey_ == null)
         return "";
      StringBuilder builder = new StringBuilder();
      builder.append(String.format("CONSTRAINT %s PRIMARY KEY (", primaryKey_.getName()));
      String[] columnNames = primaryKey_.getColumnNames();
      for (int i = 0; i < columnNames.length; i++) {
         builder.append(columnNames[i]);
         if (i < (columnNames.length - 1))
            builder.append(",");
      }
      builder.append(")");
      return builder.toString();
   }

   public String generateInsertDML(Map<String, ?> row_, DKDBTable table_)
      throws SQLException {
      if (row_ == null)
         return null;
      List<String> columnNames = new ArrayList<String>();
      List<String> valueStrings = new ArrayList<String>();
      DKDBTypeInfoDataAccess typeInfoDataAccess = _database.getTypeInfoDataAccess();
      for (Map.Entry<String, ?> entry : row_.entrySet()) {
         String columnName = entry.getKey();
         columnNames.add(columnName);
         DKDBColumn column = table_.getColumn(columnName);
         if (column == null)
            throw new RuntimeException(String.format(
               "columnName->%s from row_->%s is not part of this table->%s", columnName,
               row_, this));
         DKDBTypeInfo typeInfo = typeInfoDataAccess.getTypeInfo(column.getDBTypeName());
         valueStrings.add(DKSqlUtil.formatForSql(entry.getValue(),
            typeInfo.getWriteType()));
      }
      return String.format("INSERT INTO %s %s\nVALUES %s",
         table_.getSchemaQualifiedTableName(), DKStringUtil.toSetString(columnNames),
         DKStringUtil.toSetString(valueStrings));
   }

   public String generateSelectDML(DKDBTable table_) {
      return String.format("SELECT * FROM %s", table_.getSchemaQualifiedTableName());
   }
}
