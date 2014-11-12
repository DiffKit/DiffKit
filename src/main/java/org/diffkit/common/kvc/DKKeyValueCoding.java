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
public interface DKKeyValueCoding {

   public Object getValue(String key_);

   public void setValue(String key_, Object value_);

   public Object getValueAtPath(String keyPath_);

   public void setValueAtPath(String keyPath_, Object value_);

   public Map<String, ?> getValues(List<String> keys_);

   public void setValues(Map<String, ?> keyValues_);

}
