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

import org.diffkit.diff.conf.DKAutomaticTableComparison;
import org.diffkit.diff.engine.DKColumnModel
import org.diffkit.diff.engine.DKDiff;
import org.diffkit.diff.engine.DKTableModel 
import org.diffkit.diff.sns.DKFileSource 

import groovy.util.GroovyTestCase


/**
 * @author jpanico
 */
public class TestAutomaticTableComparison extends GroovyTestCase {
	
	public void testDefault(){
		def lhsFileResourcePath = 'org/diffkit/diff/conf/tst/test.lhs.csv'
		def rhsFileResourcePath = 'org/diffkit/diff/conf/tst/test.rhs.csv'
		DKColumnModel column1 = [0, 'column1', DKColumnModel.Type.STRING]
		DKColumnModel column2 = [1, 'column2', DKColumnModel.Type.STRING]
		DKColumnModel column3 = [2, 'column3', DKColumnModel.Type.INTEGER]
		DKColumnModel column4 = [3, 'column4', DKColumnModel.Type.STRING]
		DKColumnModel[] columns = [column1, column2, column3, column4]
		int[] key = [0,3]
		DKTableModel lhsTable = ['lhs.table', columns, key]
		DKTableModel rhsTable = ['rhs.table', columns, key]
		DKFileSource lhsFileSource = [lhsFileResourcePath, lhsTable, null, "\\,"]
		DKFileSource rhsFileSource = [rhsFileResourcePath, rhsTable, null, "\\,"]
		
		def defaultComparison = new DKAutomaticTableComparison( lhsFileSource, rhsFileSource, DKDiff.Kind.BOTH, null, null, null, Long.MAX_VALUE, null, null).standardComparison
		println "defaultComparison->${defaultComparison.description}"
		assert defaultComparison
		def map = defaultComparison.map
		assert map
		assert map.size()== 4
		map.each { assert (it._lhsColumn == it._rhsColumn) }
		def diffIndexes = defaultComparison.diffIndexes
		assert diffIndexes
		assert diffIndexes == [1,2]
		def displayIndexes = defaultComparison.displayIndexes
		assert displayIndexes
		assert displayIndexes == [[0,3],[0,3]]
		assert defaultComparison.maxDiffs == Long.MAX_VALUE
	}
}
