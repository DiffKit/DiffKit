/**
 * Copyright 2010 Joseph Panico
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




import groovy.util.GroovyTestCase;
import org.apache.commons.beanutils.converters.IntegerConverter 
import org.diffkit.diff.diffor.DKConvertingDiffor 
import org.diffkit.diff.diffor.DKEqualsDiffor;


/**
 * @author jpanico
 */
public class TestConvertingDiffor extends GroovyTestCase {
   
   public void testDiff(){
      
      DKConvertingDiffor diffor = [null, Long.class, DKEqualsDiffor.instance]
      assert ! diffor.isDiff(new Long(1234), '1234', null)
   }
   
   public void testConvert(){
      
      assert new IntegerConverter(true).convert(Long.class, '1234') == 1234
      
      assert new BigDecimal(1).equals(new BigDecimal(1))
   }
}
