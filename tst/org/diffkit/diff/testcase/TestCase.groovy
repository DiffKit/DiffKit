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

import java.io.File;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import org.diffkit.common.DKValidate;



/**
 * @author jpanico
 */
public class TestCase implements Comparable<TestCase>{
   
   public final Integer id
   public final String name
   public final String description
   public final File dbSetupFile
   public final File lhsSourceFile
   public final File rhsSourceFile
   public final File planFile
   public final File expectedFile
   
   public TestCase(Integer id_, String name_, String description_, File dbSetupFile_, File lhsSourceFile_, 
   File rhsSourceFile_, File planFile_, File expectedFile_){
      
      id = id_
      name = name_
      description = description_
      dbSetupFile = dbSetupFile_
      planFile = planFile_
      lhsSourceFile = lhsSourceFile_
      rhsSourceFile = rhsSourceFile_
      expectedFile = expectedFile_
      DKValidate.notNull(id, name)
      this.validate()
   }
   
   public String toString() {
      return String.format("%s(%s)",
      ClassUtils.getShortClassName(this.getClass()), name);
   }
   
   public String getDescription() {
      return ReflectionToStringBuilder.toString(this);
   }
   
   @Override
   public int compareTo(TestCase target_) {
      if (id < target_.id)
         return -1;
      if (id > target_.id)
         return 1;
      return 0;
   }
   
   /**
    * the receiver validates itself
    */
   private void validate(){
      this.validateResourceFile('lhsSourceFile',lhsSourceFile)
      this.validateResourceFile('rhsSourceFile', rhsSourceFile)
      this.validateResourceFile('planFile', planFile)
      this.validateResourceFile('expectedFile', expectedFile)
   }
   
   private void validateResourceFile(String name_, File resourceFile_){
      if(!resourceFile_)
         throw new RuntimeException("does not allow null value for->$name_")
      if(!resourceFile_.canRead())
         throw new RuntimeException("cannot read resourceFile_->$resourceFile_")
   }
}
