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
package org.diffkit.util.tst



import org.diffkit.util.DKMapUtil;

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestMapUtil extends GroovyTestCase {
   
   public void testValueForKeyPrefix(){
      
      def target = ['aaa111':'value1', 'bbb111':'value2','ccc111':'value3','ccc112':'value4','ddd111':'value5']
      
      assert DKMapUtil.getValueForKeyPrefix(target, 'aaa') == 'value1'
      assert DKMapUtil.getValueForKeyPrefix(target, 'bbb') == 'value2'
      assert ! DKMapUtil.getValueForKeyPrefix(target, 'zzz') 
      assert ! DKMapUtil.getValueForKeyPrefix(target, null)
      assert DKMapUtil.getValueForKeyPrefix(target, 'ccc')
   }
}
