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


import java.sql.Time 
import java.sql.Timestamp;

import groovy.util.GroovyTestCase;
import org.diffkit.common.DKUserException 
import org.diffkit.diff.sns.DKPoiSheet 
import org.diffkit.diff.engine.DKColumnModel;
import org.diffkit.diff.engine.DKTableModel 
import org.diffkit.diff.engine.DKColumnModel.Type;

import org.diffkit.util.DKResourceUtil 
import org.diffkit.util.DKTimeUtil;


/**
 * @author jpanico
 */
public class TestPoiSheet extends GroovyTestCase {
   
   // isSorted = false, so Iterator should sort the rows on the simple key; 
   // user defined model does not include ROW_NUM
   public void testSortMedium() {
      DKColumnModel col1 = [0, 'A', DKColumnModel.Type.STRING]
      DKColumnModel col2 = [1, 'B', DKColumnModel.Type.INTEGER]
      DKColumnModel col3 = [2, 'C', DKColumnModel.Type.DATE]
      DKColumnModel col4 = [3, 'D', DKColumnModel.Type.STRING]
      DKColumnModel col5 = [4, 'E', DKColumnModel.Type.TIME]
      DKColumnModel col6 = [5, 'F', DKColumnModel.Type.INTEGER]
      DKColumnModel col7 = [6, 'G', DKColumnModel.Type.DECIMAL]
      DKColumnModel col8 = [7, 'H', DKColumnModel.Type.DECIMAL]
      DKColumnModel col9 = [8, 'I', DKColumnModel.Type.DECIMAL]
      DKColumnModel col10 = [9, 'J', DKColumnModel.Type.TIMESTAMP]
      DKColumnModel col11 = [10, 'K', DKColumnModel.Type.STRING]
      DKColumnModel col12 = [12, 'L', DKColumnModel.Type.INTEGER]
      DKColumnModel col13 = [13, 'M', DKColumnModel.Type.INTEGER]
      DKColumnModel col14 = [14, 'N', DKColumnModel.Type.STRING]
      DKColumnModel col15 = [15, 'O', DKColumnModel.Type.STRING]
      DKColumnModel col16 = [16, 'P', DKColumnModel.Type.STRING]
      DKColumnModel[] cols = [col1,col2,col3,col4,col5,col6,col7,col8, col9, col10, col11, col12, col13,col14,col15,col16]
      DKTableModel tableModel = ['MyModel', cols, (int[])[0]]
      assert !tableModel.hasRowNum()
      
      def sourceFile = DKResourceUtil.findResourceAsFile('xcel_test.xls', this)
      println "sourceFile->$sourceFile"
      assert sourceFile
      DKPoiSheet poiSheet = [sourceFile, "easy sheet", false, false, false]
      Iterator rowIterator = poiSheet.getRowIterator(poiSheet.createModelFromSheet())
      assert rowIterator
      assert rowIterator.hasNext()
      def aRow = rowIterator.next()
      assert aRow
      assert aRow[0] == 1
      aRow = rowIterator.next()
      assert aRow[0] == 2
      (1..10).each { rowIterator.next()}
      aRow = rowIterator.next()
      assert aRow[0] == 13
      (1..7).each { aRow = rowIterator.next()}
      assert aRow[0] == 20
      assert aRow[1] == 'uuuu'
      assert ! rowIterator.hasNext()
   }
   // isSorted = false, so Iterator should sort the rows on ROW_NUM, which means
   // no change in ordering
   public void testSortEasy() {
      def sourceFile = DKResourceUtil.findResourceAsFile('xcel_test.xls', this)
      println "sourceFile->$sourceFile"
      assert sourceFile
      DKPoiSheet poiSheet = [sourceFile, "easy sheet", false, false, false]
      Iterator rowIterator = poiSheet.getRowIterator(poiSheet.createModelFromSheet())
      assert rowIterator
      assert rowIterator.hasNext()
      def aRow = rowIterator.next()
      assert aRow
      assert aRow[0] == 1
      aRow = rowIterator.next()
      assert aRow[0] == 2
      (1..10).each { rowIterator.next()}
      aRow = rowIterator.next()
      assert aRow[0] == 13
      (1..7).each { aRow = rowIterator.next()}
      assert aRow[0] == 20
      assert aRow[1] == 'uuuu'
      assert ! rowIterator.hasNext()
   }
   
