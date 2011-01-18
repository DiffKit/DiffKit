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
package org.diffkit.diff.sns.tst




import groovy.util.GroovyTestCase;
import java.sql.Time 
import java.sql.Timestamp 

import org.diffkit.diff.engine.DKColumnModel;
import org.diffkit.diff.engine.DKTableModel 
import org.diffkit.diff.engine.DKColumnModel.Type 
import org.diffkit.diff.sns.DKSpreadSheetFileSource 
import org.diffkit.util.DKResourceUtil 
import org.diffkit.util.DKTimeUtil 


/**
 * @author jpanico
 */
public class TestSpreadSheetSource extends GroovyTestCase {
   
   public void testReadHard() {
      def sourceFile = DKResourceUtil.findResourceAsFile('xcel_test.xls', this)
      println "sourceFile->$sourceFile"
      assert sourceFile
      def source = new DKSpreadSheetFileSource(sourceFile.absolutePath, 'Sheet1', null, null, null, true, true, false)
      source.open(null)
      def aRow = source.nextRow
      assert aRow
      assert aRow.class == Object[].class
      // first row should be ROW_NUM=2, because there is a header
      assert aRow[0] == 2
      source.nextRow
      source.nextRow
      aRow = source.nextRow
      assert aRow
      assert aRow[0] == 5
      assert aRow[0].class == Integer.class
      assert aRow[1] == -2222
      assert aRow[1].class == Long.class
      assert aRow[2] == '       '
      assert aRow[2].class == String.class
      assert aRow[3] == 0.0
      assert aRow[3].class == BigDecimal.class
      assert aRow[4] == 3.0
      assert aRow[4].class == BigDecimal.class
      assert aRow[5] == '-1.0'
      assert aRow[5].class == String.class
      // ????
      assert aRow[6].toString() == '31-Dec-1899'
      assert aRow[6].class == String.class
      assert aRow[7] == 14.2
      assert aRow[7].class == Double.class
      assert aRow[8] == 'FALSE'
      assert aRow[8].class == String.class
      assert ! source.nextRow
      assert ! source.nextRow
      assert source.lastIndex == 3
   }
   
   public void testReadEasy() {
      def sourceFile = DKResourceUtil.findResourceAsFile('xcel_test.xls', this)
      println "sourceFile->$sourceFile"
      assert sourceFile
      def source = new DKSpreadSheetFileSource(sourceFile.absolutePath, 'easy sheet', null, null, null, true, false, false)
      source.open(null)
      def aRow = source.nextRow
      assert aRow
      assert aRow.class == Object[].class
      assert aRow[0] == 1
      assert aRow[0].class == Integer.class
      assert aRow[1] == 'aaaa'
      assert aRow[1].class == String.class
      assert aRow[2] == 1111
      assert aRow[2].class == BigDecimal.class
      assert aRow[3] == DKTimeUtil.createDate( 2008, 0, 1)
      assert aRow[3].class == Date.class
      assert aRow[4] == 'zzzz'
      assert aRow[4].class == String.class
      assert aRow[5].toString() == '00:31:31'
      assert aRow[5].class == Time.class
      assert aRow[6] == 1234
      assert aRow[6].class == BigDecimal.class
      assert aRow[7] == 123456.78
      assert aRow[7].class == BigDecimal.class
      assert aRow[8] == 1234.5678
      assert aRow[8].class == BigDecimal.class
      assert aRow[9] == 1234.5678
      assert aRow[9].class == BigDecimal.class
      assert aRow[10].toString() == '2004-05-23 14:25:10.487'
      assert aRow[10].class == Timestamp.class
      assert aRow[11] == 'TRUE'
      assert aRow[11].class == String.class
      assert aRow[12] == 10
      assert aRow[12].class == Long.class
      assert aRow[13] == 12345
      assert aRow[13].class == BigDecimal.class
      assert aRow[14] == 'column14'
      assert aRow[14].class == String.class
      assert aRow[15] == 'column15'
      assert aRow[15].class == String.class
      assert aRow[16] == 'my clobby text'
      assert aRow[16].class == String.class
      assert source.lastIndex ==0
      (1..19).each { assert source.nextRow }
      assert ! source.nextRow
      assert source.lastIndex == 19
   }
   
