/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.util;

import java.util.List;

/**
 * @author jpanico
 */
public class DKListUtil {

   public static int[] toPrimitiveArray(List<Integer> target_) {
      if (target_ == null)
         return null;
      int[] array = new int[target_.size()];
      for (int i = 0; i < array.length; i++)
         array[i] = target_.get(i).intValue();
      return array;
   }

}
