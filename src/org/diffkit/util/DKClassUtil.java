/**
 * Copyright 2010 Joseph Panico
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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKConstructorNotFoundException;

/**
 * @author jpanico
 */
public class DKClassUtil {

   private static final Logger LOG = LoggerFactory.getLogger(DKClassUtil.class);

   private DKClassUtil() {
   }

   /**
    * Method can have any visibility; recursively climbs the Class hierarchy; no
    * arg methods only; instance methods only
    */
   public static Method findMethod(String methodName_, int argCount_, Class<?> target_) {
      if ((methodName_ == null) || (target_ == null))
         return null;

      try {
         Method[] declaredMethods = target_.getDeclaredMethods();
         if ((declaredMethods == null) || (declaredMethods.length == 0))
            return null;
         for (Method method : declaredMethods) {
            int modifiers = method.getModifiers();
            if (Modifier.isAbstract(modifiers))
               continue;
            if (!methodName_.equals(method.getName()))
               continue;
            if (!(getParameterCount(method) == argCount_))
               continue;
            return method;
         }
      }
      catch (Exception e_) {
         LOG.error(null, e_);
         return null;
      }
      return findMethod(methodName_, argCount_, target_.getSuperclass());
   }

   /**
    * Field can have any visibility; recursively climbs the Class hierarchy;
    * instance variables only
    * 
    * @param fieldNames_
    *           list of aliases for a single field, searched in order
    * @return the first Field that matches one of fieldNames_
    */
   public static Field findField(String[] fieldNames_, Class<?> target_) {
      if ((fieldNames_ == null) || (fieldNames_.length == 0) || (target_ == null))
         return null;

      try {
         Field[] declaredFields = target_.getDeclaredFields();
         if ((declaredFields == null) || (declaredFields.length == 0))
            return null;
         for (String fieldName : fieldNames_) {
            for (Field field : declaredFields) {
               int modifiers = field.getModifiers();
               if (Modifier.isStatic(modifiers))
                  continue;
               if (!fieldName.equals(field.getName()))
                  continue;
               return field;
            }
         }
      }
      catch (Exception e_) {
         LOG.error(null, e_);
         return null;
      }
      return findField(fieldNames_, target_.getSuperclass());
   }

   /**
    * null and Exception safe
    */
   public static Object getValue(AccessibleObject access_, Object target_) {
      if ((access_ == null) || (target_ == null))
         return null;
      boolean isField = access_ instanceof Field;
      boolean isMethod = access_ instanceof Method;
      if (!isField && !isMethod)
         throw new IllegalArgumentException(String.format(
            "unhandled accessible object->%s", access_));
      if (isMethod)
         return getValue((Method) access_, target_);
      return getValue((Field) access_, target_);
   }

   /**
    * null and Exception safe
    */
   public static void setValue(AccessibleObject access_, Object value_, Object target_) {
      if ((access_ == null) || (target_ == null))
         return;
      boolean isField = access_ instanceof Field;
      boolean isMethod = access_ instanceof Method;
      if (!isField && !isMethod)
         throw new IllegalArgumentException(String.format(
            "unhandled accessible object->%s", access_));
      if (isMethod)
         setValue((Method) access_, value_, target_);
      else
         setValue((Field) access_, value_, target_);
   }

   /**
    * null and Exception safe; drills through accessibility issues
    */
   public static Object getValue(Field field_, Object target_) {
      if ((field_ == null) || (target_ == null))
         return null;

      try {
         if (!field_.isAccessible())
            field_.setAccessible(true);

         return field_.get(target_);
      }
      catch (Exception e_) {
         LOG.error(null, e_);
         return null;
      }
   }

   /**
    * null and Exception safe; drills through accessibility issues
    */
   public static void setValue(Field field_, Object value_, Object target_) {
      if ((field_ == null) || (target_ == null))
         return;

      try {
         if (!field_.isAccessible())
            field_.setAccessible(true);

         field_.set(target_, value_);
      }
      catch (Exception e_) {
         LOG.error(null, e_);
      }
   }

   /**
    * null and Exception safe; drills through accessibility issues
    */
   public static Object getValue(Method getter_, Object target_) {
      if ((getter_ == null) || (target_ == null))
         return null;

      try {
         if (!getter_.isAccessible())
            getter_.setAccessible(true);

         return getter_.invoke(target_, (Object[]) null);
      }
      catch (Exception e_) {
         LOG.error(null, e_);
         return null;
      }
   }

   /**
    * null and Exception safe; drills through accessibility issues
    */
   public static void setValue(Method setter_, Object value_, Object target_) {
      if ((setter_ == null) || (target_ == null))
         return;

      try {
         if (!setter_.isAccessible())
            setter_.setAccessible(true);

         setter_.invoke(target_, value_);
      }
      catch (Exception e_) {
         LOG.error(null, e_);
      }
   }

   public static Class<?>[] getClasses(Object... params_) {
      if (params_ == null)
         return null;
      Class<?>[] classes = new Class<?>[params_.length];
      for (int i = 0; i < params_.length; i++)
         classes[i] = (params_[i] == null ? Object.class : params_[i].getClass());
      return classes;
   }

   /**
    * uses {@link #getConstructor(Class, Class[])} to find appropriate
    * Constructor, and then calls that Constructor with supplied params_
    */
   public static <T> T createInstance(Class<T> target_, Object... params_)
      throws NoSuchMethodException {
      if (target_ == null)
         return null;
      Class<?>[] paramTypes = getClasses(params_);
      Constructor<T> constructor = target_.getConstructor(paramTypes);
      try {
         return constructor.newInstance(params_);
      }
      catch (Exception e_) {
         LOG.error(null, e_);
         return null;
      }
   }

   /**
    * throws a RuntimeException if more than on Constructor qualifies as longest
    */
   @SuppressWarnings("unchecked")
   public static <T> Constructor<T> findLongestConstructor(Class<T> target_) {
      if (target_ == null)
         return null;
      Constructor<T>[] constructors = (Constructor<T>[]) target_.getConstructors();
      if ((constructors == null) || (constructors.length == 0))
         return null;
      Comparator<Constructor<T>> parmComparator = Collections.reverseOrder(new ConstructorParmComparator<T>());
      Arrays.sort(constructors, parmComparator);
      if ((constructors.length > 1)
         && (DKConstructorUtil.getParameterCount(constructors[0]) == DKConstructorUtil.getParameterCount(constructors[1])))
         throw new DKConstructorNotFoundException(String.format(
            "more than one longest Constructor: %s & %s", constructors[0],
            constructors[1]));
      return constructors[0];
   }

   /**
    * compares Constructors based on parameter count
    */
   private static class ConstructorParmComparator<T>
      implements Comparator<Constructor<T>> {

      public int compare(Constructor<T> lhs_, Constructor<T> rhs_) {
         boolean lhsIsNull = (lhs_ == null);
         boolean rhsIsNull = (rhs_ == null);
         if (lhsIsNull && rhsIsNull)
            return 0;
         if (lhsIsNull)
            return -1;
         if (rhsIsNull)
            return 1;
         int lhsCount = DKConstructorUtil.getParameterCount(lhs_);
         int rhsCount = DKConstructorUtil.getParameterCount(rhs_);
         if (lhsCount < rhsCount)
            return -1;
         if (lhsCount > rhsCount)
            return 1;
         return 0;
      }
   }

   public static int getParameterCount(Method target_) {
      if (target_ == null)
         return -1;
      Class<?>[] paramTypes = target_.getParameterTypes();
      if (paramTypes == null)
         return 0;
      return paramTypes.length;
   }

}
