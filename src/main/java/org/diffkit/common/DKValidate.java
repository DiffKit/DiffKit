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

import java.util.Collection;

/**
 * @author jpanico
 */
public class DKValidate {
   private static final String NOT_NULL_FAILURE_MESSAGE = "does not accept null args";
   private static final String NOT_EMPTY_FAILURE_MESSAGE = "does not accept empty args";

   public static void notNull(Object... args_) {
      if (args_ == null)
         throw new IllegalArgumentException(NOT_NULL_FAILURE_MESSAGE);

      for (Object arg : args_) {
         if (arg == null)
            throw new IllegalArgumentException(NOT_NULL_FAILURE_MESSAGE);
      }
   }

   public static void notEmpty(Object... args_) {
      if (args_ == null)
         throw new IllegalArgumentException(NOT_EMPTY_FAILURE_MESSAGE);

      for (Object arg : args_) {
         if (arg == null)
            throw new IllegalArgumentException(NOT_EMPTY_FAILURE_MESSAGE);
         if (arg instanceof Collection<?>) {
            if (((Collection<?>) arg).size() == 0)
               throw new IllegalArgumentException(NOT_EMPTY_FAILURE_MESSAGE);
         }
      }
   }
}
