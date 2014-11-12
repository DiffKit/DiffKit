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
package org.diffkit.util.tst




import org.diffkit.db.DKDBColumn 
import org.diffkit.db.DKDBConnectionInfo 
import org.diffkit.db.DKDatabase 
import org.diffkit.db.DKDBFlavor;
import org.diffkit.db.DKDBPrimaryKey 
import org.diffkit.db.DKDBTable;
import org.diffkit.util.DKSqlUtil;

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestSqlUtil extends GroovyTestCase {
   
   public void testBatchUpdate() {
      DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test;DB_CLOSE_DELAY=-1", null, null, 'test', 'test']
      println "connectionInfo->$connectionInfo"
      DKDatabase database = [connectionInfo]
      DKDBTable table = this.createTestTable()
      assert table
      if(database.tableExists(table))
         database.dropTable(table)
      database.createTable(table)
      assert database.tableExists(table)
      
      def row0 = ['FIRST_NAME':'0.first_name', 'LAST_NAME': '0.last_name' ]
      def row1 = ['FIRST_NAME':'1.first_name', 'LAST_NAME': '1.last_name' ]
      def rows = [row0,row1]
      def insertStrings = database.generateInsertDML( rows, table)
      println "insertStrings->$insertStrings"
      assert insertStrings
      assert insertStrings.size() == 2
      
      def connection = database.connection
      println "connection->$connection"
      assert connection
      
//      assert DKSqlUtil.executeBatchUpdate(insertStrings, connection) == 2
       DKSqlUtil.executeBatchUpdate(insertStrings, connection) 
      def readRows = database.readAllRows(table)
      assert readRows
      assert readRows.size() == 2
      assert readRows.contains(row0)
      assert readRows.contains(row1)
      
      assert database.dropTable(table)
      assert !database.tableExists(table)
   }
   
   private DKDBTable createTestTable(){
      DKDBColumn column1 = ['FIRST_NAME', 1, 'VARCHAR', 20, true]
      DKDBColumn column2 = ['LAST_NAME', 2, 'VARCHAR', -1, true]
      DKDBColumn[] columns = [column1, column2]
      String[] pkColNames = [ 'LAST_NAME']
      DKDBPrimaryKey pk = ['pk_customer', pkColNames]
      DKDBTable table = [ null, null, 'CUSTOMER', columns, pk]
      return table
   }
   
   public void testDatabaseInfo() {
      DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
      println "connectionInfo->$connectionInfo"
      DKDatabase database = [connectionInfo]
      def connection = database.connection
      println "connection->$connection"
      assert connection
      def dbInfo = DKSqlUtil.getDatabaseInfo(connection)
      println "dbInfo->$dbInfo"
      assert dbInfo
      assert dbInfo[DKSqlUtil.DATABASE_PRODUCT_NAME_KEY] == 'H2'
   }
   
   public void testUpdate() {
      def createTableSql =
            """CREATE TABLE customer
            (  first_name    varchar(50),
               last_name     varchar(50),
               address       varchar(50),
               city          varchar(50),
               country       varchar(25),
               birth_date    date)
      """
      
      DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
      println "connectionInfo->$connectionInfo"
      DKDatabase database = [connectionInfo]
      def connection = database.connection
      
      
      assert DKSqlUtil.executeUpdate(createTableSql, connection)
      assert DKSqlUtil.executeUpdate('DROP TABLE customer', connection)
   }
   
   public void testReadRowsFromSelect(){
      DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
      println "connectionInfo->$connectionInfo"
      DKDatabase database = [connectionInfo]
      def connection = database.connection
      println "connection->$connection"
      
      assert connection
      def rows = DKSqlUtil.readRows('select * from INFORMATION_SCHEMA.TABLES', connection)
      assert rows
      println "rows->$rows"
      assert rows.find { it['TABLE_NAME'] == 'TABLES' }
   }
   
   public void testReadRowsFromResultSet(){
      
      DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
      println "connectionInfo->$connectionInfo"
      DKDatabase database = [connectionInfo]
      def connection = database.connection
      println "connection->$connection"
      
      assert connection
      def meta = connection.metaData
      println "meta->$meta"
      assert meta
      def typeInfo = meta.typeInfo
      assert typeInfo
      
      def typeRows = DKSqlUtil.readRows(typeInfo)
      println "typeRows->$typeRows"
      assert typeRows
      def firstType = typeRows[0]
      assert firstType
      assert firstType.containsKey('TYPE_NAME')
      assert firstType.containsKey('DATA_TYPE')
      assert firstType.containsKey('PRECISION')
      
      assert typeRows.find { it['TYPE_NAME'] == 'BIGINT' }
      assert typeRows.find { it['TYPE_NAME'] == 'CLOB' }
      assert typeRows.find { it['TYPE_NAME'] == 'VARCHAR' }
      
      DKSqlUtil.close(typeInfo)
      DKSqlUtil.close(connection)
   }
}
