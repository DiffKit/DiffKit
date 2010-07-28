/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.common.kvc;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.util.DKStringUtil;

/**
 * @author jpanico
 */
public class DKKeyValueCoder implements DKKVCImp {

   public static final char KEY_PATH_SEPARATOR = '.';
   public static final String KEY_PATH_SEPARATOR_STRING = String.valueOf('.');

   private static final DKKeyValueCoder _instance = new DKKeyValueCoder();

   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   private DKKeyValueCoder() {
   }

   public static DKKeyValueCoder getInstance() {
      return _instance;
   }

   /**
    * convenience method
    */
   public static boolean isPath(String key_) {
      if (key_ == null)
         return false;
      if (!StringUtils.contains(key_, KEY_PATH_SEPARATOR))
         return false;
      return true;
   }

   public Class<?> getHandledType() {
      return Object.class;
   }

   public Object getValue(String key_, Object target_) {
      DKKVCImp imp = this.getImpForTarget(target_);
      if (imp == null) {
         _log.error(String.format("couldn't find imp for target->%s", target_));
         return null;
      }
      return imp.getValue(key_, target_);
   }

   public Object getValueAtPath(String keyPath_, Object target_) {
      DKKVCImp imp = this.getImpForTarget(target_);
      if (imp == null) {
         _log.error(String.format("couldn't find imp for target->%s", target_));
         return null;
      }
      return imp.getValueAtPath(keyPath_, target_);
   }

   public Map<String, ?> getValues(List<String> keys_, Object target_) {
      DKKVCImp imp = this.getImpForTarget(target_);
      if (imp == null) {
         _log.error(String.format("couldn't find imp for target->%s", target_));
         return null;
      }
      return imp.getValues(keys_, target_);
   }

   public void setValue(String key_, Object value_, Object target_) {
      DKKVCImp imp = this.getImpForTarget(target_);
      if (imp == null) {
         _log.error(String.format("couldn't find imp for target->%s", target_));
         return;
      }
      imp.setValue(key_, value_, target_);
   }

   public void setValueAtPath(String keyPath_, Object value_, Object target_) {
      DKKVCImp imp = this.getImpForTarget(target_);
      if (imp == null) {
         _log.error(String.format("couldn't find imp for target->%s", target_));
         return;
      }
      imp.setValueAtPath(keyPath_, value_, target_);
   }

   public void setValues(Map<String, ?> keyValues_, Object target_) {
      DKKVCImp imp = this.getImpForTarget(target_);
      if (imp == null) {
         _log.error(String.format("couldn't find imp for target->%s", target_));
         return;
      }
      imp.setValues(keyValues_, target_);
   }

   public DKKVCImp getImpForTarget(Object target_) {
      if (target_ == null)
         return null;
      if (target_ instanceof DKKeyValueCoding)
         return DKKeyValueCodingCoder.getInstance();
      if (target_ instanceof Map<?, ?>)
         return DKMapKeyValueCoder.getInstance();
      return DKPropertyKeyValueCoder.getInstance();
   }

   public static String firstKeyPathElement(String keyPath_) {
      String[] elements = keyPathElements(keyPath_);
      if ((elements == null) || (elements.length == 0))
         return null;
      return elements[0];
   }

   public static String lastKeyPathElement(String keyPath_) {
      String[] elements = keyPathElements(keyPath_);
      if ((elements == null) || (elements.length == 0))
         return null;
      return elements[elements.length - 1];
   }

   public static String pathByRemovingFirstKeyPathElement(String keyPath_) {
      if (keyPath_ == null)
         return null;
      String[] elements = keyPathElements(keyPath_);
      if ((elements == null || (elements.length < 2)))
         return null;
      if (elements.length == 2)
         return elements[1];

      return StringUtils.join(elements, DKKeyValueCoder.KEY_PATH_SEPARATOR, 1,
         elements.length);
   }

   public static String pathByRemovingLastKeyPathElement(String keyPath_) {
      if (keyPath_ == null)
         return null;
      String[] elements = keyPathElements(keyPath_);
      if ((elements == null || (elements.length < 2)))
         return null;
      if (elements.length == 2)
         return elements[0];

      return StringUtils.join(elements, DKKeyValueCoder.KEY_PATH_SEPARATOR, 0,
         elements.length - 1);
   }

   public static String[] keyPathElements(String keyPath_) {
      if (keyPath_ == null)
         return null;
      return StringUtils.split(keyPath_, DKKeyValueCoder.KEY_PATH_SEPARATOR);
   }

   public static int keyPathElementCount(String keyPath_) {
      if (keyPath_ == null)
         return 0;
      keyPath_ = StringUtils.strip(keyPath_, DKKeyValueCoder.KEY_PATH_SEPARATOR_STRING);
      int length = keyPath_.length();
      if (length == 0)
         return 0;

      return (DKStringUtil.countMatches(keyPath_, DKKeyValueCoder.KEY_PATH_SEPARATOR, 0,
         length) + 1);
   }
}
