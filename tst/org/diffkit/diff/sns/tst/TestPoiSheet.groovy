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


import groovy.util.GroovyTestCase;
import org.diffkit.common.DKUserException 
import org.diffkit.diff.sns.DKPoiSheet 
import org.diffkit.diff.engine.DKColumnModel;

import org.diffkit.util.DKResourceUtil 


/**
 * @author jpanico
 */
public class TestPoiSheet extends GroovyTestCase {
   
   public void testCreateModel() {
      def sourceFile = DKResourceUtil.findResourceAsFile('xcel_test.xls', this)
      println "sourceFile->$sourceFile"
      assert sourceFile
      DKPoiSheet poiSheet = [sourceFile, "easy sheet", false, false, false]
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
      
      poiSheet = [sourceFile, 'Sheet2', false, false, false]
      internalSheet = poiSheet.getSheet()
      println "internalSheet->$internalSheet"
      assert internalSheet
      assert internalSheet.sheetName == 'Sheet2'
      
      poiSheet = [sourceFile, 'does_not_exist', false, false, false]
      shouldFail(IOException) { 
         internalSheet = poiSheet.getSheet()
      }
   }
}
