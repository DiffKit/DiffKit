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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.commons.lang.mutable.MutableInt;

/**
 * permits null entries
 * 
 * @author jpanico
 */
public class DKOrderedBag {
   private final LinkedHashMap<Object, MutableInt> _storage = new LinkedHashMap<Object, MutableInt>();

   public DKOrderedBag() {
   }

   public boolean add(Object object_) {
      return this.add(object_, 1);
   }

   public boolean add(Object object_, int copies_) {
      MutableInt value = _storage.get(object_);
      boolean isNew = false;
      if (value == null) {
         isNew = true;
         value = new MutableInt(0);
         _storage.put(object_, value);
      }
      value.add(copies_);
      return isNew;
   }

   public int getCount(Object object_) {
      MutableInt value = _storage.get(object_);
      if (value == null)
         return 0;
      return value.intValue();
   }

   public int size() {
      Iterator<Object> iterator = this.iterator();
      if (iterator == null)
         return 0;
      int size = 0;
      while (iterator.hasNext()) {
         Object entry = iterator.next();
         size += this.getCount(entry);
      }
      return size;
   }

   /**
    * @return Iterator over the unique members, in addition order
    */
   public Iterator<Object> iterator() {
      Set<Object> keySet = _storage.keySet();
      if (keySet == null)
         return null;
      return keySet.iterator();
   }
}
