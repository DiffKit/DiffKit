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



import org.diffkit.diff.diffor.DKEqualsDiffor;
import org.diffkit.diff.engine.DKColumnComparison 
import org.diffkit.diff.engine.DKColumnModel;

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestColumnComparison extends GroovyTestCase {
	
	public void testDiff(){
		
		Object[] lhs = ['value1', 'value2', 3, 'value4'] 
		Object[] rhs = ['value0', 'value1', 3, 'value4'] 
		
		DKColumnModel lhColumnModel = [0, 'column1', DKColumnModel.Type.STRING]
		DKColumnModel rhColumnModel = [1, 'column2', DKColumnModel.Type.STRING]
		DKColumnComparison columnPlan = [lhColumnModel, rhColumnModel, DKEqualsDiffor.instance]
		assert ! columnPlan.isDiff(lhs, rhs, null)
		
		lhColumnModel = [0, 'column1', DKColumnModel.Type.STRING]
		rhColumnModel = [0, 'column1', DKColumnModel.Type.STRING]
		columnPlan = [lhColumnModel, rhColumnModel, DKEqualsDiffor.instance]
		assert  columnPlan.isDiff(lhs, rhs, null)
		
		lhColumnModel = [2, 'column3', DKColumnModel.Type.INTEGER]
		rhColumnModel = [2, 'column3', DKColumnModel.Type.INTEGER]
		columnPlan = [lhColumnModel, rhColumnModel, DKEqualsDiffor.instance]
		assert  !columnPlan.isDiff(lhs, rhs, null)
	}
	
	public void testGet(){
		
		Object[] lhs = ['value1', 'value2', 3, 'value4'] 
		Object[] rhs = ['value0', 'value1', 3, 'value4'] 
		
		DKColumnModel lhColumnModel = [0, 'column1', DKColumnModel.Type.STRING]
		DKColumnModel rhColumnModel = [1, 'column2', DKColumnModel.Type.STRING]
		DKColumnComparison columnPlan = [lhColumnModel, rhColumnModel, DKEqualsDiffor.instance]
		assert columnPlan.getLHValue(lhs) == 'value1'
		assert columnPlan.getRHValue(rhs) == 'value1'
		
		lhColumnModel = [0, 'column1', DKColumnModel.Type.STRING]
		rhColumnModel = [0, 'column1', DKColumnModel.Type.STRING]
		columnPlan = [lhColumnModel, rhColumnModel, DKEqualsDiffor.instance]
		assert columnPlan.getLHValue(lhs) == 'value1'
		assert columnPlan.getRHValue(rhs) == 'value0'
		
		lhColumnModel = [2, 'column3', DKColumnModel.Type.INTEGER]
		rhColumnModel = [2, 'column3', DKColumnModel.Type.INTEGER]
		columnPlan = [lhColumnModel, rhColumnModel, DKEqualsDiffor.instance]
		assert columnPlan.getLHValue(lhs) == 3
		assert columnPlan.getRHValue(rhs) == 3
	}
}
