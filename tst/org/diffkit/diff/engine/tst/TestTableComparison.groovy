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



import org.apache.commons.collections.comparators.ComparatorChain 

import org.diffkit.diff.diffor.DKEqualsDiffor;
import org.diffkit.diff.engine.DKColumnComparison 
import org.diffkit.diff.engine.DKColumnModel
import org.diffkit.diff.engine.DKDiff;
import org.diffkit.diff.engine.DKSide;
import org.diffkit.diff.engine.DKStandardTableComparison 
import org.diffkit.diff.engine.DKTableModel 

import groovy.util.GroovyTestCase


/**
 * @author jpanico
 */
public class TestTableComparison extends GroovyTestCase {
	
	public void testRowDisplayValues(){
		DKColumnModel column1_1 = [0, 'column1', DKColumnModel.Type.STRING]
		DKColumnModel column1_2 = [1, 'column2', DKColumnModel.Type.STRING]
		DKColumnModel column1_3 = [2, 'column3', DKColumnModel.Type.INTEGER]
		DKColumnModel[] columns = [column1_1, column1_2, column1_3]
		int[] key = [0,2]
		DKTableModel lhsTable = ["lhs_table",columns, key]
		
		DKColumnModel column2_1 = [0, 'column1', DKColumnModel.Type.STRING]
		DKColumnModel column2_2 = [1, 'column2', DKColumnModel.Type.INTEGER]
		DKColumnModel column2_3 = [2, 'column3', DKColumnModel.Type.INTEGER]
		columns = [column2_1, column2_2, column2_3]
		key = [0,2]
		DKTableModel rhsTable = ["rhs_table",columns, key]
		
		DKColumnComparison[] map = DKColumnComparison.createColumnPlans(lhsTable, rhsTable, (int[])[1], DKEqualsDiffor.instance)
		int[][] displayIndexes = [(int[])[0,2],(int[])[0,2]]
		DKStandardTableComparison plan = new DKStandardTableComparison(lhsTable, rhsTable, DKDiff.Kind.BOTH, map, (int[])[1], displayIndexes, 100)
		
		def rowDisplayValues = plan.getRowDisplayValues((Object[])['zzzz', 'zzzz', 1], (Object[])['zzzz', 'zzzz', 1])
		//		println "rowDisplayValues->$rowDisplayValues"
		assert rowDisplayValues == [column1:'zzzz', column3:'1']
		
		rowDisplayValues = plan.getRowDisplayValues((Object[])['zzzz', 'zzzz', 1], (Object[])['aaaa', 'zzzz', 1])
		assert rowDisplayValues == [column1:'zzzz:aaaa', column3:'1']
		
		rowDisplayValues = plan.getRowDisplayValues((Object[])[null, 'zzzz', 1], (Object[])['aaaa', 'zzzz', 1])
		assert rowDisplayValues == [column1:'<null>:aaaa', column3:'1']
		
		displayIndexes = [(int[])[0,2],(int[])[1]]
		plan = new DKStandardTableComparison(lhsTable, rhsTable, DKDiff.Kind.BOTH, map, (int[])[1], displayIndexes, 100)
		rowDisplayValues = plan.getRowDisplayValues((Object[])['zzzz', 'zzzz', 1], (Object[])['zzzz', 'cccc', 1])
		assert rowDisplayValues == [column1:'zzzz:', column2:':cccc', column3:'1:']
		
		column1_1 = [0, 'column1_1', DKColumnModel.Type.STRING]
		column1_2 = [1, 'column1_2', DKColumnModel.Type.STRING]
		column1_3 = [2, 'column1_3', DKColumnModel.Type.INTEGER]
		columns = [column1_1, column1_2, column1_3]
		key = [0,2]
		lhsTable = ["lhs_table",columns, key]
		
		column2_1 = [0, 'column2_1', DKColumnModel.Type.STRING]
		column2_2 = [1, 'column2_2', DKColumnModel.Type.INTEGER]
		column2_3 = [2, 'column2_3', DKColumnModel.Type.INTEGER]
		columns = [column2_1, column2_2, column2_3]
		key = [0,2]
		rhsTable = ["rhs_table",columns, key]
		
		displayIndexes = [(int[])[0,2],(int[])[0,2]]
		map =DKColumnComparison.createColumnPlans(lhsTable, rhsTable, (int[])[1], DKEqualsDiffor.instance)
		plan = new DKStandardTableComparison(lhsTable, rhsTable, DKDiff.Kind.BOTH, map, (int[])[1], displayIndexes, 100)
		rowDisplayValues = plan.getRowDisplayValues((Object[])['zzzz', 'zzzz', 1], (Object[])['zzzz', 'zzzz', 1])
		println "rowDisplayValues->$rowDisplayValues"
		assert rowDisplayValues == [column1_1:'zzzz:', column2_1:':zzzz', column1_3:'1:', column2_3:':1']
		
		column1_1 = [0, 'column1_1', DKColumnModel.Type.STRING]
		column1_2 = [1, 'column1_2', DKColumnModel.Type.STRING]
		column1_3 = [2, 'column3', DKColumnModel.Type.INTEGER]
		columns = [column1_1, column1_2, column1_3]
		key = [0,2]
		lhsTable = ["lhs_table",columns, key]
		
		column2_1 = [0, 'column2_1', DKColumnModel.Type.STRING]
		column2_2 = [1, 'column2_2', DKColumnModel.Type.INTEGER]
		column2_3 = [2, 'column3', DKColumnModel.Type.INTEGER]
		columns = [column2_1, column2_2, column2_3]
		key = [0,2]
		rhsTable = ["rhs_table",columns, key]
		
		displayIndexes = [(int[])[0,2],(int[])[0,2]]
		map =DKColumnComparison.createColumnPlans(lhsTable, rhsTable, (int[])[1], DKEqualsDiffor.instance)
		plan = new DKStandardTableComparison(lhsTable, rhsTable, DKDiff.Kind.BOTH, map, (int[])[1], displayIndexes, 100)
		rowDisplayValues = plan.getRowDisplayValues((Object[])['zzzz', 'zzzz', 1], (Object[])['zzzz', 'zzzz', 1])
		assert rowDisplayValues == [column1_1:'zzzz:', column2_1:':zzzz', column3:'1']
	}
	
