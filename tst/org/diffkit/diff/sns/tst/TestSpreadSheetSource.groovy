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
import org.diffkit.diff.engine.DKColumnModel.Type 
import org.diffkit.diff.sns.DKSpreadSheetFileSource 
import org.diffkit.util.DKResourceUtil 


/**
 * @author jpanico
 */
public class TestSpreadSheetSource extends GroovyTestCase {
   
   public void testModelColumns() {
      def sourceFile = DKResourceUtil.findResourceAsFile('xcel_test.xls', this)
      println "sourceFile->$sourceFile"
      assert sourceFile
      assert sourceFile.canRead()
      
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
      
      source = new DKSpreadSheetFileSource(sourceFile.absolutePath, null, null, null, null, true, true, false)
      model = source.model
      assert model
      assert model.name == 'Sheet1'
      
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
   }
   
   public void testKey() {
      //      def sourceFile = DKResourceUtil.findResourceAsFile('type_test.xls', this)
      //      println "sourceFile->$sourceFile"
      //      assert sourceFile
      //      assert sourceFile.canRead()
      //      
      //      def source = new DKSpreadSheetFileSource(sourceFile.absolutePath, "Sheet1", null, null)
      //      def model = source.model
      //      assert model
      //      def key = model.key
      //      assert key
      //      assert key == (int[])[0]
   }
   
   public void testModelName() {
      //      def sourceFile = DKResourceUtil.findResourceAsFile('type_test.xls', this)
      //      println "sourceFile->$sourceFile"
      //      assert sourceFile
      //      assert sourceFile.canRead()
      //      
      //      def source = new DKSpreadSheetFileSource(sourceFile.absolutePath, "Sheet1", null, null)
      //      def model = source.model
      //      assert model
      //      assert model.name  == 'Sheet1'
   }
}
