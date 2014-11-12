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

import org.diffkit.common.annot.Stateless;

/**
 * @author jpanico
 */
@Stateless
@SuppressWarnings("unchecked")
public class DKComparableComparator implements Comparator<Comparable> {

   private static final DKComparableComparator INSTANCE = new DKComparableComparator();

   public static DKComparableComparator getInstance() {
      return INSTANCE;
   }

   /**
    * null safe; null is < notnull
    * 
    * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
    */
   public int compare(Comparable lhs_, Comparable rhs_) {
      boolean lhsNull = (lhs_ == null);
      boolean rhsNull = (rhs_ == null);
      if (lhsNull && rhsNull)
         return 0;
      if (lhsNull)
         return -1;
      if (rhsNull)
         return 1;
      return lhs_.compareTo(rhs_);
   }
}
