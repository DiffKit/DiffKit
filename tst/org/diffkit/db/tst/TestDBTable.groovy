

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

import java.sql.Time 
import java.sql.Timestamp;
import java.util.Calendar;

import org.apache.commons.lang.time.DateUtils;

import org.diffkit.db.DKDBColumn;
import org.diffkit.db.DKDBConnectionInfo 
import org.diffkit.db.DKDatabase 
import org.diffkit.db.DKDBFlavor;
import org.diffkit.db.DKDBPrimaryKey 
import org.diffkit.db.DKDBTable 
import org.diffkit.db.DKDBTableDataAccess;
import org.diffkit.util.DKSqlUtil;

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestDBTable extends GroovyTestCase {
   
   /**
    * are MEM tables specific to a single connection (like temp tables), 
    * or are they visible across connections?
    */
   public void testH2MemTable(){
      DKDBConnectionInfo connectionInfo = ['test',DKDBFlavor.H2,"mem:test;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=2", null, null, 'test', 'test']
      println "connectionInfo->$connectionInfo"
      DKDatabase database = [connectionInfo]
      DKDBTableDataAccess tableDataAccess = [database]
      DKDBTable table = this.createCustomerMetaTable()
      def connection = database.connection
      assert database.createTable( table)
      DKSqlUtil.close(connection)
      def fetchedTable = tableDataAccess.getTable(null, null, table.tableName)
      assert fetchedTable
      
      assert database.dropTable( fetchedTable)
      fetchedTable = tableDataAccess.getTable(null, null, table.tableName)
      assert !fetchedTable
   }
   
   public void testInsert(){
      DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
      println "connectionInfo->$connectionInfo"
      DKDatabase database = [connectionInfo]
      def table = this.createContextMetaTable()
      
      DKDBTableDataAccess tableDataAccess = [database]
      def connection = database.connection
      assert database.createTable( table)
      def fetchedTable = tableDataAccess.getTable(null, null, table.tableName)
      assert fetchedTable
      
      def date = DateUtils.round(new Date(10000), Calendar.DAY_OF_MONTH)
      def row = [ID:1000, LHS_SOURCE: 'lhs source', RHS_SOURCE: 'rhs source', WHEN: new Timestamp(date.time), RUN_DATE: date ]
      assert database.insertRow(row, fetchedTable)
      
      def fetchedRows = database.readAllRows( fetchedTable)
      println "fetchedRows->$fetchedRows"
      assert fetchedRows
      assert fetchedRows.size() == 1
      assert fetchedRows[0]['LHS_SOURCE'] == row['LHS_SOURCE']
      assert fetchedRows[0]['ID'] == row['ID']
      assert fetchedRows[0]['WHEN'] == row['WHEN']
      assert fetchedRows[0]['RUN_DATE'] == row['RUN_DATE']
      
      assert database.dropTable( table)
      fetchedTable = tableDataAccess.getTable(null, null, table.tableName)
      assert !fetchedTable
   }
   
   private DKDBTable createContextMetaTable() {
      DKDBColumn column1 = ['ID', 1, 'BIGINT', -1, false]
      DKDBColumn column2 = ['LHS_SOURCE', 2, 'VARCHAR', -1, true]
      DKDBColumn column3 = ['RHS_SOURCE', 3, 'VARCHAR', -1, true]
      DKDBColumn column4 = ['WHEN', 4, 'TIMESTAMP', -1, true]
      DKDBColumn column5 = ['RUN_DATE', 5, 'DATE', -1, true]
      DKDBColumn[] columns = [column1, column2, column3, column4, column5]
      DKDBTable table = [null, null, 'DIFF_CONTEXT', columns, null]
      return table
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
      DKDBTable table = [null, null, 'CUSTOMER', columns, pk]
      return table
   }
}
