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
package org.diffkit.diff.engine.tst

import java.io.File;

import org.apache.commons.lang.ClassUtils;

import groovy.util.GroovyTestCase

import org.diffkit.common.DKComparatorChain;
import org.diffkit.common.DKMapKeyValueComparator;
import org.diffkit.db.DKDBColumn 
import org.diffkit.db.DKDBConnectionInfo 
import org.diffkit.db.DKDBDatabase 
import org.diffkit.db.DKDBFlavor 
import org.diffkit.db.DKDBH2Loader 
import org.diffkit.db.DKDBPrimaryKey 
import org.diffkit.db.DKDBTable;
import org.diffkit.db.DKDBTableDataAccess 
import org.diffkit.db.DKDBTableLoader 
import org.diffkit.diff.diffor.DKEqualsDiffor;
import org.diffkit.diff.engine.DKColumnComparison 
import org.diffkit.diff.engine.DKColumnDiff 
import org.diffkit.diff.engine.DKColumnModel 
import org.diffkit.diff.engine.DKContext 
import org.diffkit.diff.engine.DKDiff;
import org.diffkit.diff.engine.DKDiffEngine 
import org.diffkit.diff.engine.DKRowDiff 
import org.diffkit.diff.engine.DKSide;
import org.diffkit.diff.engine.DKSource 
import org.diffkit.diff.engine.DKStandardTableComparison 
import org.diffkit.diff.engine.DKTableModel 
import org.diffkit.diff.sns.DKDBSink 
import org.diffkit.diff.sns.DKDBSource 
import org.diffkit.diff.sns.DKFileSink 
import org.diffkit.diff.sns.DKFileSource 
import org.diffkit.diff.sns.DKListSource
import org.diffkit.diff.sns.DKListSink
import org.diffkit.diff.sns.DKTableModelUtil;
import org.diffkit.util.DKFileUtil;
import org.diffkit.util.DKResourceUtil;
import org.diffkit.util.DKStringUtil;


/**
 * @author jpanico
 */
public class TestEngine extends GroovyTestCase {
   
   /**
    * both sides use the same TableModel, but there are diffs of each sort; uses DBSources
    */
   public void testSameModelFromDBToDB(){
      def database = this.getDatabase()
      def connection = database.connection
      def lhsName = 'lhs2'
      def rhsName = 'rhs2'
      def lhsDBTable = this.createSimpleDBTableModel(lhsName)
      def rhsDBTable = this.createSimpleDBTableModel(rhsName)
      
      assert database.createTable( lhsDBTable)
      assert database.createTable( rhsDBTable)
      
      DKDBTableDataAccess tableDataAccess = [database]
      DKDBH2Loader loader = [database]
      
      lhsDBTable = tableDataAccess.getTable(lhsName.toUpperCase())
      rhsDBTable = tableDataAccess.getTable(rhsName.toUpperCase())
      assert lhsDBTable
      assert rhsDBTable
      
      assert this.load(lhsName, lhsDBTable, loader)
      assert this.load(rhsName, rhsDBTable, loader)
      
      def lhsSource = this.createDBSource(lhsDBTable, database)
      def rhsSource = this.createDBSource(rhsDBTable, database)
      def tableComparison = this.createComparison2( lhsDBTable, rhsDBTable)
      DKDBSink sink = new DKDBSink(database)
      DKDiffEngine engine = new DKDiffEngine()
      
      engine.diff(lhsSource, rhsSource, sink, tableComparison)
      
      assert lhsSource.lastIndex == 5
      assert rhsSource.lastIndex == 5
      assert database.dropTable( lhsDBTable)
      assert database.dropTable( rhsDBTable)
      
      def diffContextTable = sink.diffContextTable
      def diffTable = sink.diffTable
      assert diffContextTable
      assert diffTable
      def contexts = database.readAllRows( diffContextTable)
      assert contexts
      assert contexts.size() == 1
      println "context->${contexts[0]}"
      def diffs =  database.readAllRows( diffTable)
      assert diffs
      assert diffs.size() == 4
      def rowStepComparator = new DKMapKeyValueComparator("ROW_STEP")
      def columnStepComparator = new DKMapKeyValueComparator("COLUMN_STEP")
      def comparatorChain = new DKComparatorChain(rowStepComparator, columnStepComparator)
      Collections.sort( diffs, comparatorChain)
      println "diffs[0]->${diffs[0]}"
      assert diffs[0]["CONTEXT_ID"] == contexts[0]["ID"]
      assert diffs[0]["KIND"] == DKDiff.Kind.COLUMN_DIFF.ordinal()
      assert diffs[0]["ROW_STEP"] == 2
      assert diffs[0]["COLUMN_STEP"] == 1
      assert diffs[0]["LHS"] == '1111'
      assert diffs[0]["RHS"] == 'xxxx'
      
      println "diffs[3]->${diffs[3]}"
      assert diffs[3]["CONTEXT_ID"] == contexts[0]["ID"]
      assert diffs[3]["KIND"] == DKDiff.Kind.COLUMN_DIFF.ordinal()
      assert diffs[3]["ROW_STEP"] == 6
      assert diffs[3]["COLUMN_STEP"] == 1
      assert diffs[3]["LHS"] == '6666'
      assert diffs[3]["RHS"] == 'xxxx'
      
      assert database.dropTable( diffContextTable)
      assert database.dropTable( diffTable)
   }
   
