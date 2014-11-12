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


import org.diffkit.common.DKMapKeyValueComparator 

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestMapKeyValueComparator extends GroovyTestCase {
	
	public void testCompare(){
		
		def map0 = ['key1':'aaaa', 'key2':3]
		def map1 = ['key1':'zzzz', 'key2':2]
		def map2 = ['key1':'mmmm', 'key2':1]
		def maps = [map0, map1, map2]
		
		Collections.sort( maps, new DKMapKeyValueComparator('key1'))
		assert maps[0]['key1'] == 'aaaa'
		assert maps[1]['key1'] == 'mmmm'
		assert maps[2]['key1'] == 'zzzz'
		
		Collections.sort( maps, new DKMapKeyValueComparator('key2'))
		assert maps[0]['key1'] == 'mmmm'
		assert maps[1]['key1'] == 'zzzz'
		assert maps[2]['key1'] == 'aaaa'
		
	}
	
}
