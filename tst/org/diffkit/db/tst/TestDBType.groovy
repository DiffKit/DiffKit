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
package org.diffkit.db.tst


import java.util.regex.Pattern;

import org.diffkit.db.DKDBConnectionInfo;
import org.diffkit.db.DKDBDatabase 
import org.diffkit.db.DKDBFlavor;
import org.diffkit.db.DKDBTypeInfoDataAccess 

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestDBType extends GroovyTestCase {
   
   public void testMap(){
      def prefixPattern = Pattern.compile('^(_.*_)') 
      def matcher = prefixPattern.matcher('_ORACLE_VARCHAR2')
      assert matcher.find()
      assert matcher.group( ) == '_ORACLE_'
      
      matcher = prefixPattern.matcher('ORACLE_VARCHAR2')
      assert !matcher.find()
   }
}