   /**
    * both sides use the same TableModel, but there are diffs of each sort; uses DBSources
    */
   public void testSameModelFromDB(){
      def database = this.getDatabase()
      def connection = database.connection
      def lhsName = 'lhs2'
      def rhsName = 'rhs2'
      def lhsDBTable = this.createSimpleDBTableModel(lhsName)
      def rhsDBTable = this.createSimpleDBTableModel(rhsName)
      
      assert database.createTable( lhsDBTable )
      assert database.createTable( rhsDBTable)
      
      DKDBTableDataAccess tableDataAccess = [database]
      DKDBH2Loader loader = [database]
      
      lhsDBTable = tableDataAccess.getTable(lhsName.toUpperCase())
      rhsDBTable = tableDataAccess.getTable(rhsName.toUpperCase())
      assert lhsDBTable
      assert rhsDBTable
      
      assert this.load(lhsName, lhsDBTable, loader)
      assert this.load(rhsName, rhsDBTable, loader)
      
      def lhsSource = this.createDBSource(lhsDBTable, database)
      def rhsSource = this.createDBSource(rhsDBTable, database)
      def tableComparison = this.createComparison2( lhsDBTable, rhsDBTable)
      def diffFileName = 'testSameModelFromDB.diff'
      DKFileSink sink = this.createSink2( diffFileName)
      DKDiffEngine engine = new DKDiffEngine()
      
      engine.diff(lhsSource, rhsSource, sink, tableComparison)
      
      assert lhsSource.lastIndex == 5
      assert rhsSource.lastIndex == 5
      assert database.dropTable( lhsDBTable)
      assert database.dropTable( rhsDBTable)
      
      def expectedFile = this.getExpectedFile(diffFileName)
      def actualFile = sink.file
      assert expectedFile
      assert actualFile
      
      assert DKFileUtil.readFullyAsString(expectedFile) == DKFileUtil.readFullyAsString(actualFile)
   }
   
   private File getExpectedFile(String filename_){
      String expectedFilePath = ClassUtils.getPackageName(this.getClass()) 
      expectedFilePath = DKStringUtil.packageNameToResourcePath(expectedFilePath) + filename_
      return  DKResourceUtil.findResourceAsFile(expectedFilePath)
   }
   
   private DKFileSink createSink2(String sinkFileName_){
      File sinkFile = ['./'+sinkFileName_]
      if(sinkFile.exists())
         sinkFile.delete()
      return new DKFileSink(sinkFile, false)
   }
   
   private DKStandardTableComparison createComparison2(DKDBTable lhsDBTable_, DKDBTable rhsDBTable_) {
      DKTableModel lhsTableModel = DKTableModelUtil.createDefaultTableModel(lhsDBTable_, null)
      DKTableModel rhsTableModel = DKTableModelUtil.createDefaultTableModel(rhsDBTable_, null)
      
      DKColumnComparison[] map = DKColumnComparison.createColumnPlans( lhsTableModel, rhsTableModel, (int[]) [1,2], DKEqualsDiffor.instance)
      //    println "map->$map"
      DKStandardTableComparison comparison = new DKStandardTableComparison(lhsTableModel, rhsTableModel, DKDiff.Kind.BOTH, map, (int[])[0,1], (int[][])[[0],[0]], (long)100)
      //    println "comparison->$comparison"
      return comparison
   }
   
   private DKDBSource createDBSource(DKDBTable table_, DKDBDatabase database_) {
      def tableModel = DKTableModelUtil.createDefaultTableModel(table_, null)
      assert tableModel
      
      return new DKDBSource(table_.tableName, null, database_, tableModel, null, null)
   }
   
