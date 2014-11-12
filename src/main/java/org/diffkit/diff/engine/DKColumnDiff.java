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

import org.apache.commons.collections.OrderedMap;

import org.diffkit.common.DKValidate;

/**
 * @author jpanico
 */
public class DKColumnDiff implements DKDiff {

   private final DKColumnDiffRow _row;
   private final int _columnStep;
   private final Object _lhs;
   private final Object _rhs;

   public DKColumnDiff(DKColumnDiffRow row_, int columnStep_, Object lhs_, Object rhs_) {
      _row = row_;
      _columnStep = columnStep_;
      _lhs = lhs_;
      _rhs = rhs_;
      DKValidate.notNull(_row);
   }

   public DKColumnDiffRow getRow() {
      return _row;
   }

   public OrderedMap getRowDisplayValues() {
      return _row.getRowDisplayValues();
   }

   /**
    * convenience method that delegates to underlying ColumnDiffRow
    */
   public Object getRowDisplayValue(String columnName_) {
      return _row.getRowDisplayValue(columnName_);
   }

   public Object[] getRowKeyValues() {
      return _row.getRowKeyValues();
   }

   public Kind getKind() {
      return Kind.COLUMN_DIFF;
   }

   public DKTableComparison getTableComparison() {
      return _row.getTableComparison();
   }

   public long getRowStep() {
      return _row.getRowStep();
   }

   public long getColumnStep() {
      return _columnStep;
   }

   public String getColumnName() {
      return _row.getColumnName(_columnStep);
   }

   public Object getLhs() {
      return _lhs;
   }

   public Object getRhs() {
      return _rhs;
   }

   public String getRowDisplayString() {
      return this.getRowDisplayValues().toString();
   }

   public int compareTo(DKDiff target_) {
      if (target_ == null)
         return +1;
      long targetRowStep = target_.getRowStep();
      long thisRowStep = this.getRowStep();
      if (thisRowStep > targetRowStep)
         return +1;
      else if (thisRowStep < targetRowStep)
         return -1;
      long targetColumnStep = target_.getColumnStep();
      long thisColumnStep = this.getColumnStep();
      if (thisColumnStep > targetColumnStep)
         return +1;
      else if (thisColumnStep < targetColumnStep)
         return -1;
      return 0;
   }
}
