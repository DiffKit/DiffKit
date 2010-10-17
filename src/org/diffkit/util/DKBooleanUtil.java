/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.util;

import org.apache.commons.lang.BooleanUtils;

/**
 * @author jpanico
 */
public class DKBooleanUtil {

   private DKBooleanUtil() {
   }

   public static boolean toBoolean(Object target_) {
      if (target_ == null)
         return false;
      if (target_ instanceof Boolean)
         return ((Boolean) target_).booleanValue();
      else if (target_ instanceof Integer)
         return BooleanUtils.toBoolean(((Integer) target_).intValue());
      else if (target_ instanceof String)
         return BooleanUtils.toBoolean((String) target_);
      else
         throw new IllegalArgumentException(String.format("unrecognized type->",
            target_.getClass()));
   }
}