   private boolean load(String name_, DKDBTable table_, DKDBTableLoader loader_){
      def csvFile = this.getCsvFile(name_)
      return loader_.load(table_, csvFile)
   }
   
   private DKDBDatabase getDatabase(){
      DKDBConnectionInfo connectionInfo = ['test', DKDBFlavor.H2,"mem:test", null, null, 'test', 'test']
      println "connectionInfo->$connectionInfo"
      return  new DKDBDatabase(connectionInfo)
   }
   
   private File getCsvFile(String name_){
      def csvFile = DKResourceUtil.findResourceAsFile(String.format('org/diffkit/diff/engine/tst/%s.csv',name_))
      println "csvFile->$csvFile"
      assert csvFile
      return csvFile
   }
   
   private DKDBTable createSimpleDBTableModel(String tablename_){
      DKDBColumn column1 = ['column1', 1, 'VARCHAR', 20, true]
      DKDBColumn column2 = ['column2', 2, 'VARCHAR', -1, true]
      DKDBColumn column3 = ['column3', 2, 'VARCHAR', -1, true]
      DKDBColumn[] columns = [column1, column2, column3]
      String[] pkColNames = ['column1']
      DKDBPrimaryKey pk = ['pk_' + tablename_, pkColNames]
      DKDBTable table = [null, null, tablename_, columns, pk]
      return table
   }
   
   /**
    * both sides use the same TableModel, but there are diffs of each sort; uses FileSources
    */
   public void testSameModelFromFile(){
      
      def lhsFile = this.getTestFile('lhs1.csv')
      println "lhsFile->$lhsFile"
      assert lhsFile
      
      def rhsFile = this.getTestFile('rhs1.csv')
      println "rhsFile->$rhsFile"
      assert rhsFile
      
      DKTableModel tableModel = this.createSimpleTableModel()
      DKFileSource lhsSource = new DKFileSource(lhsFile.absolutePath,  tableModel, null, null,'\\,', true, true)
      DKFileSource rhsSource = new DKFileSource(rhsFile.absolutePath, tableModel, null, null,'\\,',  true, true)
      def filename = 'testSameModelFromFile.diff'
      File sinkFile = ['./'+filename]
      if(sinkFile.exists())
         sinkFile.delete()
      DKFileSink sink = [sinkFile, false]
      DKStandardTableComparison tableComparison = this.createSimpleComparison()
      println "tableComparison->$tableComparison"
      DKDiffEngine engine = new DKDiffEngine()
      
      engine.diff(lhsSource, rhsSource, sink, tableComparison)
      
      assert sink.diffCount == 6
      
      String expectedFilePath = ClassUtils.getPackageName(this.getClass()) 
      expectedFilePath = DKStringUtil.packageNameToResourcePath(expectedFilePath) + filename
      def expectedFile = DKResourceUtil.findResourceAsFile(expectedFilePath)
      
      String expected = DKFileUtil.readFullyAsString( expectedFile)
      assert expected
      String actual = DKFileUtil.readFullyAsString( sinkFile)
      assert actual
      
      assert expected == actual
   }
   
   private File getTestFile(String filename_){
      String sourceFilePath = ClassUtils.getPackageName(this.getClass()) 
      sourceFilePath = DKStringUtil.packageNameToResourcePath(sourceFilePath) + filename_
      return DKResourceUtil.findResourceAsFile(sourceFilePath)
   }
   
   public void testOneSided(){
      DKTableModel tableModel = this.createSimpleTableModel()
      println "tableModel->$tableModel"
      Object[] l1 = ['1111', '1111', 1]
      Object[] l2 = ['1111', '1111', 2]
      Object[] l3 = ['4444', '4444', 1]
      Object[] l4 = ['4444', '4444', 2]
      Object[] l5 = ['6666', '6666', 1]
      Object[] l6 = ['6666', '6666', 2]
      def rows= [l1,l2,l3,l4, l5, l6]
      DKListSource lSource = [tableModel, rows]
      println "lSource->$lSource"
      
      DKListSource rSource = [tableModel, []]
      println "rSource->$rSource"
      DKStandardTableComparison tableComparison = this.createSimpleComparison()
      println "tableComparison->$tableComparison"
      DKDiffEngine engine = new DKDiffEngine()
      DKListSink sink = new DKListSink()
      engine.diff(lSource, rSource, sink, tableComparison)
      
      assert sink.diffCount == 6
      for(diff in sink.diffs) {
         assert diff.kind == DKDiff.Kind.ROW_DIFF
         assert diff.side == DKSide.RIGHT
      }
      
      lSource = [tableModel, []]
      println "lSource->$lSource"
      rSource = [tableModel, rows]
      println "rSource->$rSource"
      sink = new DKListSink()
      engine.diff(lSource, rSource, sink, tableComparison)
      
      assert sink.diffCount == 6
      for(diff in sink.diffs) {
         assert diff.kind == DKDiff.Kind.ROW_DIFF
         assert diff.side == DKSide.LEFT
      }
   }
   
