
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
package org.diffkit.diff.conf.tst


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext 
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.diffkit.db.tst.DBTestSetup;
import org.diffkit.diff.engine.DKColumnModel;
import org.diffkit.diff.sns.DKDBSource 
import org.diffkit.diff.sns.DKWriterSink 

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestTool extends GroovyTestCase {
   private final Logger _log = LoggerFactory.getLogger(this.getClass())
   
   public void testPlan(){
      DBTestSetup.setupDB(new File('org/diffkit/diff/conf/tst/test.dbsetup.xml'), (File[])[new File('org/diffkit/diff/conf/tst/dbConnectionInfo.xml')], 'org/diffkit/diff/conf/tst/test.lhs.csv', 'org/diffkit/diff/conf/tst/test.rhs.csv')
      ApplicationContext context = new ClassPathXmlApplicationContext('org/diffkit/diff/conf/tst/testtool.xml');
      assert context
      
      def plan = context.getBean('plan')
      println "plan->$plan"
      assert plan
      def lhsColumn1 = context.getBean('lhs.column1')
      assert lhsColumn1
      assert lhsColumn1._index == 0
      assert lhsColumn1._name == 'column1'
      assert lhsColumn1._type == DKColumnModel.Type.STRING
      
      def tableModel = context.getBean('lhs.table.model')
      assert tableModel
      assert tableModel.columns.length == 3
      assert tableModel.columns[0].index == 0
      assert tableModel.columns[0].name == 'column1'
      assert tableModel.columns[0].type == DKColumnModel.Type.STRING
      assert tableModel.columns[2].name == 'column3'
      assert tableModel.columns[2].type == DKColumnModel.Type.INTEGER
      
      def lhsSource = plan.lhsSource
      assert lhsSource
      assert lhsSource instanceof DKDBSource
      assert lhsSource.tableName == 'LHS_TABLE'
      
      def rhsSource = plan.rhsSource
      assert rhsSource
      assert rhsSource instanceof DKDBSource
      assert rhsSource.tableName == 'RHS_TABLE'
      
      def sink = plan.sink
      assert sink
      assert sink instanceof DKWriterSink
      
      def tableComparison = plan.tableComparison
      assert tableComparison
      assert tableComparison.diffIndexes== [1,2]
   }
}

