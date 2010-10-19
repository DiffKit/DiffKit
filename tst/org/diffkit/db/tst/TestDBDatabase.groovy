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
import org.diffkit.db.DKDBConnectionInfo;
import org.diffkit.db.DKDBDatabase 
import org.diffkit.db.DKDBFlavor;
import org.diffkit.db.DKDBPrimaryKey 
import org.diffkit.db.DKDBTable;

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestDBDatabase extends GroovyTestCase {
   
   public void testSupportsType() {
      DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
      println "connectionInfo->$connectionInfo"
      DKDBDatabase database = [connectionInfo]
      assert database.canConnect()
      assert database.supportsType('VARCHAR')
      assert database.supportsType('BOOLEAN')
   }
   
   public void testCanConnect(){
      
      DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
      println "connectionInfo->$connectionInfo"
      DKDBDatabase database = [connectionInfo]
      assert database.canConnect()
   }
   
   public void testTableExists(){
      DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
      DKDBDatabase database = [connectionInfo]
      def table = this.createCustomerMetaTable()
      assert table
      assert !database.tableExists(table)
      assert database.createTable(table)
      assert database.tableExists(table)
      assert database.dropTable(table)
      assert !database.tableExists(table)
   }
   
   public void testH2(){
      DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
      println "connectionInfo->$connectionInfo"
      DKDBDatabase database = [connectionInfo]
      def connection = database.connection
      println "connection->$connection"
      
      assert connection
      def meta = connection.metaData
      println "meta->$meta"
      assert meta
   }
   
   private DKDBTable createCustomerMetaTable(){
      DKDBColumn column1 = ['first_name', 1, 'VARCHAR', 20, true]
      DKDBColumn column2 = ['last_name', 2, 'VARCHAR', -1, true]
      DKDBColumn column3 = ['address', 2, 'VARCHAR', -1, true]
      DKDBColumn column4 = ['city', 2, 'VARCHAR', -1, true]
      DKDBColumn column5 = ['country', 2, 'VARCHAR', -1, true]
      DKDBColumn column6 = ['age', 2, 'INTEGER', -1, true]
      DKDBColumn[] columns = [column1, column2, column3, column4, column5, column6]
      String[] pkColNames = ['first_name', 'last_name']
      DKDBPrimaryKey pk = ['pk_customer', pkColNames]
      DKDBTable table = [ null, null, 'CUSTOMER', columns, pk]
      return table
   }
}
