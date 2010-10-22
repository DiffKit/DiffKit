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


import org.diffkit.db.DKDBConnectionInfo 
import org.diffkit.db.DKDatabase 
import org.diffkit.db.DKDBFlavor 
import org.diffkit.db.DKDBH2Loader 
import org.diffkit.db.DKDBTableDataAccess;
import org.diffkit.util.DKResourceUtil;
import org.diffkit.util.DKSqlUtil;

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestH2Load extends GroovyTestCase {
	
	public void testLoader(){
		DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
		println "connectionInfo->$connectionInfo"
		DKDatabase connectionSource = [connectionInfo]
		def connection = connectionSource.connection
		println "connection->$connection"
		
		assert connection
		DKDBTableDataAccess tableDataAccess = [connectionSource]
		def table = tableDataAccess.getTable(null, null, 'CUSTOMER')
		if(table )
			DKSqlUtil.executeUpdate('DROP TABLE CUSTOMER', connection)
		this.createTable(connection)
		
		def csvFile = this.getCsvFile()
		def loader = new DKDBH2Loader(connectionSource)
		table = tableDataAccess.getTable(null, null, 'CUSTOMER')
		assert table
		assert loader.load( table, csvFile)
		
		def rows = DKSqlUtil.readRows('SELECT * FROM CUSTOMER', connection)
		assert rows
		println "rows->$rows"
		assert rows.size() ==2
		def rob = rows.find { it['FIRST_NAME'] == 'rob'}
		assert rob
		assert rob['LAST_NAME'] == 'smith'
		assert rob['AGE'] == 50
		
		assert DKSqlUtil.executeUpdate('DROP TABLE CUSTOMER', connection)
		DKSqlUtil.close(connection)
	}
	
	public void testLoad(){
		
		DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
		println "connectionInfo->$connectionInfo"
		DKDatabase connectionSource = [connectionInfo]
		def connection = connectionSource.connection
		println "connection->$connection"
		
		assert connection
		this.createTable(connection)
		
		def csvFile = this.getCsvFile()
		def insertSql =  "INSERT INTO customer (SELECT * FROM CSVREAD('${csvFile.getAbsolutePath()}') );"
		println "insertSql->$insertSql"
		DKSqlUtil.executeUpdate( insertSql, connection)
		
		def rows = DKSqlUtil.readRows('SELECT * FROM customer', connection)
		assert rows
		println "rows->$rows"
		assert rows.size() ==2
		assert rows.find { it['FIRST_NAME'] == 'rob'}
		assert rows.find { it['LAST_NAME'] == 'jobs'}
		
		
		assert DKSqlUtil.executeUpdate('DROP TABLE customer', connection)
	}
	
	public void testReadFromCSV(){
		def csvFile = this.getCsvFile()
		def selectSql =  "SELECT * FROM CSVREAD('${csvFile.getAbsolutePath()}');"
		println "selectSql->$selectSql"
		
		DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
		println "connectionInfo->$connectionInfo"
		DKDatabase connectionSource = [connectionInfo]
		def connection = connectionSource.connection
		println "connection->$connection"
		
		assert connection
		def rows = DKSqlUtil.readRows(selectSql, connection)
		assert rows
		println "rows->$rows"
		assert rows.size() == 2
		assert rows.find { it['FIRST_NAME'] == 'rob'}
		assert rows.find { it['LAST_NAME'] == 'jobs'}
		
	}
	
	private File getCsvFile(){
		def csvFile = DKResourceUtil.findResourceAsFile('org/diffkit/db/tst/customers.csv')
		println "csvFile->$csvFile"
		assert csvFile
		return csvFile		
	}
	
	private void createTable(def connection_) {
		def createTableSql = 
		"""CREATE TABLE customer
            (  first_name    varchar(50),
               last_name     varchar(50),
               address       varchar(50),
               city          varchar(50),
               country       varchar(25),
		         age           integer )
      """         
		
		assert DKSqlUtil.executeUpdate(createTableSql, connection_)
	}
	
	private void dropTable(def connection_){
		assert DKSqlUtil.executeUpdate('DROP TABLE customer', connection_)
	}
	
}
