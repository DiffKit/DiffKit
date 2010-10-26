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

import org.diffkit.db.DKDBFlavor;
import org.diffkit.db.DKDBType;


import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestDBType extends GroovyTestCase {
   
   public void testGetBaseTypeName() {
      assert DKDBType.getBaseTypeName('VARCHAR') == 'VARCHAR'
      assert DKDBType.getBaseTypeName('VARCHAR()') == 'VARCHAR'
      assert DKDBType.getBaseTypeName('VARCHAR(128)') == 'VARCHAR'
   }
   
   public void testGetConcreteType() {
      assert !DKDBType.getConcreteType(null, null)
      assert !DKDBType.getConcreteType(DKDBFlavor.H2, null)
      assert DKDBType.getConcreteType(null, 'VARCHAR') == DKDBType.VARCHAR
      assert DKDBType.getConcreteType(null, '_H2_IDENTITY') == DKDBType._H2_IDENTITY
      assert DKDBType.getConcreteType(DKDBFlavor.H2, '_H2_IDENTITY') == DKDBType._H2_IDENTITY
      assert DKDBType.getConcreteType(DKDBFlavor.ORACLE, 'VARCHAR') == DKDBType._ORACLE_VARCHAR2
      assert DKDBType.getConcreteType(DKDBFlavor.ORACLE, 'BIGINT') == DKDBType._ORACLE_NUMBER
   }
   
   public void testSqlTypeName(){
      assert DKDBType.VARCHAR.sqlTypeName == 'VARCHAR'
      assert DKDBType._ORACLE_VARCHAR2.sqlTypeName == 'VARCHAR2'
      assert DKDBType._ORACLE_NUMBER.sqlTypeName == 'NUMBER'
   }
   
   public void testConcreteForAbstract(){
      assert !DKDBType.getConcreteTypeForAbstractType(null, null)
      assert !DKDBType.getConcreteTypeForAbstractType(DKDBFlavor.H2 , null)
      
      assert DKDBType.getConcreteTypeForAbstractType(null, DKDBType.VARCHAR) ==DKDBType.VARCHAR
      assert DKDBType.getConcreteTypeForAbstractType(DKDBFlavor.H2, DKDBType.VARCHAR) ==DKDBType.VARCHAR
      assert DKDBType.getConcreteTypeForAbstractType(DKDBFlavor.H2, DKDBType.VARCHAR) ==DKDBType.VARCHAR
      assert DKDBType.getConcreteTypeForAbstractType(DKDBFlavor.ORACLE, DKDBType.VARCHAR) ==DKDBType._ORACLE_VARCHAR2
   }
   
   public void testGetType(){
      assert !DKDBType.getType(null, null)
      assert !DKDBType.getType(DKDBFlavor.H2 , null)
      assert DKDBType.getType(null , 'VARCHAR') == DKDBType.VARCHAR
      shouldFail(RuntimeException) {
         assert !DKDBType.getType(null , 'VARCHAR2')
      }
      assert DKDBType.getType(DKDBFlavor.ORACLE , 'VARCHAR2') == DKDBType._ORACLE_VARCHAR2
   }
   
   public void testFlavorDemangler(){
      def prefixPattern = Pattern.compile('^(_.*_)') 
      def matcher = prefixPattern.matcher('_ORACLE_VARCHAR2')
      assert matcher.find()
      assert matcher.group( ) == '_ORACLE_'
      
      matcher = prefixPattern.matcher('ORACLE_VARCHAR2')
      assert !matcher.find()
   }
}
