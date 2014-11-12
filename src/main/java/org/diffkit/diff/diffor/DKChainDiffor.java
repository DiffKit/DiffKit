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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;

import org.diffkit.common.DKValidate;
import org.diffkit.diff.engine.DKContext;
import org.diffkit.diff.engine.DKDiffor;

/**
 * Implements boolean AND of all DKDiffors in chain. Can be used to represent
 * the idea of "successive relaxation".
 * 
 * @author jpanico
 */
public class DKChainDiffor implements DKDiffor {

   private final DKDiffor[] _diffors;

   public DKChainDiffor(List<DKDiffor> diffors_) {
      DKValidate.notEmpty(diffors_);
      _diffors = diffors_.toArray(new DKDiffor[diffors_.size()]);
   }

   public DKChainDiffor(DKDiffor... diffors_) {
      _diffors = (DKDiffor[]) ArrayUtils.clone(diffors_);
      DKValidate.notNull((Object) _diffors);
   }

   public boolean isDiff(Object lhs_, Object rhs_, DKContext context_) {
      for (DKDiffor diffor : _diffors) {
         if (!diffor.isDiff(lhs_, rhs_, context_))
            return false;
      }
      return true;
   }

   public String toString() {
      return String.format("%s[%s]", ClassUtils.getShortClassName(this.getClass()),
         Arrays.toString(_diffors));
   }
}
