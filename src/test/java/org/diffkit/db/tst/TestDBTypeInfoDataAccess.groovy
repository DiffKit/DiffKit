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
package org.diffkit.db.tst


import org.diffkit.db.DKDBConnectionInfo;
import org.diffkit.db.DKDatabase 
import org.diffkit.db.DKDBFlavor;
import org.diffkit.db.DKDBTypeInfoDataAccess 

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestDBTypeInfoDataAccess extends GroovyTestCase {
   
   public void testMap(){
      
      //      DKDBConnectionInfo connectionInfo = ['db2', DKDBFlavor.DB2,'SAMPLE', '10.0.1.11', 50000, 'db2admin', 'torabora']
      DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
      println "connectionInfo->$connectionInfo"
      DKDatabase database = [connectionInfo]
      DKDBTypeInfoDataAccess typeInfoDataAccess = [database]
      
      def typeName = typeInfoDataAccess.getNameForSqlType(4)
      assert typeName
      assert typeName == 'INTEGER'
   }
}
