/**
 * Copyright 2010-2011 Joseph Panico
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
package org.diffkit.diff.engine;

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;

import org.diffkit.common.DKValidate;
import org.diffkit.common.annot.Immutable;
import org.diffkit.common.kvc.DKKeyValueCoder;
import org.diffkit.diff.engine.DKColumnModel.Type;

/**
 * @author jpanico
 */
@Immutable
public class DKTableModel {

   private final String _name;
   private final DKColumnModel[] _columns;
   /**
    * indexes into _columns
    */
   private final int[] _key;

   public DKTableModel(String name_, DKColumnModel[] columns_, int[] key_) {
      DKValidate.notEmpty((Object[]) columns_);
      DKValidate.notEmpty(key_);
      _name = name_;
      _columns = (columns_ == null) ? null : Arrays.copyOf(columns_, columns_.length);
      _key = key_;
      this.validateColumnIndices(_key);
      this.ensureRelationships(_columns);
   }

   /**
    * @return a deep copy of the receiver
    */
   public DKTableModel copy() {
      return new DKTableModel(_name, this.copyColumns(), Arrays.copyOf(_key, _key.length));
   }

   /**
    * @return a deep copy of the receiver, except with newKeyColumnNames for
    *         key, instead of receiver's values. newKeyColumnNames_ will be
    *         validated up front.
    */
   public DKTableModel copyWithNewKey(String[] newKeyColumnNames_) {
      this.validateColumnNames(newKeyColumnNames_);
      int[] newKey = this.getColumnIndexes(newKeyColumnNames_);
      return new DKTableModel(_name, this.copyColumns(), newKey);
   }

   private DKColumnModel[] copyColumns() {
      DKColumnModel[] newColumns = new DKColumnModel[_columns.length];
      for (int i = 0; i < _columns.length; i++)
         newColumns[i] = _columns[i].copy();
      return newColumns;
   }

   private void ensureRelationships(DKColumnModel[] columns_) {
      if ((columns_ == null) || (columns_.length == 0))
         return;
      for (DKColumnModel column : columns_)
         DKKeyValueCoder.getInstance().setValue("_table", this, column);
   }

   public String getName() {
      return _name;
   }

   /**
    * convenience method that checks if column[0] isRowNum()
    */
   public boolean hasRowNum() {
      if (ArrayUtils.isEmpty(_columns))
         return false;
      return _columns[0].isRowNum();
   }

   /**
    * convenience method that checks if key.length == 1, and key column isRowNum
    */
   public boolean keyIsRowNum() {
      int[] key = this.getKey();
      if (ArrayUtils.isEmpty(key))
         return false;
      if (key.length > 1)
         return false;
      DKColumnModel keyColumn = this.getColumn(key[0]);
      return keyColumn.isRowNum();
   }

   public DKColumnModel[] getColumns() {
      return Arrays.copyOf(_columns, _columns.length);
   }

   public DKColumnModel getColumn(int index_) {
      for (DKColumnModel column : _columns) {
         if (column._index == index_)
            return column;
      }
      return null;
   }

   public DKColumnModel getColumn(String columnName_) {
      if (columnName_ == null)
         return null;
      if ((_columns == null) || (_columns.length == 0))
         return null;
      for (DKColumnModel column : _columns) {
         if (column.getName().equals(columnName_))
            return column;
      }
      return null;
   }

   public DKColumnModel[] getColumns(int[] indxs_) {
      if (indxs_ == null)
         return null;
      this.validateColumnIndices(indxs_);
      DKColumnModel[] columns = new DKColumnModel[indxs_.length];
      for (int i = 0; i < indxs_.length; i++)
         columns[i] = this.getColumn(indxs_[i]);
      return columns;
   }

   /**
    * convenience method that extracts names from underlying DKColumns
    */
   public String[] getColumnNames() {
      String[] columnNames = new String[_columns.length];
      for (int i = 0; i < _columns.length; i++) {
         columnNames[i] = _columns[i].getName();
      }
      return columnNames;
   }

   /**
    * convenience method that extracts types from underlying DKColumns
    */
   public Type[] getColumnTypes() {
      Type[] columnTypes = new Type[_columns.length];
      for (int i = 0; i < _columns.length; i++) {
         columnTypes[i] = _columns[i].getType();
      }
      return columnTypes;
   }

   public int[] getKey() {
      return Arrays.copyOf(_key, _key.length);
   }

   /**
    * convenience method that assumes simple (1 column) key. throws exception if
    * key is compound
    */
   public DKColumnModel getKeyColumn() {
      int[] key = this.getKey();
      if (key == null)
         return null;
      if (key.length > 1)
         throw new RuntimeException("compound key!");
      return this.getColumn(key[0]);
   }

