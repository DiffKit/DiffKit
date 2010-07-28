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

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import org.diffkit.common.DKValidate;
import org.diffkit.util.DKSqlUtil;

/**
 * @author jpanico
 */
public class DKDBColumn implements Comparable<DKDBColumn> {
   private final String _name;
   /**
    * index of column in table (starting at 1)
    */
   private final int _ordinalPosition;
   /**
    * from java.sql.Types
    */
   private final String _dataTypeName;
   private final int _size;
   private final boolean _nullable;
   private DKDBTable _table;

   public DKDBColumn(String name_, int ordinalPosition_, String dataTypeName_, int size_,
                     boolean nullable_) {
      _name = name_;
      _ordinalPosition = ordinalPosition_;
      _dataTypeName = dataTypeName_;
      _size = size_;
      _nullable = nullable_;
      DKValidate.notNull(_name);
   }

   public void setTable(DKDBTable table_) {
      _table = table_;
   }

   public DKDBTable getTable() {
      return _table;
   }

   public String getName() {
      return _name;
   }

   public int getOrdinalPosition() {
      return _ordinalPosition;
   }

   public String getDataTypeName() {
      return _dataTypeName;
   }

   public int getSize() {
      return _size;
   }

   public boolean isNullable() {
      return _nullable;
   }

   public String generateCreateDDL() {
      StringBuilder builder = new StringBuilder();
      String sizeSpecifier = (_size < 0 ? "" : String.format("(%s)", _size));
      builder.append(String.format("%s\t\t%s%s", _name, _dataTypeName, sizeSpecifier));
      return builder.toString();
   }

   public String formatForSql(Object value_) {
      return DKSqlUtil.formatForSql(value_, DKSqlUtil.getSqlTypeForName(_dataTypeName));
   }

   public String toString() {
      return String.format("%s[%s:%s,%s]", ClassUtils.getShortClassName(this.getClass()),
         _ordinalPosition, _name, _dataTypeName);
   }

   public String getDescription() {
      return ReflectionToStringBuilder.toString(this);
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
