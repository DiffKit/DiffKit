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
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import org.diffkit.common.DKValidate;
import org.diffkit.util.DKSqlUtil.ReadType;

/**
 * @author jpanico
 */
public class DKDBTable {
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

   public DKDBTable copy(Map<String, String> typeNameSubstitutionMap_) {
      if (MapUtils.isEmpty(typeNameSubstitutionMap_))
         return this;
      DKDBColumn[] copyColumns = this.copyColumns(typeNameSubstitutionMap_);
      return new DKDBTable(this.getCatalog(), this.getSchema(), this.getTableName(),
         copyColumns);
   }

   private DKDBColumn[] copyColumns(Map<String, String> typeNameSubstitutionMap_) {
      if (MapUtils.isEmpty(typeNameSubstitutionMap_))
         return this.getColumns();
      DKDBColumn[] columns = this.getColumns();
      if (ArrayUtils.isEmpty(columns))
         return columns;
      DKDBColumn[] copyColumns = new DKDBColumn[columns.length];
      for (int i = 0; i < columns.length; i++) {
         String newTypeName = typeNameSubstitutionMap_.get(columns[i].getDBTypeName());
         if (newTypeName == null)
            copyColumns[i] = columns[i];
         else
            copyColumns[i] = columns[i].copy(newTypeName);
      }
      return copyColumns;
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
    * convenience that just extracts names from underlying columns
    */
   public String[] getColumnNames() {
      DKDBColumn[] columns = this.getColumns();
      if (ArrayUtils.isEmpty(columns))
         return null;
      String[] columnNames = new String[columns.length];
      for (int i = 0; i < columns.length; i++)
         columnNames[i] = columns[i].getName();
      return columnNames;
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
   public ReadType[] getReadTypes(String[] columnNames_, DKDBDatabase connectionSource_)
      throws SQLException {
      if ((columnNames_ == null) || (columnNames_.length == 0))
         return null;
      ReadType[] readTypes = new ReadType[columnNames_.length];
      DKDBTypeInfoDataAccess typeInfoDataAccess = connectionSource_.getTypeInfoDataAccess();
      for (int i = 0; i < columnNames_.length; i++) {
         DKDBColumn column = this.getColumn(columnNames_[i]);
         if (column == null)
            throw new RuntimeException(String.format(
               "could not find column for columnName [%s]", columnNames_[i]));
         readTypes[i] = typeInfoDataAccess.getTypeInfo(column.getDBTypeName()).getReadType();
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
