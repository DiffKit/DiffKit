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
package org.diffkit.diff.engine;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.OrderedMap;

import org.diffkit.common.DKValidate;

/**
 * @author jpanico
 */
public class DKColumnDiffRow {

   private final long _rowStep;
   private final Object[] _rowKeyValues;
   private final OrderedMap _rowDisplayValues;
   private final DKTableComparison _tableComparison;
   private final List<DKColumnDiff> _diffs = new ArrayList<DKColumnDiff>();

   public DKColumnDiffRow(long rowStep_, Object[] rowKeyValues_,
                          OrderedMap rowDisplayValues_, DKTableComparison tableComparison_) {
      _rowStep = rowStep_;
      _rowKeyValues = rowKeyValues_;
      _rowDisplayValues = rowDisplayValues_;
      _tableComparison = tableComparison_;
      DKValidate.notNull(_rowKeyValues, _rowDisplayValues, _tableComparison);
   }

   public long getRowStep() {
      return _rowStep;
   }

   public Object[] getRowKeyValues() {
      return _rowKeyValues;
   }

   public OrderedMap getRowDisplayValues() {
      return _rowDisplayValues;
   }

   /**
    * convenience method that gets displayValue for give columnName_
    */
   public Object getRowDisplayValue(String columnName_) {
      return _rowDisplayValues.get(columnName_);
   }

   public DKTableComparison getTableComparison() {
      return _tableComparison;
   }

   public DKColumnDiff createDiff(int columnStep_, Object lhs_, Object rhs_) {
      DKColumnDiff diff = new DKColumnDiff(this, columnStep_, lhs_, rhs_);
      _diffs.add(diff);
      return diff;
   }

   public String getColumnName(int columnStep_) {
      return _tableComparison.getColumnName(columnStep_);
   }
}