   public Object[] getKeyValues(Object[] row_) {
      if (row_ == null)
         return null;
      Object[] keyValues = new Object[_key.length];
      for (int i = 0; i < _key.length; i++)
         keyValues[i] = row_[_key[i]];
      return keyValues;
   }

   public String toString() {
      return String.format("%s[%s]", ClassUtils.getShortClassName(this.getClass()), _name);
   }

   public String getDescription() {
      return String.format("%s[name=%s, columnCount=%s, key=%s]",
         ClassUtils.getShortClassName(this.getClass()), _name, _columns.length,
         Arrays.toString(this.getKeyColumnNames()));
   }

   /**
    * @throws RuntimeException
    *            if it finds any element of columnIndices_ that does not
    *            correspond to a KDColumnModel, in the receiver, having the same
    *            index value
    */
   public void validateColumnIndices(int[] columnIndices_) {
      if (columnIndices_ == null)
         return;
      for (int index : columnIndices_) {
         DKColumnModel column = this.getColumn(index);
         if (column == null)
            throw new RuntimeException(String.format(
               "couldn't find Column for index->%s", index));
      }
   }

   /**
    * @throws RuntimeException
    *            if it finds any element of columnNames_ that the receiver
    *            cannot find a column for
    */
   public void validateColumnNames(String[] columnNames_) {
      if (ArrayUtils.isEmpty(columnNames_))
         return;
      for (String name : columnNames_) {
         DKColumnModel column = this.getColumn(name);
         if (column == null)
            throw new RuntimeException(String.format("couldn't find Column for name->%s",
               name));
      }
   }

   public String[] getKeyColumnNames() {
      if (_key == null)
         return null;
      String[] keyColumnNames = new String[_key.length];
      for (int i = 0; i < _key.length; i++) {
         keyColumnNames[i] = _columns[_key[i]]._name;
      }
      return keyColumnNames;
   }

   /**
    * convenience method
    */
   public boolean containsColumn(DKColumnModel column_) {
      if (column_ == null)
         return false;
      return ArrayUtils.contains(_columns, column_);
   }

   /**
    * convenience method
    */
   public boolean containsColumn(String columnName_) {
      if (columnName_ == null)
         return false;
      return (this.getColumn(columnName_) != null);
   }

   /**
    * convenience method
    */
   public int getColumnIndex(DKColumnModel column_) {
      if (column_ == null)
         return -1;
      if ((_columns == null) || (_columns.length == 0))
         return -1;
      return ArrayUtils.indexOf(_columns, column_);
   }

   /**
    * convenience method
    */
   public int[] getColumnIndexes(String[] columnNames_) {
      return DKColumnModel.getColumnIndexes(_columns, columnNames_);
   }

   /**
    * convenience method
    * 
    * @return true if column_ is one of the columns that comprises key
    */
   public boolean isInKey(DKColumnModel column_) {
      if (column_ == null)
         return false;
      if ((_key == null) || (_key.length == 0))
         return false;
      int columnIdx = this.getColumnIndex(column_);
      if (columnIdx < 0)
         return false;
      return ArrayUtils.contains(_key, columnIdx);
   }

   /**
    * convenience factory method that creates generic column names based on
    * index and then calls createGenericStringModel(String[], int[])
    */
   public static DKTableModel createGenericStringModel(int columnCount_, int[] key_) {
      if (columnCount_ <= 0)
         return null;
      String[] columnNames = new String[columnCount_];
      for (int i = 0; i < columnNames.length; i++)
         columnNames[i] = String.format("column_%s", i + 1);

      return createGenericStringModel(columnNames, key_);
   }

   /**
    * convenience factory method that creates DKTableModel having String type
    * columns using columnNames_ and key = key_
    */
   public static DKTableModel createGenericStringModel(String[] columnNames_, int[] key_) {
      if ((columnNames_ == null) || (columnNames_.length == 0))
         return null;
      if (key_ == null)
         return null;
      for (int i = 0; i < key_.length; i++) {
         if ((key_[i] < 0) || (key_[i] >= columnNames_.length))
            throw new RuntimeException(String.format(
               "key index->%s not in range for columnNames_.length->%s", key_[i],
               columnNames_.length));
      }
      DKColumnModel[] columns = new DKColumnModel[columnNames_.length];
      for (int i = 0; i < columnNames_.length; i++)
         columns[i] = new DKColumnModel(i, columnNames_[i], Type.STRING);

      return new DKTableModel("GENERIC_STRING_MODEL", columns, key_);
   }
}
