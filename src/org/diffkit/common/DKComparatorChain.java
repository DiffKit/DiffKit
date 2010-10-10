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
package org.diffkit.common;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.lang.ClassUtils;

/**
 * Subclass commons ComparatorChain just to get a decent looking toString()
 * 
 * @author jpanico
 */
public class DKComparatorChain extends ComparatorChain {

   private static final long serialVersionUID = 1L;

   public DKComparatorChain() {
      super();
   }

   public DKComparatorChain(Comparator... comparators_) {
      super(Arrays.asList(comparators_));
   }

   public String toString() {
      return String.format("%s[%s]", ClassUtils.getShortClassName(this.getClass()),
         comparatorChain);
   }
}
