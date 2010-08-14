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
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import org.diffkit.common.DKValidate;
import org.diffkit.util.DKResourceUtil;



/**
 * @author jpanico
 */
public class TestCase implements Comparable<TestCase>{
   
   public final Integer id
   public final String name
   public final String description
   public final String dbSetupPath
   public final String lhsSourcePath
   public final String rhsSourcePath
   public final String planFile
   public final String expectedPath
   
   public TestCase(Integer id_, String name_, String description_, String dbSetupPath_, String lhsSourcePath_, 
   String rhsSourcePath_, String planFile_, String expectedPath_){
      
      id = id_
      name = name_
      description = description_
      dbSetupPath = dbSetupPath_
      planFile = planFile_
      lhsSourcePath = lhsSourcePath_
      rhsSourcePath = rhsSourcePath_
      expectedPath = expectedPath_
      DKValidate.notNull(id, name,  lhsSourcePath, rhsSourcePath, planFile, expectedPath)
   }
   
   public File getLhsSourceFile(){
      return DKResourceUtil.findResourceAsFile(lhsSourcePath)
   }
   
   public File getRhsSourceFile(){
      return DKResourceUtil.findResourceAsFile(rhsSourcePath)
   }
   
   public File getExpectedFile(){
      return DKResourceUtil.findResourceAsFile(expectedPath)
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
   public void validate(){
      this.validateResource(lhsSourcePath)
      this.validateResource(rhsSourcePath)
      this.validateResource(planFile)
      this.validateResource(expectedPath)
   }
   
   private String getAbsolutePathForResource(String resourcePath_){
      File resourceFile = DKResourceUtil.findResourceAsFile(resourcePath_)
      if(!resourceFile)
         return null
      return resourceFile.absolutePath
   }
   
   private void validateResource(String resourcePath_){
      def resourceFile = DKResourceUtil.findResourceAsFile(resourcePath_)
      if(!resourceFile)
         throw new RuntimeException("couldn't find resourcePath_->$resourcePath_")
      if(!resourceFile.canRead())
         throw new RuntimeException("can't read resourceFile->$resourceFile")
   }
}
