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
import org.diffkit.db.DKDBFlavor 
import org.diffkit.db.DKDBTableDataAccess;

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestDBTableDataAccess extends GroovyTestCase {
   
   public void testGetTables(){
      
      println "startsWith->" + 'TABLE_SCHEMA'.startsWith('TABLE_SCHEM')
      
      DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
      println "connectionInfo->$connectionInfo"
      DKDatabase connectionSource = [connectionInfo]
      def connection = connectionSource.connection
      println "connection->$connection"
      def dbMeta = connection.metaData
      DKDBTableDataAccess tableDataAccess = [connectionSource]
      println "tableDataAccess->$tableDataAccess"
      def tables = tableDataAccess.getTables(null, null, 'TABLES')
      println "tables->$tables"
      
      assert tables
      assert tables.size() == 1
      println "tables[0]->${tables[0].description}"
      
      assert tables[0].tableName == 'TABLES'
      assert tables[0].catalog == 'TEST'
      assert tables[0].schema == 'INFORMATION_SCHEMA'
      assert tables[0].columns 
      assert tables[0].columns.length == 11 
      
      tables[0].columns.each { println "column->${it.description}" } 
      assert tables[0].columns[0].name == 'TABLE_CATALOG'
      assert tables[0].columns[0].ordinalPosition == 1
      assert tables[0].columns[0].DBTypeName == 'VARCHAR'
      assert tables[0].columns[0].size == -1
      assert tables[0].columns[0].nullable == true
   }
   
   public void testTableMaps(){
      
      DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
      println "connectionInfo->$connectionInfo"
      DKDatabase connectionSource = [connectionInfo]
      def connection = connectionSource.connection
      println "connection->$connection"
      def dbMeta = connection.metaData
      DKDBTableDataAccess tableDataAccess = [connectionSource]
      println "tableDataAccess->$tableDataAccess"
      def tableMaps = tableDataAccess.getTableMaps(null, null, null, dbMeta)
      println "tableMaps->$tableMaps"
      
      assert tableMaps
      assert tableMaps.find { it['TABLE_NAME'] == 'CATALOGS'}
      assert tableMaps.find { it['TABLE_NAME'] == 'COLUMNS'}
      assert tableMaps.find { it['TABLE_NAME'] == 'TABLES'}
   }
}
