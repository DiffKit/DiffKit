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
package org.diffkit.diff.testcase

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;
import org.diffkit.diff.conf.DKPassthroughPlan;
import org.diffkit.diff.conf.DKPlan 
import org.diffkit.diff.engine.DKDiffEngine 
import org.diffkit.util.DKFileUtil;


/**
 * @author jpanico
 */
public class TestCaseRun {
	
	public final TestCase testCase
	public final DKPlan plan
	public Date start
	public Date end
	public String actualFile
	public boolean isExecuted
	private final Logger _log = LoggerFactory.getLogger(this.getClass())
	
	public TestCaseRun(TestCase testCase_, DKPlan plan_){
		this(testCase_, plan_, null, null, null)
	}
	
	public TestCaseRun(TestCase testCase_, DKPlan plan_, Date start_, Date end_,  
	String actualFile_){
		
		testCase = testCase_
		plan = new DKPassthroughPlan(plan_)
		start = start_
		end = end_
		actualFile = actualFile_
		DKValidate.notNull(testCase, plan)
	}
	
	private void execute(){
		DKDiffEngine engine = []
		try{
			isExecuted = true
			engine.diff(plan.lhsSource, plan.rhsSource, plan.sink, plan.tableComparison)
		}
		catch(Exception e_){
			_log.error(null,e_)
		}
	}
	
	public String getReport(){
		if(!isExecuted)
			return 'Not yet executed!'
		File expectedFile = testCase.expectedFile
		// N.B. TestCaseRunner ensures that sink is File type
		File actualFile = plan.sink.file
		String expectedContent = DKFileUtil.readFullyAsString(expectedFile)
		String actualContent = DKFileUtil.readFullyAsString(actualFile)
		def passed = StringUtils.equals( expectedContent, actualContent)
		def resultString = passed ? 'PASSED' : '*FAILED*'
		return "${testCase.name} $resultString"
	}
	
	public String toString() {
		return String.format("%s(%s)",
		ClassUtils.getShortClassName(this.getClass()), testCase.name);
	}
}
