/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.util;

import java.lang.reflect.Constructor;

/**
 * @author jpanico
 */
public class DKConstructorUtil {

   private DKConstructorUtil() {
   }

   public static int getParameterCount(Constructor<?> constructor_) {
      if (constructor_ == null)
         return -1;
      Class<?>[] parameterTypes = constructor_.getParameterTypes();
      if (parameterTypes == null)
         return 0;
      return parameterTypes.length;
   }
}
