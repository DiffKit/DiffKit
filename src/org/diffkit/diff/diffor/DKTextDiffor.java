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

import org.apache.commons.lang.StringUtils;

import org.diffkit.diff.engine.DKContext;
import org.diffkit.diff.engine.DKDiffor;

/**
 * @author jpanico
 */
public class DKTextDiffor implements DKDiffor {

   private final String _ignoreChars;

   public DKTextDiffor(String ignoreChars_) {
      _ignoreChars = ignoreChars_;
   }

   /**
    * @see org.diffkit.diff.engine.DKDiffor#isDiff(java.lang.Object,
    *      java.lang.Object, org.diffkit.diff.engine.DKContext)
    */
   public boolean isDiff(Object lhs_, Object rhs_, DKContext context_) {
      boolean lhsNull = (lhs_ == null);
      boolean rhsNull = (rhs_ == null);
      if (lhsNull && rhsNull)
         return false;
      if (lhsNull || rhsNull)
         return true;
      boolean equals = lhs_.equals(rhs_);
      if (equals)
         return false;
      if (StringUtils.isEmpty(_ignoreChars))
         return !equals;
      String lhsString = StringUtils.replaceChars((String) lhs_, _ignoreChars, "");
      String rhsString = StringUtils.replaceChars((String) rhs_, _ignoreChars, "");
      return !lhsString.equals(rhsString);
   }

}
