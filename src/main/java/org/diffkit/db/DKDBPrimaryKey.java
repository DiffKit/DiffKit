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

import org.apache.commons.lang.ArrayUtils;

import org.diffkit.common.DKValidate;

/**
 * @author jpanico
 */
public class DKDBPrimaryKey {
   private final String _name;
   private final String[] _columnNames;

   public DKDBPrimaryKey(String name_, String[] columnNames_) {
      _name = name_;
      _columnNames = columnNames_;
      DKValidate.notNull(_name, _columnNames);
   }

   public String getName() {
      return _name;
   }

   public String[] getColumnNames() {
      return _columnNames;
   }

   public boolean containsColumnName(String columnName_) {
      if (columnName_ == null)
         return false;
      return ArrayUtils.contains(_columnNames, columnName_);
   }
}
