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
import java.util.Map;

/**
 * compare two maps based on the values associated with one key
 * 
 * @author jpanico
 */
@SuppressWarnings("unchecked")
public class DKMapKeyValueComparator implements Comparator<Map> {

   private final Object _key;

   public DKMapKeyValueComparator(Object key_) {
      _key = key_;
      DKValidate.notNull(_key);
   }

   /**
    * null safe; null is < notnull
    * 
    * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
    */
   public int compare(Map lhs_, Map rhs_) {
      Comparable lhValue = (Comparable) (lhs_ == null ? null : lhs_.get(_key));
      Comparable rhValue = (Comparable) (rhs_ == null ? null : rhs_.get(_key));
      boolean lhValueNull = (lhValue == null);
      boolean rhValueNull = (rhValue == null);

      if (lhValueNull && rhValueNull)
         return 0;
      if (lhValueNull)
         return -1;
      if (rhValueNull)
         return +1;
      return lhValue.compareTo(rhValue);
   }
}
