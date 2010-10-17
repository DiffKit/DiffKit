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


import org.diffkit.db.DKDBColumn 
import org.diffkit.db.DKDBConnectionInfo 
import org.diffkit.db.DKDBDatabase 
import org.diffkit.db.DKDBFlavor 
import org.diffkit.db.DKDBPrimaryKey 
import org.diffkit.db.DKDBTable;

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestInsertTableLoader extends GroovyTestCase {
   
   public void testCompareTo(){
      DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
      DKDBDatabase database = [connectionInfo]
      DKDBTable table = this.createTestTable()
      
      database.createTable(table)
      assert database.tableExists(table)
      def row = [id:1000, LHS_SOURCE: 'lhs source', RHS_SOURCE: 'rhs source', WHEN: new Date(10000), RUN_DATE: new Date(10000) ]
      
      assert database.dropTable(table)
      assert !database.tableExists(table)
   }
   
   private DKDBTable createTestTable(){
      DKDBColumn column1 = ['first_name', 1, 'VARCHAR', 20, true]
      DKDBColumn column2 = ['last_name', 2, 'VARCHAR', -1, true]
      DKDBColumn[] columns = [column1, column2]
      String[] pkColNames = [ 'last_name']
      DKDBPrimaryKey pk = ['pk_customer', pkColNames]
      DKDBTable table = [ null, null, 'CUSTOMER', columns, pk]
      return table
   }
}
