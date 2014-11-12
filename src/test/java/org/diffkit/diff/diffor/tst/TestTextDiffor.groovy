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



import org.apache.commons.lang.StringUtils;
import org.diffkit.diff.diffor.DKTextDiffor 

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestTextDiffor extends GroovyTestCase {
   
   public void testStringUtils(){
      assert StringUtils.replaceChars('abcdef', 'bd', '') == 'acef'
   }
   
   public void testNormalize() {
	   def diffor = new DKTextDiffor(null)
	   assert diffor.normalize('in this world of affordable') == 'in this world of affordable'
	   assert diffor.normalize('in   this    world    of  affordable') == 'in this world of affordable'
	   assert diffor.normalize('in  \n this\n    world\n\t\r  \r\t\n  of  affordable') == 'in this world of affordable'
   }
   
   public void testDiff(){
      def diffor = new DKTextDiffor("")
      assert ! diffor.isDiff( 'hello', 'hello', null)
      assert diffor.isDiff( 'hello', 'world', null)
      
      diffor = new DKTextDiffor("")
      assert ! diffor.isDiff( 'hello', 'hello', null)
      assert diffor.isDiff( 'hello', 'world', null)
      assert !diffor.isDiff( 'hello\n\r', 'hello', null)
      assert !diffor.isDiff( 'hel\nlo\r', 'hel lo', null)
      
   }
}