   /**
    * both sides use the same TableModel, but there are diffs of each sort
    */
   public void testSameModelSimpleDiffs(){
      DKTableModel tableModel = this.createSimpleTableModel()
      println "tableModel->$tableModel"
      Object[] l1 = ['1111', '1111', 1]
      Object[] l2 = ['1111', '1111', 2]
      Object[] l3 = ['4444', '4444', 1]
      Object[] l4 = ['4444', '4444', 2]
      Object[] l5 = ['6666', '6666', 1]
      Object[] l6 = ['6666', '6666', 2]
      def lRows= [l1,l2,l3,l4, l5, l6]
      DKListSource lSource = [tableModel, lRows]
      println "lSource->$lSource"
      
      Object[] r1 = ['1111', '1111', 1]
      Object[] r2 = ['1111', 'xxxx', 2]
      Object[] r3 = ['2222', '2222', 1]
      Object[] r4 = ['5555', '5555', 2]
      Object[] r5 = ['6666', 'xxxx', 1]
      Object[] r6 = ['6666', '6666', 2]
      def rRows= [r1,r2,r3,r4, r5, r6]
      DKListSource rSource = [tableModel, rRows]
      println "rSource->$rSource"
      DKStandardTableComparison tableComparison = this.createSimpleComparison()
      println "tableComparison->$tableComparison"
      DKDiffEngine engine = new DKDiffEngine()
      DKListSink sink = []
      engine.diff(lSource, rSource, sink, tableComparison)
      
      assert sink.diffCount == 6
      
      assert sink.diffs[0] instanceof DKColumnDiff
      assert sink.diffs[0].kind == DKDiff.Kind.COLUMN_DIFF
      assert sink.diffs[0].rowStep == 2
      assert sink.diffs[0].columnStep == 1
      assert sink.diffs[0].rowDisplayValues == [column2:'1111:xxxx', column3:'2']
      assert sink.diffs[0].lhs == '1111'
      assert sink.diffs[0].rhs == 'xxxx'
      
      
      assert sink.diffs[1] instanceof DKRowDiff
      assert sink.diffs[1].kind == DKDiff.Kind.ROW_DIFF
      assert sink.diffs[1].rowStep == 3
      assert sink.diffs[1].side == DKSide.LEFT
      assert sink.diffs[1].rowKeyValues == ['2222',1]
      assert sink.diffs[1].rowDisplayValues == [column2:'2222', column3:'1']
      
      assert sink.diffs[2] instanceof DKRowDiff
      assert sink.diffs[2].kind == DKDiff.Kind.ROW_DIFF
      assert sink.diffs[2].rowStep == 4
      assert sink.diffs[2].side == DKSide.RIGHT
      assert sink.diffs[2].rowKeyValues == ['4444',1]
      assert sink.diffs[2].rowDisplayValues == [column2:'4444', column3:'1']
      
      assert sink.diffs[3] instanceof DKRowDiff
      assert sink.diffs[3].kind == DKDiff.Kind.ROW_DIFF
      assert sink.diffs[3].rowStep == 5
      assert sink.diffs[3].side == DKSide.RIGHT
      assert sink.diffs[3].rowKeyValues == ['4444',2]
      assert sink.diffs[3].rowDisplayValues == [column2:'4444', column3:'2']
      
      assert sink.diffs[4] instanceof DKRowDiff
      assert sink.diffs[4].kind == DKDiff.Kind.ROW_DIFF
      assert sink.diffs[4].rowStep == 6
      assert sink.diffs[4].side == DKSide.LEFT
      assert sink.diffs[4].rowKeyValues == ['5555',2]
      assert sink.diffs[4].rowDisplayValues == [column2:'5555', column3:'2']
      
      assert sink.diffs[5] instanceof DKColumnDiff
      assert sink.diffs[5].kind == DKDiff.Kind.COLUMN_DIFF
      assert sink.diffs[5].rowStep == 7
      assert sink.diffs[5].columnStep == 1
      assert sink.diffs[5].rowDisplayValues == [column2:'6666:xxxx', column3:'1']
      assert sink.diffs[5].lhs == '6666'
      assert sink.diffs[5].rhs == 'xxxx'
   }
   
