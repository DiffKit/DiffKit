/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.util;

import java.util.Map;
import java.util.Set;

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
}