   // read rows using RowIterator, based on model explicitly supplied by Unit. 
   // There is no ROW_NUM col specified, so that column shouldn't show up.
   public void testRowIteratorWithModelEasy() {
      DKColumnModel col1 = [0, 'COLUMN1', DKColumnModel.Type.INTEGER]
      DKColumnModel col2 = [1, 'COLUMN2', DKColumnModel.Type.STRING]
      DKColumnModel col3 = [2, 'COLUMN3', DKColumnModel.Type.DECIMAL]
      DKColumnModel col4 = [3, 'COLUMN4', DKColumnModel.Type.DECIMAL]
      DKColumnModel col5 = [4, 'COLUMN5', DKColumnModel.Type.STRING]
      DKColumnModel col6 = [5, 'COLUMN6', DKColumnModel.Type.STRING]
      DKColumnModel col7 = [6, 'COLUMN7', DKColumnModel.Type.REAL]
      DKColumnModel col8 = [7, 'COLUMN8', DKColumnModel.Type.STRING]
      DKColumnModel[] cols = [col1,col2,col3,col4,col5,col6,col7,col8]
      DKTableModel tableModel = ['MyModel', cols, (int[])[0]]
      assert !tableModel.hasRowNum()
      
      def sourceFile = DKResourceUtil.findResourceAsFile('xcel_test.xls', this)
      println "sourceFile->$sourceFile"
      assert sourceFile
      DKPoiSheet poiSheet = [sourceFile, "Sheet1", true, true, false]
      Iterator rowIterator = poiSheet.getRowIterator(tableModel)
      assert rowIterator
      assert rowIterator.hasNext()
      def aRow = rowIterator.next()
      assert aRow
      // first row should be row_num=2, because there is a header, first 
      // column is COLUMN1, because there is no ROW_NUM
      assert aRow[0] == 11111
      
      rowIterator.next()
      rowIterator.next()
      aRow = rowIterator.next()
      
      assert aRow
      assert aRow[0] == -2222
      assert aRow[0].class == Long.class
      assert aRow[1] == '       '
      assert aRow[1].class == String.class
      assert aRow[2] == 0.0
      assert aRow[2].class == BigDecimal.class
      assert aRow[3] == 3.0
      assert aRow[3].class == BigDecimal.class
      assert aRow[4] == '-1.0'
      assert aRow[4].class == String.class
      // ????
      assert aRow[5].toString() == '31-Dec-1899'
      assert aRow[5].class == String.class
      assert aRow[6] == 14.2
      assert aRow[6].class == Double.class
      assert aRow[7] == 'FALSE'
      assert aRow[7].class == String.class
      
   }
   
   // read rows using RowIterator, based on model extracted from Sheet
   public void testRowIteratorHard() {
      def sourceFile = DKResourceUtil.findResourceAsFile('xcel_test.xls', this)
      println "sourceFile->$sourceFile"
      assert sourceFile
      DKPoiSheet poiSheet = [sourceFile, "Sheet1", true, true, false]
      Iterator rowIterator = poiSheet.getRowIterator(poiSheet.createModelFromSheet())
      assert rowIterator
      assert rowIterator.hasNext()
      def aRow = rowIterator.next()
      assert aRow
      assert aRow.class == Object[].class
      // first row should be row_num=2, because there is a header
      assert aRow[0] == 2
      rowIterator.next()
      rowIterator.next()
      aRow = rowIterator.next()
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
   }
   
