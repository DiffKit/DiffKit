
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
package org.diffkit.diff.conf.tst


import org.apache.commons.io.FilenameUtils;
import org.diffkit.db.DKDBConnectionInfo 
import org.diffkit.db.tst.DBTestSetup;
import org.diffkit.diff.conf.DKMagicPlan 
import org.diffkit.diff.conf.DKMagicPlanBuilder 
import org.diffkit.diff.diffor.DKEqualsDiffor 
import org.diffkit.diff.engine.DKSourceSink;
import org.diffkit.diff.sns.DKDBSource 
import org.diffkit.diff.sns.DKFileSource 
import org.diffkit.util.DKResourceUtil;



/**
 * @author jpanico
 */
public class TestMagicPlanBuilder extends GroovyTestCase {
   
   /**
    * test that including two mutually exclusive properties, properties that trigger 
    * mutually exclusive rules, results in Exception
    */
   public void testMagicExclusion() {
      def lhsFileResourcePath = 'org/diffkit/diff/conf/tst/test.lhs.csv'
      def rhsFileResourcePath = 'org/diffkit/diff/conf/tst/test.rhs.csv'
      def lhsFile = DKResourceUtil.findResourceAsFile(lhsFileResourcePath)
      def rhsFile = DKResourceUtil.findResourceAsFile(rhsFileResourcePath)
      
      DKMagicPlan magicPlan = []
      magicPlan.lhsFilePath =lhsFile.absolutePath
      magicPlan.rhsFilePath = rhsFile.absolutePath
      magicPlan.lhsDBTableName = 'LHS_TABLE'
      magicPlan.rhsDBTableName = 'RHS_TABLE'
      
      DKMagicPlanBuilder builder = [magicPlan]
      shouldFail(RuntimeException) {
         def builtPlan = builder.build()
         assert builtPlan
      }
   }
   
   public void testFullyMagicFileBuild(){
      def lhsFileResourcePath = 'org/diffkit/diff/conf/tst/test.lhs.csv'
      def rhsFileResourcePath = 'org/diffkit/diff/conf/tst/test.rhs.csv'
      DKMagicPlan magicPlan = []
      def lhsFile = DKResourceUtil.findResourceAsFile(lhsFileResourcePath)
      def rhsFile = DKResourceUtil.findResourceAsFile(rhsFileResourcePath)
      magicPlan.lhsFilePath =lhsFile.absolutePath
      magicPlan.rhsFilePath = rhsFile.absolutePath
      magicPlan.delimiter = '\\,'
      
      DKMagicPlanBuilder builder = [magicPlan]
      def builtPlan = builder.build()
      assert builtPlan
      
      def lhsSource = builtPlan.lhsSource
      assert lhsSource
      assert lhsSource instanceof DKFileSource
      def lhsModel = lhsSource.model
      assert lhsModel
      assert lhsModel.columns
      assert lhsModel.columns.length ==3
      assert lhsModel.columns[0].name == 'column1'
      def lhsSourceFile = lhsSource.file
      assert lhsSourceFile
      assert lhsSourceFile.exists()
      def normalizedLhsSourceFilePath = FilenameUtils.normalize(lhsSourceFile.path)
      def normalizedLhsFileResourcePath = FilenameUtils.normalize(lhsFileResourcePath)
      assert normalizedLhsSourceFilePath.endsWith(normalizedLhsFileResourcePath)
      
      def tableComparison = builtPlan.tableComparison
      assert tableComparison
      assert tableComparison.rhsModel == builtPlan.rhsSource.model
      def comparisonMap = tableComparison.map
      assert comparisonMap
      assert comparisonMap.length == 3
      for ( i in 0..2 ) {
         comparisonMap[i]._lhsColumn.name == comparisonMap[i]._rhsColumn.name
         comparisonMap[i]._diffor instanceof DKEqualsDiffor
      }
      assert tableComparison.diffIndexes == [1,2]
      assert tableComparison.displayIndexes == [[0],[0]]
   }
   
   public void testFullyMagicDBBuild(){
      DBTestSetup.setupDB(new File('org/diffkit/diff/conf/tst/test.dbsetup.xml'), (File[])[new File('org/diffkit/diff/conf/tst/dbConnectionInfo.xml')], 'org/diffkit/diff/conf/tst/test.lhs.csv', 'org/diffkit/diff/conf/tst/test.rhs.csv')
      DKDBConnectionInfo dbConnectionInfo = ['test', DKDBConnectionInfo.Kind.H2, 'mem:conf.test;DB_CLOSE_DELAY=-1', null, null, 'test', 'test']
      
      DKMagicPlan magicPlan = []
      magicPlan.lhsDBTableName = 'LHS_TABLE'
      magicPlan.rhsDBTableName = 'RHS_TABLE'
      magicPlan.dbConnectionInfo = dbConnectionInfo
      
      DKMagicPlanBuilder builder = [magicPlan]
      def builtPlan = builder.build()
      assert builtPlan
      
      def tableComparison = builtPlan.tableComparison
      assert tableComparison
      assert tableComparison.lhsModel == builtPlan.lhsSource.model
      assert tableComparison.rhsModel == builtPlan.rhsSource.model
      def comparisonMap = tableComparison.map
      assert comparisonMap
      assert comparisonMap.length == 3
      for ( i in 0..2 ) {
         comparisonMap[i]._lhsColumn.name == comparisonMap[i]._rhsColumn.name
         comparisonMap[i]._diffor instanceof DKEqualsDiffor
      }
      assert tableComparison.diffIndexes == [1]
      assert tableComparison.displayIndexes == [[0,2],[0,2]]
      
      def lhsSource = builtPlan.lhsSource
      assert lhsSource
      assert lhsSource instanceof DKDBSource
      def lhsDBTable = lhsSource.table
      assert lhsDBTable
      assert lhsDBTable.tableName == 'LHS_TABLE'
      assert lhsDBTable.columns
      assert lhsDBTable.columns.length == 3
      assert lhsDBTable.columns[0].name == 'COLUMN1'
      assert lhsDBTable.columns[0].dataTypeName == 'VARCHAR'
      
      def rhsSource = builtPlan.rhsSource
      assert rhsSource
      assert rhsSource instanceof DKDBSource
      def rhsDBTable = rhsSource.table
      assert rhsDBTable
      assert rhsDBTable.tableName == 'RHS_TABLE'
      def rhsModel = rhsSource.model
      assert rhsModel
      assert rhsModel.name == 'PUBLIC.RHS_TABLE'
      assert rhsModel.columns
      assert rhsModel.columns.length ==3
      assert rhsModel.columns[0].name == 'COLUMN1'
      
      def sink = builtPlan.sink
      assert sink
      assert sink.kind == DKSourceSink.Kind.STREAM
   }
}

