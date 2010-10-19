/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.util;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;

/**
 * @author jpanico
 */
public class DKArrayUtil {

   @SuppressWarnings("unchecked")
   public static <T> T[] createArray(Class<T> class_, int capacity_) {
      return (T[]) Array.newInstance(class_, capacity_);
   }

   @SuppressWarnings("unchecked")
   public static <T> T[] copyEmpty(T[] target_) {
      if (target_ == null)
         return null;
      return (T[]) createArray(target_.getClass().getComponentType(), target_.length);
   }

   @SuppressWarnings("unchecked")
   public static <T> T[] subarray(T[] target_, int startIndexInclusive_,
                                  int endIndexExclusive_) {
      if (target_ == null)
         return null;
      int size = endIndexExclusive_ - startIndexInclusive_;
      T[] result = (T[]) createArray(target_.getClass().getComponentType(), size);
      for (int i = 0; i < size; i++)
         result[i] = target_[i + startIndexInclusive_];
      return result;
   }

   /**
    * will be ordered according to source_
    */
   public static <T> T[] getIntersection(T[] source_, T[] target_) {
      boolean sourceIsNull = (source_ == null);
      boolean targetIsNull = (target_ == null);
      if (sourceIsNull || targetIsNull)
         return null;
      T[] intersection = copyEmpty(source_);
      int j = 0;
      for (int i = 0; i < source_.length; i++) {
         if (ArrayUtils.contains(target_, source_[i]))
            intersection[j++] = source_[i];
      }
      return subarray(intersection, 0, j);
   }

   public static <T> T[] removeElementsAtIndices(T[] target_, int[] removeIndices_) {
      if (ArrayUtils.isEmpty(target_))
         return target_;
      if (ArrayUtils.isEmpty(removeIndices_))
         return target_;
      T[] results = Arrays.copyOf(target_, target_.length);
      Arrays.fill(results, null);
      int skipped = 0;
      for (int i = 0; i < target_.length; i++) {
         if (ArrayUtils.contains(removeIndices_, i)) {
            skipped++;
            continue;
         }
         results[i - skipped] = target_[i];
      }
      return subarray(results, 0, target_.length - skipped);
   }

   /**
    * shrinks the array, from the most significant side, until first non-null
    * value is encountered
    */
   public static <T> T[] compact(T[] target_) {
      if (target_ == null)
         return null;
      int i = target_.length;
      for (; i > 0; i--) {
         if (target_[i - 1] != null)
            break;
      }
      if (i == 0)
         return null;
      return subarray(target_, 0, i);
   }

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
