

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



import org.diffkit.db.DKDBFlavor

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
   
   public void tXstHyperWeird(){
      DKDBConnectionInfo connectionInfo = ['hyper', DKDBFlavor.HYPERSQL,'mem:test', null, -1, 'SA', '']
      println "connectionInfo->$connectionInfo"
      DKDatabase database = [connectionInfo]
      DKDBTableDataAccess tableDataAccess = [database]
      println "tableDataAccess->$tableDataAccess"
      
      def table = this.createTest1LHSTable()
      def createdTable = database.createTable(table)
      assert createdTable
      
      table = this.createTest1RHSTable()
      createdTable = database.createTable(table)
      assert createdTable
   }
   
   public void tXstHyperSQL(){
      DKDBConnectionInfo connectionInfo = ['hyper', DKDBFlavor.HYPERSQL,'mem:test', null, -1, 'SA', '']
      println "connectionInfo->$connectionInfo"
      DKDatabase database = [connectionInfo]
      DKDBTableDataAccess tableDataAccess = [database]
      println "tableDataAccess->$tableDataAccess"
      assert database.supportsType('CHAR')
      def table = this.createCustomerMetaTable()
      assert table
      if(database.tableExists(table))
         database.dropTable(table)
      database.createTable(table)
      def fetchedTable = database.getTable( table.catalog, table.schema, table.tableName)
      assert fetchedTable
      assert fetchedTable.tableName == 'CUSTOMER'
      def columns = table.columns
      assert columns
      assert columns.size() == 7
      assert columns[6].name == 'BIRTH'
      assert columns[6].DBTypeName == 'DATE'
   }
   
   public void tXstPostgres(){
      DKDBConnectionInfo connectionInfo = ['postgres', DKDBFlavor.POSTGRES,'postgres', 'localhost', 5432, 'postgres', 'torabora']
      println "connectionInfo->$connectionInfo"
      DKDatabase database = [connectionInfo]
      def connection = database.connection
      println "connection->$connection"
      assert connection
      DKDBTableDataAccess tableDataAccess = [database]
      println "tableDataAccess->$tableDataAccess"
      assert database.supportsType('_POSTGRES_DOUBLE_PRECISION')
      def tables = tableDataAccess.getTables(null, null, 'TEST')
      println "tables->$tables"
      println "TABLES->${tables[0].description}"
      assert tables[0]
      assert tables[0].schema == 'public'
      assert tables[0].tableName == 'TEST'
      tables = tableDataAccess.getTables(null, null, 'test1_lhs_table')
      assert tables[0]
      assert tables[0].schema == 'public'
      tables = tableDataAccess.getTables(null, null, 'TEST1_LHS_TABLE')
      assert tables[0]
   }
   
   public void testSQLServer(){
      DKDBConnectionInfo connectionInfo = ['sqlserver', DKDBFlavor.SQLSERVER,'test', '10.0.1.8', 1433, 'diffkit', 'diffkit']
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
   
   public void testDB2(){
      DKDBConnectionInfo connectionInfo = ['db2', DKDBFlavor.DB2,'SAMPLE', '10.0.1.8', 50000, 'db2admin', 'torabora']
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
      assert tables
      assert tables.size() == 1
      assert tables[0].tableName == 'TABLES'
   }
   
   public void testOracle(){
      DKDBConnectionInfo connectionInfo = ['oracle', DKDBFlavor.ORACLE,'XE', '10.0.1.8', 1521, 'diffkit', 'diffkit']
      println "connectionInfo->${connectionInfo.JDBCUrl}"
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
   
   private DKDBTable createTest1LHSTable(){
      DKDBColumn column1 = ['COLUMN1', 1, 'VARCHAR', 128, true]
      DKDBColumn column2 = ['COLUMN2', 2, 'VARCHAR', 128, true]
      DKDBColumn column3 = ['COLUMN3', 2, 'BIGINT', 32, true]
      DKDBColumn[] columns = [column1, column2, column3]
      String[] pkColNames = ['COLUMN1', 'COLUMN3']
      DKDBPrimaryKey pk = ['TEST1_LHS_PK', pkColNames]
      DKDBTable table = [null, null, 'TEST1_LHS_TABLE', columns, pk]
      return table
   }
   private DKDBTable createTest1RHSTable(){
      DKDBColumn column1 = ['COLUMN1', 1, 'VARCHAR', 128, true]
      DKDBColumn column2 = ['COLUMN2', 2, 'VARCHAR', 128, true]
      DKDBColumn column3 = ['COLUMN3', 2, 'BIGINT', 32, true]
      DKDBColumn[] columns = [column1, column2, column3]
      String[] pkColNames = ['COLUMN1', 'COLUMN3']
      DKDBPrimaryKey pk = ['TEST1_RHS_PK', pkColNames]
      DKDBTable table = [null, null, 'TEST1_RHS_TABLE', columns, pk]
      return table
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
