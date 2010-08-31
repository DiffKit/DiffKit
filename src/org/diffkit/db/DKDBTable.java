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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;
import org.diffkit.util.DKSqlUtil;
import org.diffkit.util.DKSqlUtil.ReadType;
import org.diffkit.util.DKStringUtil;

/**
 * @author jpanico
 */
public class DKDBTable {

   private static final Logger LOG = LoggerFactory.getLogger(DKDBTable.class);

   private final String _catalog;
   private final String _schema;
   private final String _tableName;
   private final DKDBColumn[] _columns;
   private final DKDBPrimaryKey _primaryKey;

   public DKDBTable(String catalog_, String schema_, String tableName_,
                    DKDBColumn[] columns_) {
      this(catalog_, schema_, tableName_, columns_, null);
   }

   public DKDBTable(String catalog_, String schema_, String tableName_,
                    DKDBColumn[] columns_, DKDBPrimaryKey primaryKey_) {
      _catalog = catalog_;
      _schema = schema_;
      _tableName = tableName_;
      _columns = (DKDBColumn[]) ArrayUtils.clone(columns_);
      Arrays.sort(_columns);
      _primaryKey = primaryKey_;
      DKValidate.notNull(_tableName);
      this.validatePrimaryKey(_primaryKey, _columns);
      this.ensureRelationships(_columns);
   }

   private void ensureRelationships(DKDBColumn[] columns_) {
      if ((columns_ == null) || (columns_.length == 0))
         return;
      for (DKDBColumn column : columns_)
         column.setTable(this);
   }

   public String getCatalog() {
      return _catalog;
   }

   public String getSchema() {
      return _schema;
   }

   public String getTableName() {
      return _tableName;
   }

   public DKDBPrimaryKey getPrimaryKey() {
      return _primaryKey;
   }

   public String generateCreateDDL() {
      StringBuilder builder = new StringBuilder();
      builder.append(String.format("CREATE TABLE %s\n(\n",
         this.getSchemaQualifiedTableName()));
      for (int i = 0; i < _columns.length; i++) {
         builder.append(String.format("\t\t%s", _columns[i].generateCreateDDL()));
         if ((i < (_columns.length - 1)) || (_primaryKey != null))
            builder.append(",");
         builder.append("\n");
      }
      if (_primaryKey != null)
         builder.append(String.format("\t\t%s", _primaryKey.generateCreateDDL()));

      builder.append(")\n");
      return builder.toString();
   }

   public String generateDropDDL() {
      return String.format("DROP TABLE %s", this.getSchemaQualifiedTableName());
   }

   public String generateInsertDML(Map<String, ?> row_) {
      if (row_ == null)
         return null;
      List<String> columnNames = new ArrayList<String>();
      List<String> valueStrings = new ArrayList<String>();
      for (Map.Entry<String, ?> entry : row_.entrySet()) {
         String columnName = entry.getKey();
         columnNames.add(columnName);
         DKDBColumn column = this.getColumn(columnName);
         if (column == null)
            throw new RuntimeException(String.format(
               "columnName->%s from row_->%s is not part of this table->%s", columnName,
               row_, this));
         valueStrings.add(column.formatForSql(entry.getValue()));
      }
      return String.format("INSERT INTO %s %s\nVALUES %s",
         this.getSchemaQualifiedTableName(), DKStringUtil.toSetString(columnNames),
         DKStringUtil.toSetString(valueStrings));
   }

   public String generateSelectDML() {
      return String.format("SELECT * FROM %s", this.getSchemaQualifiedTableName());
   }

   public String getSchemaQualifiedTableName() {
      return (_schema == null ? _tableName : _schema + "." + _tableName);
   }

   /**
    * guaranteed to be in ordinalPosition order
    */
   public DKDBColumn[] getColumns() {
      return _columns;
   }

   /**
    * case insensitive
    */
   public DKDBColumn getColumn(String name_) {
      if (name_ == null)
         return null;
      for (DKDBColumn column : _columns) {
         if (column.getName().equalsIgnoreCase(name_))
            return column;
      }
      return null;
   }

