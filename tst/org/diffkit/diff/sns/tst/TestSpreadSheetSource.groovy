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



import org.diffkit.diff.engine.DKColumnModel;
import org.diffkit.diff.sns.DKSpreadSheetFileSource;
import org.diffkit.util.DKResourceUtil

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestSpreadSheetSource extends GroovyTestCase {
   
   public void testModelColumns() {
      def sourceFile = DKResourceUtil.findResourceAsFile('type_test.xls', this)
      println "sourceFile->$sourceFile"
      assert sourceFile
      assert sourceFile.canRead()
      
      def source = new DKSpreadSheetFileSource(sourceFile.absolutePath, "Sheet1", null, null)
      def model = source.model
      assert model
      def columns = model.columns
      assert columns
      assert columns.size() == 8
      assert columns.collect { it.name } == ['COLUMN1', 'COLUMN2', 'COLUMN3', 'COLUMN4', 'COLUMN5', 'COLUMN6', 'COLUMN7', 'COLUMN8']
      
      // Xcel number
      assert columns[0].index == 0
      assert columns[0].type == DKColumnModel.Type.DECIMAL
      // Xcel text
      assert columns[1].index == 1
      assert columns[1].type == DKColumnModel.Type.STRING
      // Xcel percent
      assert columns[2].index == 2
      assert columns[2].type == DKColumnModel.Type.DECIMAL
      // Xcel currency
      assert columns[3].index == 3
      assert columns[3].type == DKColumnModel.Type.DECIMAL
      // Xcel date
      assert columns[4].index == 4
      assert columns[4].type == DKColumnModel.Type.DATE
      // Xcel fraction
      assert columns[5].index == 5
      assert columns[5].type == DKColumnModel.Type.REAL
      // Xcel boolean value
      assert columns[6].index == 6
      assert columns[6].type == DKColumnModel.Type.BOOLEAN
   }
   
   public void testKey() {
      def sourceFile = DKResourceUtil.findResourceAsFile('type_test.xls', this)
      println "sourceFile->$sourceFile"
      assert sourceFile
      assert sourceFile.canRead()
      
      def source = new DKSpreadSheetFileSource(sourceFile.absolutePath, "Sheet1", null, null)
      def model = source.model
      assert model
      def key = model.key
      assert key
      assert key == (int[])[0]
   }
   
   public void testModelName() {
      def sourceFile = DKResourceUtil.findResourceAsFile('type_test.xls', this)
      println "sourceFile->$sourceFile"
      assert sourceFile
      assert sourceFile.canRead()
      
      def source = new DKSpreadSheetFileSource(sourceFile.absolutePath, "Sheet1", null, null)
      def model = source.model
      assert model
      assert model.name  == 'Sheet1'
   }
}