   // read rows using RowIterator, based on model extracted from Sheet
   public void testRowIteratorEasy() {
      def sourceFile = DKResourceUtil.findResourceAsFile('xcel_test.xls', this)
      println "sourceFile->$sourceFile"
      assert sourceFile
      DKPoiSheet poiSheet = [sourceFile, "easy sheet", true, false, false]
      Iterator rowIterator = poiSheet.getRowIterator(poiSheet.createModelFromSheet())
      assert rowIterator
      assert rowIterator.hasNext()
      def aRow = rowIterator.next()
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
      while(rowIterator.hasNext()) {
         aRow = rowIterator.next();
         print "aRow->${aRow[0]} "
         println "${aRow[1]} "
      }
      assert !rowIterator.hasNext()
   }
   
   // read row using types explicitly specified in Unit
   public void testReadRowHard() {
      def sourceFile = DKResourceUtil.findResourceAsFile('xcel_test.xls', this)
      println "sourceFile->$sourceFile"
      assert sourceFile
      DKPoiSheet poiSheet = [sourceFile, "Sheet1", false, false, false]
      def rows = poiSheet.rows
      assert rows
      // ROW_NUM = 4
      def row = rows.get(4)
      Type[] types = [Type.INTEGER,Type.INTEGER,Type.STRING,Type.DECIMAL,Type.DECIMAL,Type.MIXED,Type.TIME,Type.REAL,Type.BOOLEAN]
      def result = DKPoiSheet.readRow( row, types, true)
      assert result
      assert result.length == types.length
      assert result[0] == 5
      assert result[0].class == Integer.class
      assert result[1] == -2222
      assert result[1].class == Long.class
      assert result[2] == '       '
      assert result[2].class == String.class
      assert result[3] == 0.0
      assert result[3].class == BigDecimal.class
      assert result[4] == 3.0
      assert result[4].class == BigDecimal.class
      assert result[5] == '-1.0'
      assert result[5].class == String.class
      assert result[6].toString() == '05:00:00'
      assert result[6].class == Time.class
      assert result[7] == 14.2
      assert result[7].class == Double.class
      assert result[8] == Boolean.FALSE
      assert result[8].class == Boolean.class
   }
   
   // read row using types explicitly specified in Unit
   public void testReadRowEasy() {
      def sourceFile = DKResourceUtil.findResourceAsFile('xcel_test.xls', this)
      println "sourceFile->$sourceFile"
      assert sourceFile
      DKPoiSheet poiSheet = [sourceFile, "easy sheet", false, false, false]
      def rows = poiSheet.rows
      assert rows
      def row = rows.get(0)
      Type[] types = [Type.INTEGER,Type.STRING,Type.INTEGER,Type.DATE,Type.STRING,Type.TIME,Type.INTEGER,Type.DECIMAL,Type.DECIMAL,Type.DECIMAL,Type.TIMESTAMP,Type.BOOLEAN,Type.INTEGER,Type.INTEGER,Type.STRING,Type.STRING,Type.STRING]
      def result = DKPoiSheet.readRow( row, types, true)
      assert result
      assert result.length == types.length
      assert result[0] == 1
      assert result[0].class == Integer.class
      assert result[1] == 'aaaa'
      assert result[1].class == String.class
      assert result[2] == 1111
      assert result[2].class == Long.class
      assert result[3] == DKTimeUtil.createDate( 2008, 0, 1)
      assert result[3].class == Date.class
      assert result[4] == 'zzzz'
      assert result[4].class == String.class
      assert result[5].toString() == '00:31:31'
      assert result[5].class == Time.class
      assert result[6] == 1234
      assert result[6].class == Long.class
      assert result[7] == 123456.78
      assert result[7].class == BigDecimal.class
      assert result[8] == 1234.5678
      assert result[8].class == BigDecimal.class
      assert result[9] == 1234.5678
      assert result[9].class == BigDecimal.class
      assert result[10].toString() == '2004-05-23 14:25:10.487'
      assert result[10].class == Timestamp.class
      assert result[11] == Boolean.TRUE
      assert result[11].class == Boolean.class
      assert result[12] == 10
      assert result[12].class == Long.class
      assert result[13] == 12345
      assert result[13].class == Long.class
      assert result[14] == 'column14'
      assert result[14].class == String.class
      assert result[15] == 'column15'
      assert result[15].class == String.class
      assert result[16] == 'my clobby text'
      assert result[16].class == String.class
   }
   