   /**
    * both sides are identical, so there should be no diffs
    */
   public void testIdenticalSources(){
      DKDiffEngine engine = new DKDiffEngine()
      DKSource lhSource = this.createSimpleSource()
      DKSource rhSource = this.createSimpleSource()
      DKStandardTableComparison tableComparison = this.createSimpleComparison()
      DKListSink sink = []
      println "tableComparison->$tableComparison"
      
      engine.diff(lhSource, rhSource, sink, tableComparison)
      
      assert sink.diffCount == 0
   }
   
   public void testDiffRow(){
      DKDiffEngine engine = new DKDiffEngine()
      DKContext context = this.createSimpleContext()
      println "tableComparison->$context.tableComparison.description"
      
      DKSource rhSource = context.rhs
      Object[] rhRow = rhSource.getNextRow()
      def sink = context.sink
      
      assert sink.getDiffCount() == 0
      sink.open();
      
      engine.diffRow( rhRow, rhRow, context, sink)
      assert sink.getDiffCount() == 0
      
      DKSource lhSource = context.lhs
      lhSource.getNextRow()		
      Object[] lhRow = lhSource.getNextRow()
      
      engine.diffRow( lhRow, rhRow, context, sink)
      assert sink.getDiffCount() == 1
      def diff = sink.diffs[0]
      assert diff
      assert diff instanceof DKColumnDiff
      assert diff.kind == DKDiff.Kind.COLUMN_DIFF
      assert diff.lhs == 'zzzz'
      assert diff.rhs == 'aaaa'
   }
   
   public void testRecordRowDiff(){
      DKDiffEngine engine = new DKDiffEngine()
      DKContext context = this.createSimpleContext()
      DKSource rhSource = context.rhs
      Object[] row = rhSource.getNextRow()
      
      def sink = context.sink
      assert sink.getDiffCount() == 0
      sink.open();
      engine.recordRowDiff( row, DKSide.LEFT_INDEX, context, sink)
      assert sink.getDiffCount() == 1
      def diff = sink.diffs[0]
      assert diff
      assert diff instanceof DKRowDiff
      assert diff.rowStep == 0
      assert diff.rowKeyValues == ['zzzz', 1]
      assert diff.rowDisplayValues == [column2:'aaaa',column3:'1']
      assert diff.kind == DKDiff.Kind.ROW_DIFF
      assert diff.side == DKSide.LEFT
   }
   
   
   private DKContext createSimpleContext() {
      return new DKContext(this.createSimpleSource(), this.createSimpleSource(), new DKListSink(),this.createSimpleComparison())
   }
   
   private DKStandardTableComparison createSimpleComparison() {
      DKTableModel tableModel = this.createSimpleTableModel();
      DKColumnComparison[] map = DKColumnComparison.createColumnPlans( tableModel, tableModel, (int[]) [1], DKEqualsDiffor.instance)
      //		println "map->$map"
      DKStandardTableComparison comparison = new DKStandardTableComparison(tableModel, tableModel, DKDiff.Kind.BOTH, map, (int[])[0], (int[][])[[1,2],[1,2]], (long)100)
      //		println "comparison->$comparison"
      return comparison
   }
   
   private DKListSource createSimpleSource(){
      return new DKListSource(this.createSimpleTableModel(), this.createSimpleRows())
   }
   
   private List<Object[]> createSimpleRows(){
      Object[] l1 = ['zzzz', 'aaaa', 1]
      Object[] l2 = ['zzzz', 'zzzz', 2]
      Object[] l3 = ['bbbb', 'zzzz', 1]
      Object[] l4 = ['aaaa', 'bbbb', 2]
      
      return [l1,l2,l3,l4]
   }
   
   private DKTableModel createSimpleTableModel(){
      DKColumnModel column1 = [0, 'column1', DKColumnModel.Type.STRING]
      DKColumnModel column2 = [1, 'column2', DKColumnModel.Type.STRING]
      DKColumnModel column3 = [2, 'column3', DKColumnModel.Type.NUMBER]
      DKColumnModel[] columns = [column1, column2, column3]
      int[] key = [0,2]
      
      return new DKTableModel(columns, key)
   }
}
