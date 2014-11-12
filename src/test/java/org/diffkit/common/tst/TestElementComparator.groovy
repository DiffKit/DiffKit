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
package org.diffkit.common.tst

import org.diffkit.common.DKComparableComparator;
import org.diffkit.common.DKElementComparator;

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestElementComparatorentComparator extends GroovyTestCase {
	
	public void testCompare(){
		
		Object[] lhs = ['value1', 'value2', 3, 'value4', 2] 
		Object[] rhs = ['value0', 'value1', 3, 'value4', 3] 
		
		assert new DKElementComparator(0,0, DKComparableComparator.instance).compare(lhs, rhs) > 0
		assert new DKElementComparator(0,1, DKComparableComparator.instance).compare(lhs, rhs) == 0
		assert new DKElementComparator(2,2, DKComparableComparator.instance).compare(lhs, rhs) == 0
		assert new DKElementComparator(3,3, DKComparableComparator.instance).compare(lhs, rhs) == 0
		assert new DKElementComparator(4,4, DKComparableComparator.instance).compare(lhs, rhs) < 0
						
	}
	
}
