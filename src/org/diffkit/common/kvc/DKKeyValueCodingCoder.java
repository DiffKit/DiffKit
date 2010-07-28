/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.common.kvc;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jpanico
 */
public class DKKeyValueCodingCoder implements DKKVCImp {

   public Class<?> getHandledType() {
      return DKKeyValueCoding.class;
   }

   private static final DKKeyValueCodingCoder _instance = new DKKeyValueCodingCoder();

   @SuppressWarnings("unused")
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   private DKKeyValueCodingCoder() {
   }

   public static DKKeyValueCodingCoder getInstance() {
      return _instance;
   }

   /**
    * @see com.legacy.foundation.common.DKKVCImp#getValue(java.lang.String,
    *      java.lang.Object)
    */
   public Object getValue(String key_, Object target_) {
      if ((key_ == null) || (target_ == null))
         return null;
      return ((DKKeyValueCoding) target_).getValue(key_);
   }

   /**
    * @see com.legacy.foundation.common.DKKVCImp#getValueAtPath(java.lang.String,
    *      java.lang.Object)
    */
   public Object getValueAtPath(String keyPath_, Object target_) {
      if ((keyPath_ == null) || (target_ == null))
         return null;
      return ((DKKeyValueCoding) target_).getValueAtPath(keyPath_);
   }

   /**
    * @see com.legacy.foundation.common.DKKVCImp#getValues(java.util.List,
    *      java.lang.Object)
    */
   public Map<String, ?> getValues(List<String> keys_, Object target_) {
      if ((keys_ == null) || (target_ == null))
         return null;
      return ((DKKeyValueCoding) target_).getValues(keys_);
   }

   /**
    * @see com.legacy.foundation.common.DKKVCImp#setValue(java.lang.String,
    *      java.lang.Object, java.lang.Object)
    */
   public void setValue(String key_, Object value_, Object target_) {
      if ((key_ == null) || (target_ == null))
         return;
      ((DKKeyValueCoding) target_).setValue(key_, value_);
   }

   /**
    * @see com.legacy.foundation.common.DKKVCImp#setValueAtPath(java.lang.String,
    *      java.lang.Object, java.lang.Object)
    */
   public void setValueAtPath(String keyPath_, Object value_, Object target_) {
      if ((keyPath_ == null) || (target_ == null))
         return;
      ((DKKeyValueCoding) target_).setValueAtPath(keyPath_, value_);
   }

   /**
    * @see com.legacy.foundation.common.DKKVCImp#setValues(java.util.Map,
    *      java.lang.Object)
    */
   public void setValues(Map<String, ?> keyValues_, Object target_) {
      if (target_ == null)
         return;
      ((DKKeyValueCoding) target_).setValues(keyValues_);
   }

}
