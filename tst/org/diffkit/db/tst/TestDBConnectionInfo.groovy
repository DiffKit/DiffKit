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


import org.diffkit.db.DKDBConnectionInfo;
import org.diffkit.db.DKDBFlavor;

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestDBConnectionInfobleComparator extends GroovyTestCase {
   
   public void testH2(){
      
      DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
      println "connectionInfo->$connectionInfo"
      
      assert connectionInfo.JDBCUrl
      assert connectionInfo.JDBCUrl == 'jdbc:h2:mem:test'
      assert connectionInfo.driverName == 'org.h2.Driver'
   }
}
