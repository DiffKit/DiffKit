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



import org.diffkit.diff.diffor.DKNumberDiffor 

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestNumberDiffor extends GroovyTestCase {
	
	public void testDiff(){
		
		assert ! new DKNumberDiffor().isDiff( null, null, null)
		assert  new DKNumberDiffor().isDiff( 0, null, null)
		assert  new DKNumberDiffor().isDiff( null, 0, null)
		assert ! new DKNumberDiffor().isDiff( 0, 0, null)
		
		assert ! new DKNumberDiffor(0, true).isDiff( null, null, null)
		assert ! new DKNumberDiffor(0, true).isDiff( 0, null, null)
		assert ! new DKNumberDiffor(0, true).isDiff( null, 0, null)
		assert ! new DKNumberDiffor(0, true).isDiff( 0, 0, null)
		assert  new DKNumberDiffor(0, true).isDiff( 0.1, 0, null)
		assert  ! new DKNumberDiffor(0.1, true).isDiff( 0, 0, null)
		assert  ! new DKNumberDiffor(0.1, true).isDiff( 0.1, 0, null)
		assert  ! new DKNumberDiffor(0.1, true).isDiff( 0, 0.1, null)
		assert  ! new DKNumberDiffor(0.1, true).isDiff( 0.1, 0.1, null)
		
		assert  ! new DKNumberDiffor(0.1, true).isDiff( 0.1, null, null)
		assert   new DKNumberDiffor(0.1, true).isDiff( 0.1, -0.1, null)
		assert  ! new DKNumberDiffor(0.1, true).isDiff( null, -0.1, null)
		assert   new DKNumberDiffor(0.1, true).isDiff( null, -0.2, null)
	}
	
}