   public void testModelColumns() {
      def sourceFile = DKResourceUtil.findResourceAsFile('xcel_test.xls', this)
      println "sourceFile->$sourceFile"
      assert sourceFile
      assert sourceFile.canRead()
      
      // explicitly specified sheet name happens to be first sheet, no model, with header
      def source = new DKSpreadSheetFileSource(sourceFile.absolutePath, 'Sheet1', null, null, null, true, true, false)
      def model = source.model
      assert model
      assert model.name == 'Sheet1'
      assert model.keyColumnNames == (String[])['<ROW_NUM>']
      assert model.columns.length == 9
      assert model.columns[0].name == '<ROW_NUM>'
      assert model.columns[0].type == Type.INTEGER
      assert model.columns[1].name == 'COLUMN1'
      assert model.columns[1].type == Type.INTEGER
      assert model.columns[2].name == 'COLUMN2'
      assert model.columns[2].type == Type.STRING
      assert model.columns[3].name == 'COLUMN3'
      assert model.columns[3].type == Type.DECIMAL
      assert model.columns[5].name == 'COLUMN5'
      assert model.columns[5].type == Type.MIXED
      assert model.columns[7].name == 'COLUMN7'
      assert model.columns[7].type == Type.REAL
      
      source.open(null)
      source.close(null)
      
      // no sheet name supplied, default sheet is first sheet (Sheet1)
      source = new DKSpreadSheetFileSource(sourceFile.absolutePath, null, null, null, null, true, true, false)
      model = source.model
      assert model
      assert model.name == 'Sheet1'
      
      // explicitly specified sheet name is not same as default, no header so get default header names
      source = new DKSpreadSheetFileSource(sourceFile.absolutePath, 'easy sheet', null, null, null, true, false, false)
      model = source.model
      assert model
      assert model.name == 'easy sheet'
      assert model.keyColumnNames == (String[])['<ROW_NUM>']
      assert model.columns.length == 17
      assert model.columns[0].name == '<ROW_NUM>'
      assert model.columns[0].type == Type.INTEGER
      assert model.columns[1].name == 'A'
      assert model.columns[1].type == Type.STRING
      assert model.columns[2].name == 'B'
      assert model.columns[2].type == Type.DECIMAL
      assert model.columns[3].name == 'C'
      assert model.columns[3].type == Type.DATE
      assert model.columns[5].name == 'E'
      assert model.columns[5].type == Type.TIME
      assert model.columns[10].name == 'J'
      assert model.columns[10].type == Type.TIMESTAMP
      assert model.columns[11].name == 'K'
      assert model.columns[11].type == Type.STRING
      
      // automatic Model extraction, but user-supplied key
      source = new DKSpreadSheetFileSource(sourceFile.absolutePath, 'Sheet1', null, (String[])['COLUMN1'], null, true, true, false)
      model = source.model
      assert model
      assert model.name == 'Sheet1'
      assert model.keyColumnNames == (String[])['COLUMN1']
      // NOTE that when have user-supplied key, the ROW_NUM is no longer included
      assert model.columns.length == 8
      
      // supply own model
      DKColumnModel col1 = [0, 'col1', DKColumnModel.Type.DECIMAL]
      DKColumnModel col2 = [1, 'col2', DKColumnModel.Type.STRING]
      DKTableModel myModel = ['myModel', (DKColumnModel[])[col1, col2], (int[])[0]]
      source = new DKSpreadSheetFileSource(sourceFile.absolutePath, 'Sheet1', myModel, null, null, true, true, false)
      model = source.model
      assert model
      assert model.name == 'myModel'
      assert model.keyColumnNames == (String[])['col1']
      assert model.columns.length == 2
      assert model.columns[0].name == 'col1'
      assert model.columns[1].name == 'col2'
   }
}
