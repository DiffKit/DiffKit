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



import org.diffkit.util.DKArrayUtil;

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestArrayUtil extends GroovyTestCase {
   
   public void testRemoveElements(){
      
      assert !DKArrayUtil.removeElementsAtIndices(null, null) 
      assert !DKArrayUtil.removeElementsAtIndices(null, (int[])[1,2])
      assert DKArrayUtil.removeElementsAtIndices((Integer[])[111,222,333,444,555], null) == [111,222,333,444,555]
      assert DKArrayUtil.removeElementsAtIndices((Integer[])[111,222,333,444,555], (int[])[]) == [111,222,333,444,555]
      assert DKArrayUtil.removeElementsAtIndices((Integer[])[111,222,333,444,555], (int[])[9]) == [111,222,333,444,555]
      assert DKArrayUtil.removeElementsAtIndices((Integer[])[111,222,333,444,555], (int[])[9,100,1000]) == [111,222,333,444,555]
      assert DKArrayUtil.removeElementsAtIndices((Integer[])[111,222,333,444,555], (int[])[0]) == [222,333,444,555]
      assert DKArrayUtil.removeElementsAtIndices((Integer[])[111,222,333,444,555], (int[])[4]) == [111,222,333,444]
      assert DKArrayUtil.removeElementsAtIndices((Integer[])[111,222,333,444,555], (int[])[2,3]) == [111,222,555]
      assert DKArrayUtil.removeElementsAtIndices((Integer[])[111,222,333,444,555], (int[])[0,1,2,3,4]) == []
   }
}
