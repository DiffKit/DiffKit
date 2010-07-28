/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.common.kvc;

import java.util.List;
import java.util.Map;

/**
 * @author jpanico
 */
public interface DKKVCImp {

   public Object getValue(String key_, Object target_);

   public void setValue(String key_, Object value_, Object target_);

   public Object getValueAtPath(String keyPath_, Object target_);

   public void setValueAtPath(String keyPath_, Object value_, Object target_);

   public Map<String, ?> getValues(List<String> keys_, Object target_);

   public void setValues(Map<String, ?> keyValues_, Object target_);

   public Class<?> getHandledType();

}
