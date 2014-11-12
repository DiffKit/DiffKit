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


import org.diffkit.diff.engine.DKTableModel;
import org.diffkit.diff.sns.DKListSource 

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestListSource extends GroovyTestCase {
	
	
	public void testSource(){
		
		DKTableModel model = DKTableModel.createGenericStringModel(4, (int[])[2]);
		Object[] row1 = ['row1.col1', 'row1.col2', 'row1.col3', 'row1.col4']
		Object[] row2 = ['row2.col1', 'row2.col2', 'row2.col3', 'row2.col4']
		Object[] row3 = ['row3.col1', 'row3.col2', 'row3.col3', 'row3.col4']
		Object[] row4 = ['row4.col1', 'row4.col2', 'row4.col3', 'row4.col4']
		def rows = [row1, row2, row3, row4]
		
		DKListSource source = [model, rows]
		assert source.model == model
		assert source.nextRow == row1
		assert source.nextRow == row2
		assert source.nextRow == row3
		assert source.nextRow == row4
		assert !source.nextRow
	}
	
}