	public void testSimpleRowDisplayValues() {
		DKColumnModel column1_1 = [0, 'column1', DKColumnModel.Type.STRING]
		DKColumnModel column1_2 = [1, 'column2', DKColumnModel.Type.STRING]
		DKColumnModel column1_3 = [2, 'column3', DKColumnModel.Type.INTEGER]
		DKColumnModel[] columns = [column1_1, column1_2, column1_3]
		int[] key = [0,2]
		int[][] displayIndexes = [(int[])[0,2],(int[])[0,2]]
		DKTableModel lhsTable = ["lhs_table",columns, key]
		
		DKColumnComparison[] map = DKColumnComparison.createColumnPlans(lhsTable, lhsTable, (int[])[1], DKEqualsDiffor.instance)
		DKStandardTableComparison plan = [lhsTable, lhsTable, DKDiff.Kind.BOTH, map, (int[])[1], displayIndexes, 100]
		def rowDisplayValues = plan.getRowDisplayValues((Object[])['zzzz', 'aaaa', 1], DKSide.LEFT_INDEX)
		//		println "rowDisplayValues->$rowDisplayValues"
		assert rowDisplayValues == [column1:'zzzz', column3:'1']
		rowDisplayValues = plan.getRowDisplayValues((Object[])['zzzz', 'aaaa', null], DKSide.LEFT_INDEX)
		//		println "rowDisplayValues->$rowDisplayValues"
		assert rowDisplayValues == [column1:'zzzz', column3:'<null>']
	}
	
	public void testComparatorSort(){
		DKColumnModel column1_1 = [0, 'column1_1', DKColumnModel.Type.STRING]
		DKColumnModel column1_2 = [1, 'column1_2', DKColumnModel.Type.STRING]
		DKColumnModel column1_3 = [2, 'column1_3', DKColumnModel.Type.INTEGER]
		DKColumnModel[] columns = [column1_1, column1_2, column1_3]
		int[] key = [0,2]
		DKTableModel lhsTable = ["lhs_table",columns, key]
		
		DKColumnModel column2_1 = [0, 'column2_1', DKColumnModel.Type.STRING]
		DKColumnModel column2_2 = [1, 'column2_2', DKColumnModel.Type.INTEGER]
		DKColumnModel column2_3 = [2, 'column2_3', DKColumnModel.Type.INTEGER]
		columns = [column2_1, column2_2, column2_3]
		key = [0,2]
		DKTableModel rhsTable = ["rhs_table",columns, key]
		
		DKColumnComparison[] map = DKColumnComparison.createColumnPlans(lhsTable, rhsTable, (int[])[1], DKEqualsDiffor.instance)
		DKStandardTableComparison plan = new DKStandardTableComparison(lhsTable, rhsTable, DKDiff.Kind.BOTH, map,  (int[])[1], (int[][])null, 100)
		def comparator = plan.rowComparator
		
		Object[] l1 = ['zzzz', 'aaaa', 1]
		Object[] l2 = ['zzzz', 'zzzz', 2]
		Object[] l3 = ['bbbb', 'zzzz', 1]
		Object[] l4 = ['aaaa', 'bbbb', 2]
		def rows = [l1, l2, l3, l4]
		Collections.sort( rows, comparator)
		
		assert rows == [l4,l3,l1,l2]
	}
	