   // read cells using types explicitly specified in Unit
   public void testReadCellHard() {
      def sourceFile = DKResourceUtil.findResourceAsFile('xcel_test.xls', this)
      println "sourceFile->$sourceFile"
      assert sourceFile
      DKPoiSheet poiSheet = [sourceFile, "Sheet1", false, false, false]
      def rows = poiSheet.rows
      assert rows
      // ROW_NUM = 4
      def row = rows.get(3)
      assert DKPoiSheet.readCell( row.getCell(0), Type.INTEGER).class == Long.class
      assert DKPoiSheet.readCell( row.getCell(1), Type.STRING).class == String.class
      assert !DKPoiSheet.readCell( row.getCell(2), Type.DECIMAL)
      assert DKPoiSheet.readCell( row.getCell(3), Type.DECIMAL).class == BigDecimal.class
      assert !DKPoiSheet.readCell( row.getCell(4), Type.DATE)
      assert DKPoiSheet.readCell( row.getCell(5), Type.TIME).class == Time.class
      assert DKPoiSheet.readCell( row.getCell(6), Type.REAL).class == Double.class
      assert DKPoiSheet.readCell( row.getCell(7), Type.BOOLEAN).class == Boolean.class
      
      // ROW_NUM = 5
      row = rows.get(4)
      assert DKPoiSheet.readCell( row.getCell(4), Type.MIXED).class == String.class
      assert DKPoiSheet.readCell( row.getCell(7), Type.BOOLEAN).class == Boolean.class
   }
   
   // read cells using types explicitly specified in Unit
   public void testReadCellEasy() {
      def sourceFile = DKResourceUtil.findResourceAsFile('xcel_test.xls', this)
      println "sourceFile->$sourceFile"
      assert sourceFile
      DKPoiSheet poiSheet = [sourceFile, "easy sheet", false, false, false]
      def rows = poiSheet.rows
      assert rows
      def row = rows.get(0)
      assert DKPoiSheet.readCell( row.getCell(0), Type.STRING).class == String.class
      assert DKPoiSheet.readCell( row.getCell(1), Type.DECIMAL).class == BigDecimal.class
      assert DKPoiSheet.readCell( row.getCell(2), Type.DATE).class == Date.class
      assert DKPoiSheet.readCell( row.getCell(4), Type.TIME).class == Time.class
      assert DKPoiSheet.readCell( row.getCell(9), Type.TIMESTAMP).class == Timestamp.class
      assert DKPoiSheet.readCell( row.getCell(10), Type.BOOLEAN).class == Boolean.class
   }
   
   public void testCreateModel() {
      def sourceFile = DKResourceUtil.findResourceAsFile('xcel_test.xls', this)
      println "sourceFile->$sourceFile"
      assert sourceFile
      DKPoiSheet poiSheet = [sourceFile, "easy sheet", false, false, false]
      def model = poiSheet.createModelFromSheet()
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
      
      poiSheet = [sourceFile, "Sheet1", false, true, false]
      model = poiSheet.createModelFromSheet()
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
   }
   
