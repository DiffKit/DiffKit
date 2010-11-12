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
import org.diffkit.db.DKDBPrimaryKey 
import org.diffkit.db.DKDBTable;
import org.diffkit.db.DKDBType;
import org.diffkit.db.DKDBTypeInfo;
import org.diffkit.db.DKDatabase 
import org.diffkit.db.DKDBFlavor;
import org.diffkit.db.DKDBTableDataAccess 

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestVolatileStuff extends GroovyTestCase {
   
   public void testEach(){
      def success = true
      [true, false, true].each{ success = success &&  it; println success }
   }
   
   public void testSQLServer(){
      DKDBConnectionInfo connectionInfo = ['sqlserver', DKDBFlavor.SQLSERVER,'test', '10.0.1.11', 1433, 'diffkit', 'diffkit']
      println "connectionInfo->$connectionInfo"
      DKDatabase database = [connectionInfo]
      def connection = database.connection
      println "connection->$connection"
      assert connection
      DKDBTableDataAccess tableDataAccess = [database]
      println "tableDataAccess->$tableDataAccess"
      def tables = tableDataAccess.getTables(null, null, 'test')
      println "tables->$tables"
      println "TABLES->${tables[0].description}"
      assert tables[0]
      assert tables[0].schema == 'dbo'
      assert database.supportsType('VARCHAR')
   }
   
   public void tXstMySQL(){
      DKDBConnectionInfo connectionInfo = ['mysql', DKDBFlavor.MYSQL,'DiffKit', 'localhost', 3306, 'root', '']
      println "connectionInfo->$connectionInfo"
      DKDatabase database = [connectionInfo]
      def connection = database.connection
      println "connection->$connection"
      assert connection
      DKDBTableDataAccess tableDataAccess = [database]
      println "tableDataAccess->$tableDataAccess"
      def tables = tableDataAccess.getTables(null, null, 'test')
      println "tables->$tables"
      println "TABLES->${tables[0].description}"
      assert database.supportsType('VARCHAR')
   }
   
   public void tXstSupportsType() {
      DKDBConnectionInfo connectionInfo = ['db2', DKDBFlavor.DB2,'SAMPLE', '10.0.1.11', 50000, 'db2admin', 'torabora']
      println "connectionInfo->$connectionInfo"
      DKDatabase database = [connectionInfo]
      assert database.canConnect()
      assert database.supportsType('VARCHAR')
      //      assert database.supportsType('BOOLEAN')
   }
   
   public void tXstDB2(){
      DKDBConnectionInfo connectionInfo = ['db2', DKDBFlavor.DB2,'SAMPLE', '10.0.1.11', 50000, 'db2admin', 'torabora']
      println "connectionInfo->$connectionInfo"
      DKDatabase database = [connectionInfo]
      def connection = database.connection
      println "connection->$connection"
      assert connection
      DKDBTableDataAccess tableDataAccess = [database]
      println "tableDataAccess->$tableDataAccess"
      def tables = tableDataAccess.getTables(null, 'SYSCAT', 'TABLES')
      println "tables->$tables"
      println "TABLES->${tables[0].description}"
   }
   
   public void tXstOracle(){
      DKDBConnectionInfo connectionInfo = ['oracle', DKDBFlavor.ORACLE,'XE', '10.0.1.11', 1521, 'diffkit', 'diffkit']
      println "connectionInfo->$connectionInfo"
      DKDatabase database = [connectionInfo]
      def connection = database.connection
      println "connection->$connection"
      
      assert connection
      def meta = connection.metaData
      println "meta->$meta"
      assert meta
      DKDBTableDataAccess tableDataAccess = [database]
      println "tableDataAccess->$tableDataAccess"
      def tables = tableDataAccess.getTables(null, 'SYS', 'USER_TABLES')
      println "tables->$tables"
      println "USER_TABLES->${tables[0].description}"
      assert database.supportsType('_ORACLE_VARCHAR2')
      println "map->${database.typeInfoDataAccess.nameToTypeInfoMap}"
      assert database.supportsType('_ORACLE_NUMBER')
      assert database.supportsType(DKDBType._ORACLE_VARCHAR2)
      assert database.supportsType(DKDBType._ORACLE_NUMBER)
      
      def table = this.createCustomerMetaTable()
      assert table
      if(database.tableExists(table))
         database.dropTable(table)
      database.createTable(table)
      def fetchedTable = database.getTable( table.catalog, table.schema, table.tableName)
      assert fetchedTable
      DKDBTypeInfo[] typeInfos = database.getColumnConcreteTypeInfos(table);
      println "typeInfos->$typeInfos"
   }
   
   public void testH2(){
      
      // start the TCP Server
      //      Server server = Server.createTcpServer(null).start()
      //      Thread.currentThread().sleep(10000)
      //      def driverName = 'org.h2.Driver'
      //      def jdbcUrl = 'jdbc:h2:file:./test'
      //      def username = 'test'
      //      def password = 'test'
      //      
      //      Class.forName(driverName);
      //      
      //      Properties properties = new Properties();
      //      properties.put('user', username);
      //      properties.put('password', password);
      //      DriverManager.getConnection(jdbcUrl, properties);
   }
   private DKDBTable createCustomerMetaTable(){
      DKDBColumn column1 = ['first_name', 1, 'VARCHAR', 20, true]
      DKDBColumn column2 = ['last_name', 2, 'VARCHAR', 20, true]
      DKDBColumn column3 = ['address', 2, 'VARCHAR', 20, true]
      DKDBColumn column4 = ['city', 2, 'VARCHAR', 20, true]
      DKDBColumn column5 = ['country', 2, 'VARCHAR', 20, true]
      DKDBColumn column6 = ['age', 2, 'INTEGER', 20, true]
      DKDBColumn column7 = ['BIRTH', 7, 'DATE', -1, true]
      DKDBColumn[] columns = [column1, column2, column3, column4, column5, column6, column7]
      String[] pkColNames = ['first_name', 'last_name']
      DKDBPrimaryKey pk = ['pk_customer', pkColNames]
      DKDBTable table = [null, null, 'CUSTOMER', columns, pk]
      return table
   }
}
