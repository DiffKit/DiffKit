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
package org.diffkit.diff.testcase

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;
import org.diffkit.diff.conf.DKApplication;
import org.diffkit.diff.conf.DKPassthroughPlan;
import org.diffkit.diff.conf.DKPlan 
import org.diffkit.diff.engine.DKDiffEngine 
import org.diffkit.util.DKFileUtil;


/**
 * @author jpanico
 */
public class TestCaseRun {
   
   public final TestCase testCase
   public DKPlan plan
   private Date _start
   private Date _end
   private String _actualFile
   private boolean _isExecuted
   private Boolean _failed
   private Exception _exception
   private final Logger _log = LoggerFactory.getLogger(this.getClass())
   
   public TestCaseRun(TestCase testCase_, DKPlan plan_){
      this(testCase_, plan_, null, null, null)
   }
   
   public TestCaseRun(TestCase testCase_, DKPassthroughPlan plan_, Date start_, Date end_,  
   String actualFile_){
      
      testCase = testCase_
      this.setPlan(plan_)
      _start = start_
      _end = end_
      _actualFile = actualFile_
      DKValidate.notNull(testCase)
   }
   
   public void setPlan(DKPassthroughPlan plan_){
      plan = plan_
   }
   
   public void diff(){
      DKApplication.doDiff plan.lhsSource, plan.rhsSource, plan.sink, plan.tableComparison, null
   }
   
   public void setIsExecuted(boolean isExecuted_){
      _isExecuted = isExecuted_
   }
   
   public void setException(Exception exception_){
      _exception = exception_;
   }
   
   public Boolean getFailed(){
      if(_failed)
         return _failed
      _log.debug("expectDiff->{}",testCase.expectDiff())
      if(testCase.expectDiff())
         return this.getDiffFailed();
      else if(testCase.expectException())
         return this.getExceptionFailed();
      throw new RuntimeException("reached unanticipated point in code")
   }
   
   private Boolean getDiffFailed(){
      File expectedFile = testCase.expectedFile
      // N.B. TestCaseRunner ensures that sink is File type
      File actualFile = plan.sink.file
      String expectedContent = DKFileUtil.readFullyAsString(expectedFile)
      String actualContent = DKFileUtil.readFullyAsString(actualFile)
      _failed = ! StringUtils.equals( expectedContent, actualContent)
      return _failed
   }
   
   private Boolean getExceptionFailed(){
      if(!testCase.expectException()){
         if(_exception)
            return true
      }
      if(!_exception)
         return true
      Exception rootException = ExceptionUtils.getRootCause(_exception)
      Class expectedExceptionClass = testCase.exceptionClass
      _log.debug("rootException->{}",rootException)
      _log.debug("expectedExceptionClass->{}",expectedExceptionClass)
      _log.debug("rootException.class != expectedExceptionClass->{}",rootException.class != expectedExceptionClass)
      if(rootException.class != expectedExceptionClass)
         return true
      _log.debug("rootException.class != expectedExceptionClass->{}",rootException.class != expectedExceptionClass)
      if(!rootException.message.startsWith(testCase.exceptionMessage ))
         return true
      return false
   }
   
   public String getReport(){
      _log.debug("_exception->{}",_exception)
      def resultString = (!this.failed ? 'PASSED' : '*FAILED*')
      return "${testCase.name} $resultString"
   }
   
   public String toString() {
      return String.format("%s(%s)",
      ClassUtils.getShortClassName(this.getClass()), testCase.name);
   }
}
