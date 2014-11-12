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

import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.diff.engine.DKContext;
import org.diffkit.diff.engine.DKDiffor;

/**
 * @author jpanico
 */
public class DKEqualsDiffor implements DKDiffor {

   private static final DKEqualsDiffor INSTANCE = new DKEqualsDiffor();
   private final Logger _log = LoggerFactory.getLogger(this.getClass());
   private final boolean _isDebugEnabled = _log.isDebugEnabled();

   private DKEqualsDiffor() {
   }

   public static DKEqualsDiffor getInstance() {
      return INSTANCE;
   }

   /**
    * @see org.diffkit.diff.engine.DKDiffor#isDiff(java.lang.Object,
    *      java.lang.Object, org.diffkit.diff.engine.DKContext)
    */
   public boolean isDiff(Object lhs_, Object rhs_, DKContext context_) {
      if (_isDebugEnabled) {
         _log.debug("lhs_->{} lhs_.class->{}", lhs_,
            (lhs_ == null ? null : lhs_.getClass()));
         _log.debug("rhs_->{} rhs_.class->{}", rhs_,
            (rhs_ == null ? null : rhs_.getClass()));
      }
      boolean lhsNull = (lhs_ == null);
      boolean rhsNull = (rhs_ == null);
      if (lhsNull && rhsNull)
         return false;
      if (lhsNull || rhsNull)
         return true;
      return !lhs_.equals(rhs_);
   }

   public String toString() {
      return String.format("%s", ClassUtils.getShortClassName(this.getClass()));
   }
}
