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


import java.sql.Time 
import java.sql.Timestamp;

import groovy.util.GroovyTestCase;
import org.diffkit.common.DKUserException 
import org.diffkit.diff.sns.DKPoiSheet 
import org.diffkit.diff.engine.DKColumnModel;
import org.diffkit.diff.engine.DKColumnModel.Type;

import org.diffkit.util.DKResourceUtil 


/**
 * @author jpanico
 */
public class TestPoiSheet extends GroovyTestCase {
   
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
   }
   
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
      assert model.columns[11].type == Type.BOOLEAN
      
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
      assert columnTypes[10] == DKColumnModel.Type.BOOLEAN
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
