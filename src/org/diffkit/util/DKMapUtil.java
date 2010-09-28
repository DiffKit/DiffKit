/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.MapUtils;

/**
 * @author jpanico
 */
public class DKMapUtil {

   /**
    * @return the first match it encounters
    */
   public static <T> T getValueForKeyPrefix(Map<String, T> target_, String keyPrefix_) {
      if ((target_ == null) || target_.isEmpty() || (keyPrefix_ == null))
         return null;

      Set<String> keySet = target_.keySet();
      for (String key : keySet) {
         if (key.startsWith(keyPrefix_))
            return target_.get(key);
      }
      return null;
   }

   public static Object[] getKeyArray(Map<?, ?> target_) {
      if (MapUtils.isEmpty(target_))
         return null;
      Set<?> keySet = target_.keySet();
      if (keySet == null)
         return null;
      return IteratorUtils.toArray(keySet.iterator());
   }

   public static Object[] getValueArray(Map<?, ?> target_) {
      if (MapUtils.isEmpty(target_))
         return null;
      Collection<?> values = target_.values();
      if (values == null)
         return null;
      return IteratorUtils.toArray(values.iterator());
   }
}
