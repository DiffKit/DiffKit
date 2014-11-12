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

import org.diffkit.diff.engine.DKColumnComparison 
import org.diffkit.diff.engine.DKColumnDiffRow 
import org.diffkit.diff.engine.DKColumnModel 
import org.diffkit.diff.engine.DKContext 
import org.diffkit.diff.engine.DKRowDiff 
import org.diffkit.diff.engine.DKSide;
import org.diffkit.diff.engine.DKStandardTableComparison 
import org.diffkit.diff.engine.DKTableModel;
import org.diffkit.diff.engine.DKDiff;
import org.diffkit.diff.sns.DKDefaultFormatter;
import org.diffkit.diff.diffor.DKEqualsDiffor

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestFormatter  extends GroovyTestCase {
	
	
	public void testDefaultFormatter(){
		Object[] row = ['1111', '1111', 1]
		def plan = this.createSimplePlan()
		def keyValues = plan.getRowKeyValues( row, DKSide.LEFT_INDEX)
		def displayValues = plan.getRowDisplayValues( row, DKSide.LEFT_INDEX )
		DKRowDiff rowDiff = [1, row, DKSide.LEFT, plan]
		def context = new DKContext()
		String formatted = DKDefaultFormatter.instance.formatRowDiff(rowDiff, context)
		
		assert formatted == '!{column2=1111, column3=1}\n>'
		
		DKColumnDiffRow diffRow = [1, row, row, plan]
		def columnDiff = diffRow.createDiff(1, '1111', 'xxxx')
		formatted = DKDefaultFormatter.instance.formatColumnDiff(columnDiff, context)
		println "formatted->$formatted"
		assert formatted == '@{column2=1111, column3=1}\ncolumn2\n<1111\n>xxxx'
		
	}
	
	private DKStandardTableComparison createSimplePlan() {
		DKTableModel tableModel = this.createSimpleTableModel();
		DKColumnComparison[] map = DKColumnComparison.createColumnPlans( tableModel,tableModel, (int[]) [1], DKEqualsDiffor.instance)
		DKStandardTableComparison plan = new DKStandardTableComparison(tableModel, tableModel, DKDiff.Kind.BOTH, map, (int[])[0], (int[][])[[1,2],[1,2]], (long)100)
		return plan
	}
	
	private DKTableModel createSimpleTableModel(){
		DKColumnModel column1 = [0, 'column1', DKColumnModel.Type.STRING]
		DKColumnModel column2 = [1, 'column2', DKColumnModel.Type.STRING]
		DKColumnModel column3 = [2, 'column3', DKColumnModel.Type.INTEGER]
		DKColumnModel[] columns = [column1, column2, column3]
		int[] key = [0,2]
		
		return new DKTableModel("simple_table_model",columns, key) 
	}
}
