/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.util;

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
}
