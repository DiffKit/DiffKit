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


import org.diffkit.db.DKDBConnectionInfo;
import org.diffkit.db.DKDBDatabase 
import org.diffkit.util.DKSqlUtil;

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestDBConnectionSource extends GroovyTestCase {
	
	public void testH2(){
		
		DKDBConnectionInfo connectionInfo = ['test', DKDBConnectionInfo.Kind.H2,"mem:test", null, null, 'test', 'test']
		println "connectionInfo->$connectionInfo"
		DKDBDatabase connectionSource = [connectionInfo]
		def connection = connectionSource.connection
		println "connection->$connection"
		
		assert connection
		def meta = connection.metaData
		println "meta->$meta"
		assert meta
	}
	
	public void testSqlUtil() {
		def createTableSql = 
		"""CREATE TABLE customer
	         (  first_name    varchar(50),
	            last_name     varchar(50),
	            address       varchar(50),
	            city          varchar(50),
	            country       varchar(25),
	            birth_date    date)
	   """         
		
		DKDBConnectionInfo connectionInfo = ['test', DKDBConnectionInfo.Kind.H2,"mem:test", null, null, 'test', 'test']
		println "connectionInfo->$connectionInfo"
		DKDBDatabase connectionSource = [connectionInfo]
		def connection = connectionSource.connection
		
		assert DKSqlUtil.executeUpdate(createTableSql, connection)
		assert DKSqlUtil.executeUpdate('DROP TABLE customer', connection)
	}
	
}
