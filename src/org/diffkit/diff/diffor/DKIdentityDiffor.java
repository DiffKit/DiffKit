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
public class DKIdentityDiffor implements DKDiffor {

   private static final DKIdentityDiffor INSTANCE = new DKIdentityDiffor();

   private DKIdentityDiffor() {
   }

   public static DKIdentityDiffor getInstance() {
      return INSTANCE;
   }

   /**
    * @see org.diffkit.diff.engine.DKDiffor#isDiff(java.lang.Object,
    *      java.lang.Object, org.diffkit.diff.engine.DKContext)
    */
   public boolean isDiff(Object lhs_, Object rhs_, DKContext context_) {
      return !(lhs_ == rhs_);
   }

   public String toString() {
      return String.format("%s", ClassUtils.getShortClassName(this.getClass()));
   }
}
