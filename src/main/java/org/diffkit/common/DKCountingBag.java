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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.lang.mutable.MutableInt;

/**
 * permits null entries
 * 
 * @author jpanico
 */
public class DKCountingBag {
   @SuppressWarnings({ "unchecked", "rawtypes" })
   private static final Comparator STORAGE_ENTRY_COMPARATOR = new ComparatorChain(
      Arrays.asList(Collections.reverseOrder(new MapEntryValueComparator()),
         new MapEntryKeyComparator()));
   private final Map<Object, MutableInt> _storage = new HashMap<Object, MutableInt>();

   public DKCountingBag() {
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

   /**
    * @return sum across the entries
    */
   public int totalCount() {
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
    * @return number of unique entries in the collection
    */
   public int size() {
      return _storage.size();
   }

   /**
    * @return Iterator over the unique members, in count order
    */
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public Iterator<Object> iterator() {
      Set entrySet = _storage.entrySet();
      if (entrySet == null)
         return new MapEntryKeyIterator(null);
      List entryList = new ArrayList(entrySet);
      Collections.sort(entryList, STORAGE_ENTRY_COMPARATOR);
      return new MapEntryKeyIterator(entryList.iterator());
   }

   @SuppressWarnings({ "rawtypes" })
   private static class MapEntryKeyIterator implements Iterator<Object> {

      private final Iterator<Map.Entry> _source;

      private MapEntryKeyIterator(Iterator<Map.Entry> source_) {
         _source = source_;
      }

      /**
       * @see java.util.Iterator#hasNext()
       */
      public boolean hasNext() {
         if (_source == null)
            return false;
         return _source.hasNext();
      }

      /**
       * @see java.util.Iterator#next()
       */
      public Object next() {
         if (_source == null)
            throw new NoSuchElementException();
         return _source.next().getKey();
      }

      /**
       * @see java.util.Iterator#remove()
       */
      public void remove() {
         if (_source == null)
            throw new IllegalStateException();
         _source.remove();
      }

   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   private static class MapEntryValueComparator
      implements Comparator<Map.Entry<?, Comparable>> {

      public int compare(Map.Entry<?, Comparable> lhs_, Map.Entry<?, Comparable> rhs_) {
         return lhs_.getValue().compareTo(rhs_.getValue());
      }
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   private static class MapEntryKeyComparator
      implements Comparator<Map.Entry<Comparable, ?>> {

      public int compare(Map.Entry<Comparable, ?> lhs_, Map.Entry<Comparable, ?> rhs_) {
         return lhs_.getKey().compareTo(rhs_.getKey());
      }
   }

}
