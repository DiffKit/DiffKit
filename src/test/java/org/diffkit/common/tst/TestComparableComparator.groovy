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

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestComparableComparator extends GroovyTestCase {
	
	public void testCompare(){
		
		assert DKComparableComparator.instance.compare(3,3) == 0
		assert DKComparableComparator.instance.compare(3,null) > 0
		assert DKComparableComparator.instance.compare(null,3) < 0
		assert DKComparableComparator.instance.compare(null,null) == 0
		
		assert DKComparableComparator.instance.compare('aaaa','aaaa') == 0
		assert DKComparableComparator.instance.compare('aaaa','bbbb') <0
		assert DKComparableComparator.instance.compare('bbbb','aaaa') >0
		
	}
	
}
