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
package org.diffkit.diff.diffor;

import org.apache.commons.lang.ClassUtils;

import org.diffkit.diff.engine.DKContext;
import org.diffkit.diff.engine.DKDiffor;

/**
 * @author jpanico
 */
public class DKNumberDiffor implements DKDiffor {

   private final double _tolerance;
   private final boolean _nullIsZero;

   public DKNumberDiffor() {
      this(0, false);
   }

   public DKNumberDiffor(double tolerance_, boolean nullIsZero_) {
      _tolerance = tolerance_;
      _nullIsZero = nullIsZero_;
      if (_tolerance < 0)
         throw new IllegalArgumentException(String.format("_tolerance->%s cannot be < 0",
            _tolerance));
   }

   /**
    */
   public boolean isDiff(Object lhs_, Object rhs_, DKContext context_) {
      Number lhs = (Number) lhs_;
      Number rhs = (Number) rhs_;
      boolean lhsIsNull = (lhs == null);
      boolean rhsIsNull = (rhs == null);

      if (lhsIsNull && rhsIsNull)
         return false;
      if (!_nullIsZero) {
         if (lhsIsNull || (rhsIsNull))
            return true;
      }
      double lhValue = (lhsIsNull ? 0 : lhs.doubleValue());
      double rhValue = (rhsIsNull ? 0 : rhs.doubleValue());
      double diff = lhValue - rhValue;
      if (diff == 0)
         return false;
      diff = (diff >= 0 ? diff : -1 * diff);
      return (diff > _tolerance);
   }

   public String toString() {
      return String.format("%s[%s]", ClassUtils.getShortClassName(this.getClass()),
         _tolerance);
   }
}
