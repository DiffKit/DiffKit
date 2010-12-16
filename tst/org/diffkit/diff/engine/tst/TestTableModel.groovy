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



import org.diffkit.diff.engine.DKColumnModel;
import org.diffkit.diff.engine.DKTableModel 

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestTableModel extends GroovyTestCase {
	
	public void testKeyValues(){
		DKColumnModel column1_1 = [0, 'column1_1', DKColumnModel.Type.STRING]
		DKColumnModel column1_2 = [1, 'column1_2', DKColumnModel.Type.STRING]
		DKColumnModel column1_3 = [2, 'column1_3', DKColumnModel.Type.INTEGER]
		DKColumnModel[] columns = [column1_1, column1_2, column1_3]
		int[] key = [0,2]
		DKTableModel lhsTable = ["lhs_table",columns, key]
		println "column1_1->$column1_1"
		println "lhsTable->$lhsTable"
		
		assert lhsTable.getKeyValues((Object[])['zzzz', 'aaaa', 1]) == ['zzzz',1]
		assert lhsTable.getKeyValues((Object[])['zzzz', 'zzzz', 2]) == ['zzzz',2]
	}
	
}
