/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.util;

import org.apache.commons.lang.ArrayUtils;

/**
 * @author jpanico
 */
public class DKArrayUtil {

   /**
    * shrinks the array, from the most significant side, until first non-fill_
    * value is encountered
    */
   public static int[] compactFill(int[] target_, int fill_) {
      if (target_ == null)
         return null;
      int i = target_.length;
      for (; i > 0; i--) {
         if (target_[i - 1] != fill_)
            break;
      }
      if (i == 0)
         return null;
      return ArrayUtils.subarray(target_, 0, i);
   }

   public static long sum(int[] ints_) {
      if (ints_ == null)
         return 0;

      long sum = 0;
      for (int value : ints_)
         sum += value;

      return sum;
   }
}
