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


import org.diffkit.diff.diffor.DKChainDiffor;
import org.diffkit.diff.diffor.DKEqualsDiffor;
import org.diffkit.diff.diffor.DKIdentityDiffor;

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestChainDiffor extends GroovyTestCase {
	
	public void testDiff(){
		
		def lhs = new String('hello')
		def rhs = new String('hello')
		def identityDiffor = DKIdentityDiffor.instance
		def equalsDiffor = DKEqualsDiffor.instance
		
		assert identityDiffor.isDiff( lhs, rhs, null)
		assert !equalsDiffor.isDiff( lhs, rhs, null)
		
		assert new  DKChainDiffor(identityDiffor).isDiff(lhs, rhs, null)
		assert ! new  DKChainDiffor(equalsDiffor).isDiff(lhs, rhs, null)
		
		assert new  DKChainDiffor(identityDiffor, identityDiffor).isDiff(lhs, rhs, null)
		assert !new  DKChainDiffor(equalsDiffor, equalsDiffor).isDiff(lhs, rhs, null)
		
		assert !new  DKChainDiffor(identityDiffor, equalsDiffor).isDiff(lhs, rhs, null)
	}
	
}