   /**
    * convenience method
    */
   public int[] getPrimaryKeyColumnIndices() {
      if (_primaryKey == null)
         return null;
      String[] primaryKeyColumnNames = _primaryKey.getColumnNames();
      if ((primaryKeyColumnNames == null) || (primaryKeyColumnNames.length == 0))
         return null;
      return this.getIndicesOfColumns(primaryKeyColumnNames);
   }

   /**
    * convenience method
    */
   public int[] getIndicesOfColumns(String[] columnsNames_) {
      if ((columnsNames_ == null) || (columnsNames_.length == 0))
         return null;
      int[] indices = new int[columnsNames_.length];
      for (int i = 0; i < columnsNames_.length; i++)
         indices[i] = this.getIndexOfColumn(columnsNames_[i]);
      return indices;
   }

   /**
    * convenience method
    */
   public ReadType[] getReadTypes(String[] columnNames_) {
      if ((columnNames_ == null) || (columnNames_.length == 0))
         return null;
      ReadType[] readTypes = new ReadType[columnNames_.length];
      for (int i = 0; i < columnNames_.length; i++) {
         DKDBColumn column = this.getColumn(columnNames_[i]);
         if (column == null)
            throw new RuntimeException(String.format(
               "could not find column for columnName [%s]", columnNames_[i]));
         readTypes[i] = column.getReadType();
      }
      return readTypes;
   }

   public int getIndexOfColumn(String columnName_) {
      if (columnName_ == null)
         return -1;
      if ((_columns == null) || (_columns.length == 0))
         return -1;
      for (int i = 0; i < _columns.length; i++) {
         if (_columns[i].getName().equalsIgnoreCase(columnName_))
            return i;
      }
      return -1;
   }

   public boolean containsColumn(String columnName_) {
      return (this.getIndexOfColumn(columnName_) > -1);
   }

   public String toString() {
      String qualifiedTableName = _tableName;
      if (_schema != null)
         qualifiedTableName = String.format("%s.%s", _schema, qualifiedTableName);
      return String.format("%s[%s]", ClassUtils.getShortClassName(this.getClass()),
         qualifiedTableName);
   }

   public String getDescription() {
      return ReflectionToStringBuilder.toString(this);
   }

   /**
    * convenience method
    * 
    * @throws SQLException
    */
   public static boolean createTable(DKDBTable table_, Connection connection_)
      throws SQLException {
      LOG.debug("table_->{} connection_->{}", table_, connection_);
      if ((table_ == null) || (connection_ == null))
         return false;
      return DKSqlUtil.executeUpdate(table_.generateCreateDDL(), connection_);
   }

   /**
    * convenience method
    * 
    * @throws SQLException
    */
   public static boolean dropTable(DKDBTable table_, Connection connection_)
      throws SQLException {
      LOG.debug("table_->{} connection_->{}", table_, connection_);
      if ((table_ == null) || (connection_ == null))
         return false;
      return DKSqlUtil.executeUpdate(table_.generateDropDDL(), connection_);
   }

   public static boolean insertRow(DKDBTable table_, Map<String, ?> row_,
                                   Connection connection_) {
      if ((table_ == null) || (row_ == null) || (connection_ == null))
         return false;
      String insertSql = table_.generateInsertDML(row_);
      LOG.debug("insertSql->{}", insertSql);
      return DKSqlUtil.executeUpdate(insertSql, connection_);
   }

   public static List<Map<String, ?>> readAllRows(DKDBTable table_, Connection connection_)
      throws SQLException {
      if ((table_ == null) || (connection_ == null))
         return null;
      String selectSql = table_.generateSelectDML();
      LOG.debug("selectSql->{}", selectSql);
      return DKSqlUtil.readRows(selectSql, connection_);
   }

   private void validatePrimaryKey(DKDBPrimaryKey primaryKey_, DKDBColumn[] columns_) {
      if (primaryKey_ == null)
         return;
      String[] keyColumnNames = primaryKey_.getColumnNames();
      for (String keyColumnName : keyColumnNames) {
         boolean matched = false;
         for (DKDBColumn column : columns_) {
            if (keyColumnName.equals(column.getName()))
               matched = true;
         }
         if (!matched)
            throw new IllegalArgumentException(String.format(
               "keyColumnName->%s is not in columns->%s", keyColumnName,
               Arrays.toString(columns_)));
      }
   }
}
