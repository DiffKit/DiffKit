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
package org.diffkit.diff.sns.tst

import java.io.File;



import org.diffkit.db.DKDBColumn 
import org.diffkit.db.DKDBConnectionInfo 
import org.diffkit.db.DKDatabase 
import org.diffkit.db.DKDBFlavor;
import org.diffkit.db.DKDBPrimaryKey;
import org.diffkit.db.DKDBTable
import org.diffkit.db.DKDBH2Loader
import org.diffkit.db.DKDBTableDataAccess;
import org.diffkit.diff.engine.DKColumnModel;
import org.diffkit.diff.sns.DKDBSource 
import org.diffkit.diff.sns.DKTableModelUtil;
import org.diffkit.util.DKResourceUtil

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestDBSource extends GroovyTestCase {
   
   public void testKeyColumnNames() {
      DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
      println "connectionInfo->$connectionInfo"
      DKDatabase database = [connectionInfo]
      def connection = database.connection
      def dbTable = this.createCustomerMetaTable()
      assert database.createTable( dbTable)
      
      DKDBSource source = new DKDBSource(dbTable.tableName, null, database, null, (String[])['first_name'], null)
      def model = source.model
      assert model
      assert model.key == [0]
      assert database.dropTable( dbTable)
   }
   
   public void testRead(){
      DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
      println "connectionInfo->$connectionInfo"
      DKDatabase database = [connectionInfo]
      def connection = database.connection
      def dbTable = this.createCustomerMetaTable()
      assert database.createTable( dbTable)
      DKDBH2Loader loader = [database]
      def DKDBTableDataAccess tableDataAccess = [database]
      def fetchedTable = tableDataAccess.getTable(dbTable.tableName)
      def csvFile = this.getCsvFile()
      assert loader.load(dbTable, csvFile)
      def tableModel = DKTableModelUtil.createDefaultTableModel(database.flavor,dbTable, null)
      assert tableModel
      
      // goof up the model to force a fail
      tableModel.columns[0] = new DKColumnModel(0, 'fist_name', DKColumnModel.Type.STRING)
      shouldFail(IllegalArgumentException){
         DKDBSource source = new DKDBSource(dbTable.tableName, null, database, tableModel, null, null)
      }
      
      tableModel.columns[0] = new DKColumnModel(0, 'first_name', DKColumnModel.Type.STRING)
      DKDBSource source = new DKDBSource(dbTable.tableName, null, database, tableModel, null, null)
      source.open(null)
      
      def row = source.nextRow
      assert row
      println "row->$row"
      assert row == (Object[])['rob','smith', '100 spruce st', 'Phila', 'usa', 50]
      
      row = source.nextRow
      assert row == (Object[])['steve','jobs', 'infinite, loop', 'Cupertino', 'usa', 54]
      
      row = source.nextRow
      assert !row
      row = source.nextRow
      assert !row
      
      source.close(null)
      
      shouldFail() { row = source.nextRow }
      
      assert database.dropTable( dbTable)
   }
   
   private File getCsvFile(){
      def csvFile = DKResourceUtil.findResourceAsFile('org/diffkit/db/tst/customers.csv')
      println "csvFile->$csvFile"
      assert csvFile
      return csvFile
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
