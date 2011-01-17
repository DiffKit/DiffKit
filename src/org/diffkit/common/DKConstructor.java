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
package org.diffkit.common;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import org.apache.commons.lang.ClassUtils;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.Paranamer;

/**
 * wrapper class that adds parameter names for Constructor
 * 
 * @author jpanico
 */
public class DKConstructor<T> {

   private static final Paranamer _paranamer = new BytecodeReadingParanamer();

   public final Constructor<T> _constructor;
   public final String[] _parameterNames;

   public DKConstructor(Constructor<T> constructor_) {
      _constructor = constructor_;
      DKValidate.notNull(_constructor);
      _parameterNames = this.findParameterNames(constructor_);
   }

   private String[] findParameterNames(Constructor<T> constructor_) {
      return _paranamer.lookupParameterNames(constructor_);
   }

   public String toString() {
      return String.format("%s[%s:%s]", ClassUtils.getShortClassName(this.getClass()),
         ClassUtils.getShortClassName(_constructor.getDeclaringClass()),
         Arrays.toString(_parameterNames));
   }
}
