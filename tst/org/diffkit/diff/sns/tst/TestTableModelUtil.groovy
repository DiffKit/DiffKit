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




import org.diffkit.db.DKDBColumn 
import org.diffkit.db.DKDBFlavor;
import org.diffkit.db.DKDBPrimaryKey 
import org.diffkit.db.DKDBTable;
import org.diffkit.db.DKDBType 
import org.diffkit.db.DKDBTypeInfo 
import org.diffkit.diff.engine.DKColumnModel;
import org.diffkit.diff.sns.DKTableModelUtil;

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestTableModelUtil extends GroovyTestCase {
   
   
   public void testCreateColumnModel(){
      def table = this.createCustomerMetaTable()
      def firstName = DKTableModelUtil.createDefaultColumnModel(table.columns[0])
      
      assert firstName.name == 'first_name'
      assert firstName.index == 0
      assert firstName.type == DKColumnModel.Type.STRING
      
      def age = DKTableModelUtil.createDefaultColumnModel(table.columns[5])
      assert age.name == 'age'
      assert age.index == 5
      assert age.type == DKColumnModel.Type.NUMBER
   }
   
   public void testCreateTableModel(){
      def table = this.createCustomerMetaTable()
      def tableModel = DKTableModelUtil.createDefaultTableModel(table, null)
      
      assert tableModel
      assert tableModel.name == 'CUSTOMER'
      assert tableModel.columns
      assert tableModel.columns.length == 6
      
      assert tableModel.columns[0].name == 'first_name'
      assert tableModel.columns[0].index == 0
      assert tableModel.columns[0].type == DKColumnModel.Type.STRING
      
      assert tableModel.key == (int[])[0,1]
   }
   
   private DKDBTable createCustomerMetaTable(){
      DKDBColumn column1 = ['first_name', 1, 'VARCHAR', 20, true]
      DKDBColumn column2 = ['last_name', 2, 'VARCHAR', -1, true]
      DKDBColumn column3 = ['address', 3, 'VARCHAR', -1, true]
      DKDBColumn column4 = ['city', 4, 'VARCHAR', -1, true]
      DKDBColumn column5 = ['country', 5, 'VARCHAR', -1, true]
      DKDBColumn column6 = ['age', 6, 'INTEGER', -1, true]
      DKDBColumn[] columns = [column1, column2, column3, column4, column5, column6]
      String[] pkColNames = ['first_name', 'last_name']
      DKDBPrimaryKey pk = ['pk_customer', pkColNames]
      DKDBTable table = [ null, null, 'CUSTOMER', columns, pk]
      return table
   }
}
