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

/**
 * @author jpanico
 */
public class DKNumberUtil {

   private DKNumberUtil() {
   }

   /**
    * null-safe, solves some problems related to autoboxing
    */
   public static int getInt(Number target_, int defalt_) {
      if (target_ == null)
         return defalt_;
      return target_.intValue();
   }
}
