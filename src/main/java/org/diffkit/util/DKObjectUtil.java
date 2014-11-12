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
package org.diffkit.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.beanutils.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;

/**
 * @author jpanico
 */
public class DKObjectUtil {
   private static final Logger LOG = LoggerFactory.getLogger(DKObjectUtil.class);

   private DKObjectUtil() {
   }

   /**
    * null safe
    */
   public static String toString(Object target_) {
      if (target_ == null)
         return "null";
      return target_.toString();
   }

   /**
    * Hex String representation of identity hashCode()
    */
   public static String getAddressHexString(Object target_) {
      if (target_ == null)
         return null;
      return "@" + Integer.toHexString(System.identityHashCode(target_));
   }

   @SuppressWarnings("rawtypes")
   public static boolean respondsTo(Object target_, String methodName_, Class[] parmTypes_) {
      if ((target_ == null) || (methodName_ == null))
         return false;
      if (parmTypes_ == null)
         parmTypes_ = new Class[0];
      Method method = MethodUtils.getMatchingAccessibleMethod(target_.getClass(),
         methodName_, parmTypes_);
      return (method != null);
   }

   @SuppressWarnings("rawtypes")
   public static Object invoke(Object target_, String methodName_, Class[] args_)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      DKValidate.notNull(target_, methodName_);

      return MethodUtils.invokeMethod(target_, methodName_, args_);
   }

   /**
    * convenience method that calls invoke(Object,String,Class[])
    */
   @SuppressWarnings("rawtypes")
   public static Object invokeSafe(Object target_, String methodName_, Class[] args_) {
      try {
         return MethodUtils.invokeMethod(target_, methodName_, args_);
      }
      catch (Exception e_) {
         LOG.warn(null, e_);
         return null;
      }
   }
}
