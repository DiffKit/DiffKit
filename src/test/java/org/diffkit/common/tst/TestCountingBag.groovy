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




import org.apache.commons.collections.bidimap.TreeBidiMap 
import org.diffkit.common.DKCountingBag 

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestCountingBag extends GroovyTestCase {
      
   
   public void testCount(){
      DKCountingBag target = []
      assert target.getCount(null) == 0
      def key1 = 'key1'
      def key2 = 'key2'
      def key3 = 'aaaa'
      assert target.getCount(key1)== 0
      
      target.add(key1)
      assert target.getCount(key1) == 1
      assert target.getCount(key2)== 0
      assert target.totalCount() == 1
      
      target.add(key1,2)
      assert target.getCount(key1) == 3
      assert target.getCount(key2)== 0
      assert target.totalCount() == 3
      
      target.add(key2,5)
      assert target.getCount(key1) == 3
      assert target.getCount(key2)== 5
      assert target.totalCount() == 8
      
      target.add(key3)
      assert target.getCount(key1) == 3
      assert target.getCount(key2)== 5
      assert target.getCount(key3)== 1
      assert target.totalCount() == 9
      
      Iterator iterator = target.iterator()
      assert iterator.next() == key2
      assert iterator.next() == key1
      assert iterator.next() == key3
   }
}
