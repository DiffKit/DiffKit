/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.util;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

/**
 * @author jpanico
 */
public class DKListUtil {

   private DKListUtil() {
   }

   public static int[] toPrimitiveArray(List<Integer> target_) {
      if (target_ == null)
         return null;
      int[] array = new int[target_.size()];
      for (int i = 0; i < array.length; i++)
         array[i] = target_.get(i).intValue();
      return array;
   }

   /**
    * simply returns null if index_ is out of bounds
    */
   public static <T> T safeGet(List<T> target_, int index_) {
      if (CollectionUtils.isEmpty(target_))
         return null;
      if (index_ >= target_.size())
         return null;
      return target_.get(index_);
   }
}
