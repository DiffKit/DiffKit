package org.diffkit.util.tst

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

import groovy.util.GroovyTestCase;

import org.diffkit.db.DKDBFlavor;
import org.diffkit.diff.testcase.TestCaseRunner;
import org.diffkit.util.DKStringUtil;


/**
 * @author jpanico
 */
public class TestStringUtil extends GroovyTestCase {
   
   public void testParseEnumList() {
      def enumList = DKStringUtil.parseEnumList('ORACLE,DB2,H2', DKDBFlavor)
      assert enumList
      assert enumList.size() == 3
      assert enumList[1] == DKDBFlavor.DB2
   }
   
   public void testParseIntegerList() {
      def integerList = DKStringUtil.parseIntegerList('1,2,3,4')
      assert integerList
      assert integerList.size() == 4
      assert integerList[2] == 3
      assert integerList[2] instanceof Integer
   }
   
   public void testToSetString(){
      assert DKStringUtil.toSetString(['a','b']) == '(a, b)'
      assert DKStringUtil.toSetString([]) == '()'
   }
   
   public void testQuote(){
      assert ! DKStringUtil.quote(null,DKStringUtil.Quote.DOUBLE) 
      assert DKStringUtil.quote("",DKStringUtil.Quote.DOUBLE) == '""'
      assert DKStringUtil.quote("string",DKStringUtil.Quote.SINGLE) == "'string'"
      assert DKStringUtil.quote("'string'",DKStringUtil.Quote.SINGLE) == "'string'"
   }
   
   public void testReplaceEach(){
      def source =   '''Beware the Jabberwock, my son!
                        The jaws that bite, the claws that catch!
                        Beware the Jubjub bird, and shun
                        The frumious Bandersnatch!'''
      def expected =   '''swear the Jabberwock, my son!
                        The jaws that bite, the claws that catch!
                        swear the Jubjub bird, and shun
                        The frumpy Bandersnatch!'''
      def substitutions = ['Beware':'swear', 'frumious':'frumpy']
      
      assert DKStringUtil.replaceEach(source, substitutions) == expected
      
      source =   '''this is my test @TargetDatabase@'''
      expected =   '''this is my test mem:testcase;DB_CLOSE_DELAY=-1'''
      substitutions = [ : ]
      substitutions.put(TestCaseRunner.TARGET_DATABASE_TOKEN,TestCaseRunner.DEFAULT_TESTCASE_DATABASE )
      assert DKStringUtil.replaceEach(source, substitutions) == expected
   }
}
