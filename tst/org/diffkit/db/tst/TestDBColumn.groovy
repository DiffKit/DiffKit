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
package org.diffkit.db.tst


import org.diffkit.db.DKDBColumn 
import org.diffkit.db.DKDBType;
import org.diffkit.db.DKDBTypeInfo;

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestDBColumn extends GroovyTestCase {
   
   public void testCompareTo(){
      DKDBColumn[] columns = new DKDBColumn[4]
      columns[0] = new DKDBColumn('col4',4,'VARCHAR',-1,false)
      columns[1] = new DKDBColumn('col1',1,'VARCHAR',-1,false)
      columns[2] = new DKDBColumn('col3',3,'VARCHAR',-1,false)
      columns[3] = new DKDBColumn('col2',2,'VARCHAR',-1,false)
      
      Arrays.sort(columns)
      assert columns[0].ordinalPosition == 1 
      assert columns[1].ordinalPosition == 2 
      assert columns[2].ordinalPosition == 3 
      assert columns[3].ordinalPosition == 4
   }
}