	public void testIdenticalTablesSimpleKeyComparator(){
		DKColumnModel column1 = [0, 'column1', DKColumnModel.Type.STRING]
		DKColumnModel column2 = [1, 'column2', DKColumnModel.Type.STRING]
		DKColumnModel column3 = [2, 'column3', DKColumnModel.Type.INTEGER]
		DKColumnModel column4 = [3, 'column4', DKColumnModel.Type.STRING]
		DKColumnModel[] columns = [column1, column2, column3, column4]
		int[] key = [1]
		DKTableModel tableModel = ["table_model",columns, key]
		DKColumnComparison[] map = DKColumnComparison.createColumnPlans(tableModel,tableModel, (int[])[1], DKEqualsDiffor.instance)
		DKStandardTableComparison plan = [tableModel, tableModel, DKDiff.Kind.BOTH, map, (int[])[1], null, 100]
		def comparator = plan.rowComparator
		assert comparator
		assert comparator instanceof ComparatorChain
		assert comparator.size() == 1
		
		Object[] lhs = ['1111', 'aaaa', 3, 'value4'] 
		Object[] rhs = ['2222', 'bbbb', 3, 'value4'] 
		
		assert comparator.compare(lhs, rhs) <0
		
		key = [2]
		tableModel = ["table_model",columns, key]
		plan = [tableModel, tableModel, DKDiff.Kind.BOTH, map, (int[])[1], null, 100]
		comparator = plan.rowComparator     
		assert comparator.compare(lhs, rhs) ==0
		
		key = [3]
		tableModel = ["table_model",columns, key]
		plan = [tableModel, tableModel, DKDiff.Kind.BOTH, map, (int[])[1], null, 100]
		comparator = plan.rowComparator     
		assert comparator.compare(lhs, rhs) ==0
		
	}
	
	public void testFailures(){
		DKColumnModel column1_1 = [0, 'column1_1', DKColumnModel.Type.STRING]
		DKColumnModel[] columns = [column1_1]
		int[] key = [0]
		DKTableModel lhsTable = ["lhs_table",columns, key]
		
		DKColumnModel column2_1 = [0, 'column2_1', DKColumnModel.Type.STRING]
		DKColumnModel column2_2 = [1, 'column2_2', DKColumnModel.Type.INTEGER]
		DKColumnModel column2_3 = [2, 'column2_3', DKColumnModel.Type.STRING]
		columns = [column2_1, column2_2, column2_3]
		key = [0,2]
		DKTableModel rhsTable = ["rhs_table",columns, key]
		DKColumnComparison[] map = DKColumnComparison.createColumnPlans(lhsTable, rhsTable, (int[])[0], DKEqualsDiffor.instance)
		// key sizes don't match
		shouldFail(RuntimeException) {
			def DKStandardTableComparison plan = new DKStandardTableComparison(lhsTable, rhsTable, DKDiff.Kind.BOTH, map, (int[])[1], null, 100)
		}
		
		DKColumnModel column1_2 = [1, 'column1_2', DKColumnModel.Type.INTEGER]
		DKColumnModel column1_3 = [2, 'column1_3', DKColumnModel.Type.STRING]
		columns = [column1_1, column1_2, column1_3]
		key = [0,1]
		lhsTable = ["lhs_table",columns, key]
		// key type don't match
		shouldFail(RuntimeException) {
			def DKStandardTableComparison plan = new DKStandardTableComparison(lhsTable, rhsTable,  DKDiff.Kind.BOTH, map, (int[])[1], null, 100)
		}
	}
	
	public void testDifferentTablesCompoundKeyComparator(){
		DKColumnModel column1_1 = [0, 'column1_1', DKColumnModel.Type.STRING]
		DKColumnModel column1_2 = [1, 'column1_2', DKColumnModel.Type.STRING]
		DKColumnModel column1_3 = [2, 'column1_3', DKColumnModel.Type.INTEGER]
		DKColumnModel[] columns = [column1_1, column1_2, column1_3]
		int[] key = [0,2]
		DKTableModel lhsTable = ["lhs_table",columns, key]
		
		DKColumnModel column2_1 = [0, 'column2_1', DKColumnModel.Type.STRING]
		DKColumnModel column2_2 = [1, 'column2_2', DKColumnModel.Type.STRING]
		DKColumnModel column2_3 = [2, 'column2_3', DKColumnModel.Type.INTEGER]
		DKColumnModel column2_4 = [3, 'column2_4', DKColumnModel.Type.INTEGER]
		columns = [column2_1, column2_2, column2_3, column2_4]
		key = [1,3]
		DKTableModel rhsTable = ["rhs_table",columns, key]
		DKColumnComparison[] map = DKColumnComparison.createColumnPlans(lhsTable, rhsTable, (int[])[1], DKEqualsDiffor.instance)
		DKStandardTableComparison plan = new DKStandardTableComparison(lhsTable, rhsTable, DKDiff.Kind.BOTH, map,  (int[])[1], null, 100)
		def comparator = plan.rowComparator
		assert comparator
		assert comparator instanceof ComparatorChain
		assert comparator.size() == 2
	}
}
