/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.common.kvc;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jpanico
 */
public class DKMapKeyValueCoder implements DKKVCImp {

   private static final DKMapKeyValueCoder _instance = new DKMapKeyValueCoder();

   @SuppressWarnings("unused")
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   private DKMapKeyValueCoder() {
   }

   public static DKMapKeyValueCoder getInstance() {
      return _instance;
   }

   public Class<?> getHandledType() {
      return Map.class;
   }

   @SuppressWarnings("unchecked")
   public Object getValue(String key_, Object target_) {
      if ((key_ == null) || (target_ == null))
         return null;
      return ((Map<String, Object>) target_).get(key_);
   }

   public Object getValueAtPath(String keyPath_, Object target_) {
      if (DKKeyValueCoder.keyPathElementCount(keyPath_) == 1)
         return this.getValue(keyPath_, target_);
      Object firstValue = this.getValue(DKKeyValueCoder.firstKeyPathElement(keyPath_),
         target_);
      if (firstValue == null)
         return null;
      return DKKeyValueCoder.getInstance().getValueAtPath(
         DKKeyValueCoder.pathByRemovingFirstKeyPathElement(keyPath_), firstValue);
   }

   public Map<String, ?> getValues(List<String> keys_, Object target_) {
      throw new NotImplementedException();
   }

   @SuppressWarnings("unchecked")
   public void setValue(String key_, Object value_, Object target_) {
      if ((key_ == null) || (target_ == null))
         return;
      ((Map<String, Object>) target_).put(key_, value_);
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

   public void setValues(Map<String, ?> keyValues_, Object target_) {
      throw new NotImplementedException();
   }

}
