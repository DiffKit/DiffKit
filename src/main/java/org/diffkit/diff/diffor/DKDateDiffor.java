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
package org.diffkit.diff.diffor;

import java.util.Date;

import org.apache.commons.lang.ClassUtils;

import org.diffkit.common.DKCalendarSpan;
import org.diffkit.common.DKValidate;
import org.diffkit.diff.engine.DKContext;
import org.diffkit.diff.engine.DKDiffor;

/**
 * @author jpanico
 */
public class DKDateDiffor implements DKDiffor {

   private final DKCalendarSpan _tolerance;

   public DKDateDiffor() {
      this(new DKCalendarSpan(0, DKCalendarSpan.Unit.MILLISECOND));
   }

   public DKDateDiffor(DKCalendarSpan tolerance_) {
      _tolerance = tolerance_;
      DKValidate.notNull(_tolerance);
   }

   /**
    */
   public boolean isDiff(Object lhs_, Object rhs_, DKContext context_) {
      Date lhs = (Date) lhs_;
      Date rhs = (Date) rhs_;
      boolean lhsIsNull = (lhs == null);
      boolean rhsIsNull = (rhs == null);

      if (lhsIsNull && rhsIsNull)
         return false;
      if (lhsIsNull || (rhsIsNull))
         return true;

      long lhValue = lhs.getTime();
      long rhValue = rhs.getTime();
      double diff = lhValue - rhValue;
      if (diff == 0)
         return false;
      diff = (diff >= 0 ? diff : -1 * diff);
      return (diff > _tolerance._spanMillis);
   }

   public String toString() {
      return String.format("%s[%s]", ClassUtils.getShortClassName(this.getClass()),
         _tolerance);
   }
}
