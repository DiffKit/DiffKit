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

import org.apache.commons.lang.ClassUtils;

import org.diffkit.common.DKValidate;

/**
 * @author jpanico
 */
public class DKColumnComparison {

   public final DKColumnModel _lhsColumn;
   public final DKColumnModel _rhsColumn;
   public final DKDiffor _diffor;

   public DKColumnComparison(DKColumnModel lhsColumn_, DKColumnModel rhsColumn_,
                             DKDiffor diffor_) {
      _lhsColumn = lhsColumn_;
      _rhsColumn = rhsColumn_;
      _diffor = diffor_;
      DKValidate.notNull(_lhsColumn, _rhsColumn);
   }

   public boolean isDiff(Object[] lhs_, Object[] rhs_, DKContext context_) {
      if (_diffor == null)
         throw new RuntimeException("no _diffor");
      return _diffor.isDiff(lhs_[_lhsColumn._index], rhs_[_rhsColumn._index], context_);
   }

   public Object getLHValue(Object[] lhRow_) {
      return lhRow_[_lhsColumn._index];
   }

   public Object getRHValue(Object[] rhRow_) {
      return rhRow_[_rhsColumn._index];
   }

   public void validate(DKTableModel lhsTable_, DKTableModel rhsTable_) {
      if (lhsTable_ == null)
         throw new IllegalArgumentException("null lhsTable_");
      if (rhsTable_ == null)
         throw new IllegalArgumentException("null rhsTable_");
      if (!lhsTable_.containsColumn(_lhsColumn))
         throw new RuntimeException(String.format(
            "lhsTable_->%s does not contain _lhsColumn->%s", lhsTable_, _lhsColumn));
      if (!rhsTable_.containsColumn(_rhsColumn))
         throw new RuntimeException(String.format(
            "rhsTable_->%s does not contain _rhsColumn->%s", rhsTable_, _rhsColumn));
   }

   /**
    * convenience method
    * 
    * @return true if both lhs and rhs columns participate in the keys of their
    *         respective tables
    */
   public boolean columnsAreKey() {
      return (_lhsColumn.isInKey() && _rhsColumn.isInKey());
   }

   /**
    * convenience method
    * 
    * @return non-null value only if columName is the same on both sides
    */
   public String getColumnName() {
      if (!_lhsColumn.getName().equals(_rhsColumn.getName()))
         return null;
      return _lhsColumn.getName();
   }

   /**
    * convenience method
    * 
    * @return true only if isInKey == true for either side
    */
   public boolean isInKey() {
      return (_lhsColumn.isInKey() || _rhsColumn.isInKey());
   }

   public String toString() {
      return String.format("%s[%s<->%s,%s]",
         ClassUtils.getShortClassName(this.getClass()), _lhsColumn._name,
         _rhsColumn._name, ClassUtils.getShortClassName(_diffor.getClass()));
   }

   /**
    * convenience factory method assumes same TableModel on both sides
    */
   public static DKColumnComparison[] createColumnPlans(DKTableModel lhs_,
                                                        DKTableModel rhs_, int[] idxs_,
                                                        DKDiffor diffor_) {
      return createColumnPlans(lhs_, rhs_, idxs_, idxs_, diffor_);
   }

   /**
    * convenience factory method
    */
   public static DKColumnComparison[] createColumnPlans(DKTableModel lhs_,
                                                        DKTableModel rhs_,
                                                        int[] leftIdxs_,
                                                        int[] rightIdxs_, DKDiffor diffor_) {
      if (leftIdxs_.length != rightIdxs_.length)
         throw new RuntimeException(String.format(
            "leftIdxs_ length->%s does not match rightIdxs_.length->%s", leftIdxs_,
            rightIdxs_));
      DKColumnComparison[] columnPlans = new DKColumnComparison[leftIdxs_.length];
      for (int i = 0; i < leftIdxs_.length; i++)
         columnPlans[i] = createColumnPlan(lhs_, rhs_, leftIdxs_[i], rightIdxs_[i],
            diffor_);
      return columnPlans;
   }

   /**
    * convenience factory method
    */
   public static DKColumnComparison createColumnPlan(DKTableModel lhs_,
                                                     DKTableModel rhs_, int leftIdx_,
                                                     int rightIdx_, DKDiffor diffor_) {
      return new DKColumnComparison(lhs_.getColumn(leftIdx_), rhs_.getColumn(rightIdx_),
         diffor_);
   }
}