   public void testDiscoverColumnTypes(){
      def sourceFile = DKResourceUtil.findResourceAsFile('xcel_test.xls', this)
      println "sourceFile->$sourceFile"
      assert sourceFile
      DKPoiSheet poiSheet = [sourceFile, "easy sheet", false, false, false]
      def columnTypes = poiSheet.discoverColumnTypes(poiSheet.getRows())
      assert columnTypes
      assert columnTypes.size() == 16
      assert columnTypes[0] == DKColumnModel.Type.STRING
      assert columnTypes[1] == DKColumnModel.Type.DECIMAL
      assert columnTypes[2] == DKColumnModel.Type.DATE
      assert columnTypes[3] == DKColumnModel.Type.STRING
      assert columnTypes[4] == DKColumnModel.Type.TIME
      assert columnTypes[5] == DKColumnModel.Type.DECIMAL
      assert columnTypes[6] == DKColumnModel.Type.DECIMAL
      assert columnTypes[7] == DKColumnModel.Type.DECIMAL
      assert columnTypes[8] == DKColumnModel.Type.DECIMAL
      assert columnTypes[9] == DKColumnModel.Type.TIMESTAMP
      assert columnTypes[10] == DKColumnModel.Type.STRING
      assert columnTypes[11] == DKColumnModel.Type.INTEGER
      assert columnTypes[12] == DKColumnModel.Type.DECIMAL
      assert columnTypes[13] == DKColumnModel.Type.STRING
      assert columnTypes[14] == DKColumnModel.Type.STRING
      assert columnTypes[15] == DKColumnModel.Type.STRING
      
      poiSheet = [sourceFile, "Sheet1", false, true, false]
      columnTypes = poiSheet.discoverColumnTypes(poiSheet.getRows())
      assert columnTypes
      assert columnTypes.size() == 8
      assert columnTypes[0] == DKColumnModel.Type.INTEGER
      assert columnTypes[1] == DKColumnModel.Type.STRING
      assert columnTypes[2] == DKColumnModel.Type.DECIMAL
      assert columnTypes[3] == DKColumnModel.Type.DECIMAL
      assert columnTypes[4] == DKColumnModel.Type.MIXED
      assert columnTypes[5] == DKColumnModel.Type.MIXED
      assert columnTypes[6] == DKColumnModel.Type.REAL
      assert columnTypes[7] == DKColumnModel.Type.STRING
   }
   
   public void testGetHeaderRow(){
      def sourceFile = DKResourceUtil.findResourceAsFile('xcel_test.xls', this)
      println "sourceFile->$sourceFile"
      assert sourceFile
      DKPoiSheet poiSheet = [sourceFile, null, false, false, false]
      assert !poiSheet.getHeaderRow()
      poiSheet = [sourceFile, null, false, true, false]
      def headerRow =  poiSheet.getHeaderRow()
      assert headerRow
      println "lastCellNum->$headerRow.lastCellNum"
      println "firstCellNum->$headerRow.firstCellNum"
   }
   
   public void testGetRows(){
      def sourceFile = DKResourceUtil.findResourceAsFile('xcel_test.xls', this)
      println "sourceFile->$sourceFile"
      assert sourceFile
      DKPoiSheet poiSheet = [sourceFile, null, false, false, false]
      def rows = poiSheet.rows
      assert rows
      assert rows.size() == 5
   }
   
   public void testValidateLazy(){
      File doesNotExistFile = ['./does_not_exist']
      assert !doesNotExistFile.exists()
      DKPoiSheet poiSheet = new DKPoiSheet(doesNotExistFile, null, false, false, true)
      // this is a validation failure
      shouldFail(DKUserException) { poiSheet.validate() }
      
      // this is a validation failure
      shouldFail(DKUserException) { 
         poiSheet = new DKPoiSheet(doesNotExistFile, null, false, false, false)
      }
   }
   
   public void testGetSheet(){
      def sourceFile = DKResourceUtil.findResourceAsFile('xcel_test.xls', this)
      println "sourceFile->$sourceFile"
      assert sourceFile
      DKPoiSheet poiSheet = [sourceFile, null, false, false, false]
      def internalSheet = poiSheet.getSheet()
      println "internalSheet->$internalSheet"
      assert internalSheet
      assert internalSheet.sheetName == 'Sheet1'
      
      poiSheet = [sourceFile, 'Sheet1', false, false, false]
      internalSheet = poiSheet.getSheet()
      println "internalSheet->$internalSheet"
      assert internalSheet
      assert internalSheet.sheetName == 'Sheet1'
      
      poiSheet = [sourceFile, 'easy sheet', false, false, false]
      internalSheet = poiSheet.getSheet()
      println "internalSheet->$internalSheet"
      assert internalSheet
      assert internalSheet.sheetName == 'easy sheet'
      
      poiSheet = [sourceFile, 'does_not_exist', false, false, false]
      shouldFail(IOException) { 
         internalSheet = poiSheet.getSheet()
      }
   }
}
