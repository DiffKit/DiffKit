/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.common.kvc;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.annot.Stateless;
import org.diffkit.util.DKClassUtil;

/**
 * TODO in need of some serious performance tuning
 * 
 * @author jpanico
 */
@Stateless
public class DKPropertyKeyValueCoder implements DKKVCImp {

   private static final DKPropertyKeyValueCoder _instance = new DKPropertyKeyValueCoder();

   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   private DKPropertyKeyValueCoder() {
   }

   public static DKPropertyKeyValueCoder getInstance() {
      return _instance;
   }

   public Class<?> getHandledType() {
      return Object.class;
   }

   public Object getValue(String key_, Object target_) {
      if ((key_ == null) || (target_ == null))
         return null;
      AccessibleObject getAccess = this.getGetAccess(key_, target_);
      if (getAccess == null) {
         _log.debug("couldn't find getAccess for key_->{} target_->{}", key_, target_);
         return null;
      }
      return DKClassUtil.getValue(getAccess, target_);
   }

   public void setValue(String key_, Object value_, Object target_) {
      if ((key_ == null) || (target_ == null))
         return;
      AccessibleObject setAccess = this.getSetAccess(key_, target_);
      if (setAccess == null) {
         _log.debug("couldn't find setAccess for key_->{} target_->{}", key_, target_);
         return;
      }
      DKClassUtil.setValue(setAccess, value_, target_);
   }

   /**
    * null tolerant right now-- not sure that's the right answer
    */
   public Object getValueAtPath(String keyPath_, Object target_) {
      if ((keyPath_ == null) || (target_ == null))
         return null;

      if (DKKeyValueCoder.keyPathElementCount(keyPath_) == 1)
         return this.getValue(keyPath_, target_);
      Object firstValue = this.getValue(DKKeyValueCoder.firstKeyPathElement(keyPath_),
         target_);
      if (firstValue == null)
         return null;
      return DKKeyValueCoder.getInstance().getValueAtPath(
         DKKeyValueCoder.pathByRemovingFirstKeyPathElement(keyPath_), firstValue);
   }

   public void setValueAtPath(String keyPath_, Object value_, Object target_) {
      if ((keyPath_ == null) || (target_ == null))
         return;
      if (DKKeyValueCoder.keyPathElementCount(keyPath_) == 1) {
         this.setValue(keyPath_, value_, target_);
         return;
      }

      Object finalTarget = this.getValueAtPath(
         DKKeyValueCoder.pathByRemovingLastKeyPathElement(keyPath_), target_);
      if (finalTarget == null)
         return;

      DKKeyValueCoder.getInstance().setValue(
         DKKeyValueCoder.lastKeyPathElement(keyPath_), value_, finalTarget);
   }

   public Map<String, ?> getValues(List<String> keys_, Object target_) {
      throw new NotImplementedException();
   }

   public void setValues(Map<String, ?> keyValues_, Object target_) {
      throw new NotImplementedException();
   }

   /**
    * @return either a Method or a Field, in that order
    */
   private AccessibleObject getGetAccess(String key_, Object target_) {
      if ((key_ == null) || (target_ == null))
         return null;
      String getterName = getGetterName(key_);
      Class<?> targetClass = target_.getClass();
      if (getterName != null) {
         Method getter = DKClassUtil.findMethod(getterName, 0, targetClass);
         if (getter != null)
            return getter;
      }
      String[] iVarNames = getIvarNames(key_);
      return DKClassUtil.findField(iVarNames, targetClass);
   }

   /**
    * @return either a Method or a Field, in that order
    */
   private AccessibleObject getSetAccess(String key_, Object target_) {
      if ((key_ == null) || (target_ == null))
         return null;
      String setterName = getSetterName(key_);
      Class<?> targetClass = target_.getClass();
      if (setterName != null) {
         Method setter = DKClassUtil.findMethod(setterName, 1, targetClass);
         if (setter != null)
            return setter;
      }
      String[] iVarNames = getIvarNames(key_);
      return DKClassUtil.findField(iVarNames, targetClass);
   }

   private String getGetterName(String key_) {
      return "get" + StringUtils.capitalize(key_);
   }

   private String getSetterName(String key_) {
      return "set" + StringUtils.capitalize(key_);
   }

   private String[] getIvarNames(String key_) {
      return new String[] { key_, "_" + key_ };
   }

}
