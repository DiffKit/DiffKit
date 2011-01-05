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




import java.sql.Timestamp;

import org.diffkit.db.DKDBColumn 
import org.diffkit.db.DKDBConnectionInfo 
import org.diffkit.db.DKDBFlavor 
import org.diffkit.db.DKDBPrimaryKey 
import org.diffkit.db.DKDBTable;
import org.diffkit.db.DKDatabase 
import org.diffkit.diff.conf.DKAutomaticTableComparison;
import org.diffkit.diff.engine.DKColumnDiff 
import org.diffkit.diff.engine.DKColumnDiffRow 
import org.diffkit.diff.engine.DKContext 
import org.diffkit.diff.engine.DKRowDiff 
import org.diffkit.diff.engine.DKSide;
import org.diffkit.diff.engine.DKTableComparison 
import org.diffkit.diff.engine.DKTableModel 
import org.diffkit.diff.sns.DKSqlPatchSink 
import org.diffkit.diff.sns.DKTableModelUtil;


/**
 * @author jpanico
 */
public class TestSqlPatchSink extends GroovyTestCase {
   
   public void testSink() {
      DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
      println "connectionInfo->$connectionInfo"
      DKDatabase database = [connectionInfo]
      def connection = database.connection
      def dbTable = this.createCustomerMetaTable()
      assert database.createTable( dbTable)
      def writer = new StringWriter()
      DKSqlPatchSink sink = [connectionInfo, dbTable.tableName, writer]
      DKContext context = []
      println "sink->$sink"
      println "context->$context"
      
      sink.open(context)
      sink.close(context)
      assert writer.toString() == ''
      
      DKTableModel tableModel = DKTableModelUtil.createDefaultTableModel(DKDBFlavor.H2, dbTable, null)
      assert tableModel
      DKTableComparison tableComparison = DKAutomaticTableComparison.createDefaultTableComparison(tableModel, tableModel, null, null)
      assert tableModel
      Date date = [1000000000000]
      Object[] firstRow = ['bob', 'smith', 'update-addr1', 'city', 'update-country', 55, date, new Timestamp(date.time)]
      writer = new StringWriter()
      sink = [connectionInfo, dbTable.tableName, writer]      
      context = new DKContext(sink, tableComparison)
      context.open()
      
      DKRowDiff diff = [1, firstRow, DKSide.LEFT, tableComparison]
      sink.record( diff, context)
      
      Object[] secondRow = ['john', 'candy', 'candy st', 'candy land', 'CANADA', -1, date, new Timestamp(date.time)]
      diff = [2, secondRow, DKSide.RIGHT, tableComparison]
      sink.record( diff, context)
      
      Object[] thirdRowLeft = ['elton', 'john', 'nyc', 'ny', 'USA', 80, date, new Timestamp(date.time)]
      Object[] thirdRowRight = ['elton', 'john', 'new york', 'new york', 'AMERICA', -1, date, new Timestamp(date.time)]
      
      DKColumnDiffRow columnDiffRow = [3, thirdRowLeft, thirdRowRight, tableComparison]
      context._columnStep = 3
      context._rowStep = 3
      context._lhsColumnIdx = 2
      context._rhsColumnIdx = 2
      DKColumnDiff columnDiff = [columnDiffRow, 3, 'nyc', 'new york']
      sink.record( columnDiff, context)
      
      context._columnStep = 4
      context._lhsColumnIdx = 3
      context._rhsColumnIdx = 3
      columnDiff = [columnDiffRow, 3, 'ny', 'new york']
      sink.record( columnDiff, context)
      
      context.close()
      def patchString = writer.toString()
      println "patchString->$patchString"
      assert patchString.startsWith("INSERT INTO PUBLIC.CUSTOMER (FIRST_NAME, LAST_NAME, ADDRESS, CITY, COUNTRY, AGE, BIRTH, NOW)\nVALUES ('bob', 'smith', 'update-addr1', 'city', 'update-country', 55, '2001-09-08', {ts '2001-09-08 21:46:40'});\n\nDELETE FROM PUBLIC.CUSTOMER\nWHERE (FIRST_NAME='john' ) AND (LAST_NAME='candy' );\n\nUPDATE PUBLIC.CUSTOMER\nSET ADDRESS='nyc', CITY='ny'\nWHERE (FIRST_NAME='elton' ) AND (LAST_NAME='john' );")
      database.dropTable(dbTable)
   }
   
   private DKDBTable createCustomerMetaTable(){
      DKDBColumn column1 = ['FIRST_NAME', 1, 'VARCHAR', 50, true]
      DKDBColumn column2 = ['LAST_NAME', 2, 'VARCHAR', 50, true]
      DKDBColumn column3 = ['ADDRESS', 3, 'VARCHAR', 50, true]
      DKDBColumn column4 = ['CITY', 4, 'VARCHAR', 50, true]
      DKDBColumn column5 = ['COUNTRY', 5, 'VARCHAR', 25, true]
      DKDBColumn column6 = ['AGE', 6, 'INTEGER', -1, true]
      DKDBColumn column7 = ['BIRTH', 7, 'DATE', -1, true]
      DKDBColumn column8 = ['NOW', 8, 'TIMESTAMP', -1, true]
      DKDBColumn[] columns = [column1, column2, column3, column4, column5, column6, column7, column8]
      String[] pkColNames = ['FIRST_NAME', 'LAST_NAME']
      DKDBPrimaryKey pk = ['pk_customer', pkColNames]
      DKDBTable table = [ null, null, 'CUSTOMER', columns, pk]
      return table
   }
}
