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
package org.diffkit.diff.diffor.tst



import org.apache.commons.lang.time.DateUtils;

import org.diffkit.common.DKCalendarSpan;
import org.diffkit.diff.diffor.DKDateDiffor 

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestDateDiffor extends GroovyTestCase {
	
	public void testDiff(){
		
		def now = new Date()
		assert ! new DKDateDiffor().isDiff( null, null, null)
		assert  new DKDateDiffor().isDiff( now, null, null)
		assert  new DKDateDiffor().isDiff( null, now, null)
		assert ! new DKDateDiffor().isDiff( now, now, null)
      def threeDaysFromNow = DateUtils.addDays( now , 3)
      // in case of daylight savings time
      threeDaysFromNow = DateUtils.addHours( threeDaysFromNow , -1)
		assert new DKDateDiffor().isDiff( now, threeDaysFromNow, null)
		assert new DKDateDiffor().isDiff( threeDaysFromNow, now, null)
		
		def tolerance = new DKCalendarSpan(3, DKCalendarSpan.Unit.DAY);
		
		assert ! new DKDateDiffor(tolerance).isDiff( null, null, null)
		assert  new DKDateDiffor(tolerance).isDiff( now, null, null)
		assert  new DKDateDiffor(tolerance).isDiff( null, now, null)
		assert ! new DKDateDiffor(tolerance).isDiff( now, now, null)
		assert ! new DKDateDiffor(tolerance).isDiff( now, threeDaysFromNow, null)
		assert ! new DKDateDiffor(tolerance).isDiff( threeDaysFromNow, now, null)
		
		tolerance = new DKCalendarSpan(2, DKCalendarSpan.Unit.DAY);
		assert ! new DKDateDiffor(tolerance).isDiff( now, now, null)
		assert  new DKDateDiffor(tolerance).isDiff( now, threeDaysFromNow, null)
		assert  new DKDateDiffor(tolerance).isDiff( threeDaysFromNow, now, null)
		
		tolerance = new DKCalendarSpan(4, DKCalendarSpan.Unit.DAY);
		assert ! new DKDateDiffor(tolerance).isDiff( now, now, null)
		assert ! new DKDateDiffor(tolerance).isDiff( now, threeDaysFromNow, null)
		assert ! new DKDateDiffor(tolerance).isDiff( threeDaysFromNow, now, null)
		
	}
	
}
