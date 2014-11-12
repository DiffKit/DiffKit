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
package org.diffkit.common;

import java.util.Comparator;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * @author jpanico
 */
public class DKElementComparator<T> implements Comparator<T[]> {
   private final int _lhsIdx;
   private final int _rhsIdx;
   private final Comparator<T> _comparator;

   public DKElementComparator(int lhsIdx_, int rhsIdx_, Comparator<T> comparator_) {
      _lhsIdx = lhsIdx_;
      _rhsIdx = rhsIdx_;
      _comparator = comparator_;
      DKValidate.notNull(_comparator);
   }

   public int compare(T[] lhs_, T[] rhs_) {
      T lhsValue = lhs_[_lhsIdx];
      T rhsValue = rhs_[_rhsIdx];
      boolean lhsNull = (lhsValue == null) ? true : false;
      boolean rhsNull = (rhsValue == null) ? true : false;
      if (lhsNull && rhsNull)
         return 0;
      else if (lhsNull)
         return -1;
      else if (rhsNull)
         return 1;

      return _comparator.compare(lhsValue, rhsValue);
   }

   public String toString() {
      return String.format("%s(%s,%s,%s)", ClassUtils.getShortClassName(this.getClass()),
         _lhsIdx, _rhsIdx, ClassUtils.getShortClassName(_comparator.getClass()));
   }

   public String getDescription() {
      return ReflectionToStringBuilder.toString(this);
   }
}
