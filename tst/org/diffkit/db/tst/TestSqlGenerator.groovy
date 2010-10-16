

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

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import org.diffkit.db.DKDBColumn;
import org.diffkit.db.DKDBConnectionInfo 
import org.diffkit.db.DKDBDatabase 
import org.diffkit.db.DKDBFlavor;
import org.diffkit.db.DKDBPrimaryKey 
import org.diffkit.db.DKDBTable 
import org.diffkit.db.DKDBTableDataAccess;
import org.diffkit.db.DKSqlGenerator;
import org.diffkit.util.DKSqlUtil;

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestSqlGenerator extends GroovyTestCase {
	
		
	public void testGenerateInsert(){
		def table = this.createContextMetaTable()
		def row = [id:1000, LHS_SOURCE: 'lhs source', RHS_SOURCE: 'rhs source', WHEN: new Date(10000), RUN_DATE: new Date(10000) ]
      DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
      DKDBDatabase database = [connectionInfo]
      DKSqlGenerator sqlGenerator = [database]
      
		def insert = sqlGenerator.generateInsertDML(row, table)
		println "insert->$insert"
		assert  StringUtils.deleteWhitespace(insert) == StringUtils.deleteWhitespace(
		"""INSERT INTO DIFF_CONTEXT (id, LHS_SOURCE, RHS_SOURCE, WHEN, RUN_DATE)
		   VALUES (1000, 'lhs source', 'rhs source', '1969-12-3119:00:10', '1969-12-31')"""
		)
		
	}
	
	public void testCreateDropTable(){
		DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
		println "connectionInfo->$connectionInfo"
		DKDBDatabase database = [connectionInfo]
		DKDBTableDataAccess tableDataAccess = [database]
		DKDBTable table = this.createCustomerMetaTable()
		def connection = database.connection
		assert database.createTable( table)
		
		def fetchedTable = tableDataAccess.getTable(null, null, table.tableName)
		assert fetchedTable
		def fetchedPK = fetchedTable.primaryKey
		assert fetchedPK
		assert fetchedTable.primaryKeyColumnIndices == (int[])[0,1]
		
		assert database.dropTable( table)
		fetchedTable = tableDataAccess.getTable(null, null, table.tableName)
		assert !fetchedTable
	}
	
	public void testGenerateDDL(){
		DKDBTable table = this.createCustomerMetaTable()
      DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
      DKDBDatabase database = [connectionInfo]
      DKSqlGenerator sqlGenerator = [database]
      
		String ddl = sqlGenerator.generateCreateDDL(table)
		println "ddl->$ddl"
		
		assert ddl
		assert StringUtils.deleteWhitespace(ddl) == StringUtils.deleteWhitespace(
		"""CREATE TABLE CUSTOMER
		(
		      first_name     VARCHAR(20) NOT NULL,
		      last_name      VARCHAR NOT NULL,
		      address     VARCHAR,
		      code     BIGINT,
		      country     VARCHAR,
		      age      INTEGER,
            CONSTRAINT pk_customer PRIMARY KEY (first_name,last_name)
		)""")
	}
	
	private DKDBTable createContextMetaTable() {
		DKDBColumn column1 = ['ID', 1, 'BIGINT', 20, false]
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
		DKDBColumn column4 = ['code', 2, 'BIGINT', 20, true]
		DKDBColumn column5 = ['country', 2, 'VARCHAR', -1, true]
		DKDBColumn column6 = ['age', 2, 'INTEGER', -1, true]
		DKDBColumn[] columns = [column1, column2, column3, column4, column5, column6]
		String[] pkColNames = ['first_name', 'last_name']
		DKDBPrimaryKey pk = ['pk_customer', pkColNames]
		DKDBTable table = [null, null, 'CUSTOMER', columns, pk]
		return table
	}
}
