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
package org.diffkit.diff.engine;

/**
 * @author jpanico
 */
public enum DKSide {
   LEFT, RIGHT, BOTH;

   public static final int LEFT_INDEX = LEFT.ordinal();
   public static final int RIGHT_INDEX = RIGHT.ordinal();
   public static final int BOTH_INDEX = BOTH.ordinal();

   public static int getConstantForEnum(DKSide enum_) {
      return enum_.ordinal();
   }

   public static DKSide getEnumForConstant(int constant_) {
      return DKSide.class.getEnumConstants()[constant_];
   }

   public static int flip(int target_) {
      if (target_ == LEFT_INDEX)
         return RIGHT_INDEX;
      if (target_ == RIGHT_INDEX)
         return LEFT_INDEX;
      return target_;
   }
}
