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
package org.diffkit.diff.sns.tst



import org.diffkit.diff.sns.DKAbstractSheet;
import org.diffkit.diff.sns.DKPoiSheet 

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestAbstractSheet extends GroovyTestCase {
   
   public void testConstructSheet(){
      Class[] handlerClasses = (Class[])[DKPoiSheet.class]
      
      shouldFail(IllegalArgumentException) {
         def sheet = DKAbstractSheet.constructSheet( new File('./test.txt'), 'test', false, false, true, handlerClasses)
      }
      def sheet = DKAbstractSheet.constructSheet( new File('./test.xls'), 'test', false, false, true, handlerClasses)
      assert sheet
   }
   
   public void testGetHandlerClassForFile(){
      Class[] handlerClasses = (Class[])[DKPoiSheet.class]
      
      assert !DKAbstractSheet.getHandlerClassForFile( new File("./test.txt"), handlerClasses)
      DKAbstractSheet.getHandlerClassForFile( new File("./test.xls"), handlerClasses) == DKPoiSheet.class
   }
   
   public void testClassHandlesExtension(){
      
      shouldFail(IllegalArgumentException) {
         DKAbstractSheet.classHandlesExtension( this.getClass(), 'tst')
      }
      assert !DKAbstractSheet.classHandlesExtension( DKPoiSheet.class, 'tst')
      assert DKAbstractSheet.classHandlesExtension( DKPoiSheet.class, 'xls')
   }
   
   public void testDefaultColumnName(){
      assert ! DKAbstractSheet.getDefaultColumnName(-1)
      assert DKAbstractSheet.getDefaultColumnName(0) == "A"
      assert DKAbstractSheet.getDefaultColumnName(1) == "B"
      assert DKAbstractSheet.getDefaultColumnName(25) == "Z"
      assert DKAbstractSheet.getDefaultColumnName(26) == "AA"
      assert DKAbstractSheet.getDefaultColumnName(51) == "AZ"
      assert DKAbstractSheet.getDefaultColumnName(52) == "BA"
      assert DKAbstractSheet.getDefaultColumnName(77) == "BZ"
   }
}
