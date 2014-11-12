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

import org.apache.commons.lang.time.DateUtils;

/**
 * @author jpanico
 */
public class DKCalendarSpan {

   /**
    * N.B. these values are just estimates
    */
   public static final long MILLIS_PER_WEEK = DateUtils.MILLIS_PER_DAY * 7;
   public static final long MILLIS_PER_MONTH = DateUtils.MILLIS_PER_DAY * 31;
   public static final long MILLIS_PER_YEAR = DateUtils.MILLIS_PER_DAY * 365;

   public enum Unit {
      MILLISECOND(1), SECOND(DateUtils.MILLIS_PER_SECOND), MINUTE(
         DateUtils.MILLIS_PER_MINUTE), HOUR(DateUtils.MILLIS_PER_HOUR), DAY(
         DateUtils.MILLIS_PER_DAY), WEEK(MILLIS_PER_WEEK), MONTH(MILLIS_PER_MONTH), YEAR(
         MILLIS_PER_YEAR);

      /**
       * in millis
       */
      private long _duration;

      private Unit(long duration_) {
         _duration = duration_;
      }
   }

   public final long _quantity;
   public final Unit _unit;
   public final long _spanMillis;

   public DKCalendarSpan(long quantity_, Unit unit_) {
      _quantity = quantity_;
      _unit = unit_;
      DKValidate.notNull(_unit);
      _spanMillis = _quantity * _unit._duration;
   }

}
