/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.util;

import java.sql.Time;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang.time.DateUtils;

/**
 * @author jpanico
 */
public class DKTimeUtil {

   private DKTimeUtil() {
   }

   public static Date createDate(int year_, int month_, int dayOfMonth_) {
      return new GregorianCalendar(year_, month_, dayOfMonth_).getTime();
   }

   public static Date createDate(int year_, int month_, int dayOfMonth_, int hourOfDay_,
                                 int minute_, int second_, int millisecond_) {
      Date date = new GregorianCalendar(year_, month_, dayOfMonth_, hourOfDay_, minute_,
         second_).getTime();
      return DateUtils.addMilliseconds(date, millisecond_);
   }

   public static Time createTime(int hourOfDay_, int minute_, int second_,
                                 int millisecond_) {
      long millis = DateUtils.MILLIS_PER_HOUR * hourOfDay_ + DateUtils.MILLIS_PER_MINUTE
         * minute_ + DateUtils.MILLIS_PER_SECOND * second_ + millisecond_;
      return new Time(millis);
   }

}
