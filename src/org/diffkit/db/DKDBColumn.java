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
package org.diffkit.db;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import org.diffkit.common.DKValidate;

/**
 * @author jpanico
 */
public class DKDBColumn implements Comparable<DKDBColumn> {
   private final String _name;
   /**
    * index of column in table (starting at 1)
    */
   private final int _ordinalPosition;
   private final int _size;
   private final int _scale;
   private final boolean _nullable;
   private final String _dbTypeName;
   private DKDBTable _table;

   public DKDBColumn(String name_, int ordinalPosition_, String dbTypeName_, int size_,
                     boolean nullable_) {
      this(name_, ordinalPosition_, dbTypeName_, size_, 0, nullable_);
   }

   public DKDBColumn(String name_, int ordinalPosition_, String dbTypeName_, int size_,
                     int scale_, boolean nullable_) {
      _name = name_;
      _ordinalPosition = ordinalPosition_;
      _size = size_;
      _scale = scale_;
      _nullable = nullable_;
      _dbTypeName = dbTypeName_;
      DKValidate.notNull(_name, _dbTypeName);
   }

   /**
    * create a copy of the receiver, but use dbTypeName_ instead of value from
    * receiver
    */
   public DKDBColumn copy(String dbTypeName_) {
      if (StringUtils.equals(_dbTypeName, dbTypeName_))
         return this;
      return new DKDBColumn(this.getName(), this.getOrdinalPosition(), dbTypeName_,
         this.getSize(), this.isNullable());
   }

   public void setTable(DKDBTable table_) {
      _table = table_;
   }

   public DKDBTable getTable() {
      return _table;
   }

   public String getDBTypeName() {
      return _dbTypeName;
   }

   public String getName() {
      return _name;
   }

   public int getOrdinalPosition() {
      return _ordinalPosition;
   }

   public int getSize() {
      return _size;
   }

   public int getScale() {
      return _scale;
   }

   public boolean isNullable() {
      return _nullable;
   }

   public String toString() {
      return String.format("%s[%s:%s,%s]", ClassUtils.getShortClassName(this.getClass()),
         _ordinalPosition, _name, _dbTypeName);
   }

   public String getDescription() {
      return ReflectionToStringBuilder.toString(this);
   }

   public boolean isPartOfPrimaryKey() {
      DKDBTable table = this.getTable();
      if (table == null)
         return false;
      DKDBPrimaryKey pk = table.getPrimaryKey();
      if (pk == null)
         return false;
      return pk.containsColumnName(this.getName());
   }

   @Override
   public int compareTo(DKDBColumn target_) {
      if (_ordinalPosition < target_._ordinalPosition)
         return -1;
      if (_ordinalPosition > target_._ordinalPosition)
         return 1;
      return 0;
   }
}
