/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.util;

import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;

/**
 * @author jpanico
 */
public class DKMapUtil {

   private DKMapUtil() {
   }

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

   @SuppressWarnings({ "unchecked", "rawtypes" })
   public static Map combine(Map target_, Map source_) {

      if (MapUtils.isEmpty(source_))
         return target_;
      if (MapUtils.isEmpty(target_))
         return source_;
      target_.putAll(source_);
      return target_;
   }
}
