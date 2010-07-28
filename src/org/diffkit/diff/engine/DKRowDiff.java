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

import org.apache.commons.collections.OrderedMap;

import org.diffkit.common.DKValidate;

/**
 * @author jpanico
 */
public class DKRowDiff implements DKDiff {

   private final long _rowStep;
   private final Object[] _keyValues;
   private final OrderedMap _displayValues;
   private final DKSide _side;
   private final DKTableComparison _tableComparison;

   public DKRowDiff(long rowStep_, Object[] keyValues_, OrderedMap displayValues_,
                    DKSide side_, DKTableComparison tableComparison_) {
      _rowStep = rowStep_;
      _displayValues = displayValues_;
      _keyValues = keyValues_;
      _side = side_;
      _tableComparison = tableComparison_;
      DKValidate.notNull(_displayValues, _keyValues, _side, _tableComparison);
   }

   public long getRowStep() {
      return _rowStep;
   }

   public long getColumnStep() {
      return -1;
   }

   public Object[] getRowKeyValues() {
      return _keyValues;
   }

   public OrderedMap getRowDisplayValues() {
      return _displayValues;
   }

   public DKSide getSide() {
      return _side;
   }

   public Kind getKind() {
      return Kind.ROW_DIFF;
   }

   public DKTableComparison getTableComparison() {
      return _tableComparison;
   }

   public String getRowDisplayString() {
      return _displayValues.toString();
   }

   public int compareTo(DKDiff target_) {
      if (target_ == null)
         return +1;
      long targetRowStep = target_.getRowStep();
      if (_rowStep > targetRowStep)
         return +1;
      else if (_rowStep < targetRowStep)
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
