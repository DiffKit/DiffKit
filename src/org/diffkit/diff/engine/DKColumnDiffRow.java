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
import java.util.Map;

import org.apache.commons.collections.OrderedMap;

import org.diffkit.common.DKValidate;

/**
 * @author jpanico
 */
public class DKColumnDiffRow {

   private final long _rowStep;
   private final Object[] _lhsRow;
   private final Object[] _rhsRow;
   private final DKTableComparison _tableComparison;
   private final List<DKColumnDiff> _diffs = new ArrayList<DKColumnDiff>();
   // lazy
   private OrderedMap _rowDisplayValues;

   public DKColumnDiffRow(long rowStep_, Object[] lhsRow_, Object[] rhsRow_,
                          DKTableComparison tableComparison_) {
      _rowStep = rowStep_;
      _lhsRow = lhsRow_;
      _rhsRow = rhsRow_;
      _tableComparison = tableComparison_;
      DKValidate.notNull(_lhsRow, _rhsRow, _tableComparison);
   }

   public long getRowStep() {
      return _rowStep;
   }

   // key side arbitrary; keyValeus guaranteed to match on both sides
   public Object[] getRowKeyValues() {
      return _tableComparison.getRowKeyValues(_lhsRow, DKSide.LEFT_INDEX);
   }

   public OrderedMap getRowDisplayValues() {
      if (_rowDisplayValues != null)
         return _rowDisplayValues;
      _rowDisplayValues = _tableComparison.getRowDisplayValues(_lhsRow, _rhsRow);
      return _rowDisplayValues;
   }

   /**
    * convenience method that gets displayValue for give columnName_
    */
   public Object getRowDisplayValue(String columnName_) {
      Map rowDisplayValues = this.getRowDisplayValues();
      if (rowDisplayValues == null)
         return null;
      return rowDisplayValues.get(columnName_);
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
