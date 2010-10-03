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
import java.lang.reflect.Constructor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import org.diffkit.common.DKValidate;



/**
 * @author jpanico
 */
public class TestCase implements Comparable<TestCase>{
   public static final String DEFAULT_CONNECTION_INFO_FILE_NAME = 'dbConnectionInfo.xml'
   
   public final Integer id
   public final String name
   public final String description
   public final File dbSetupFile
   public final File lhsSourceFile
   public final File rhsSourceFile
   public final File lhsConnectionInfoFile
   public final File rhsConnectionInfoFile
   public final File planFile
   public final File expectedFile
   public final File exceptionFile
   public final Class exceptionClass
   public final String exceptionMessage
   private Boolean _expectedDiff
   private File _defaultConnectionInfoFile
   
   public TestCase(Integer id_, String name_, String description_, 
   File dbSetupFile_, File lhsSourceFile_, File rhsSourceFile_, 
   File lhsConnectionInfoFile_, File rhsConnectionInfoFile_, File planFile_, 
   File expectedFile_, File exceptionFile_){
      
      id = id_
      name = name_
      description = description_
      dbSetupFile = dbSetupFile_
      planFile = planFile_
      lhsSourceFile = lhsSourceFile_
      rhsSourceFile = rhsSourceFile_
      lhsConnectionInfoFile = lhsConnectionInfoFile_
      rhsConnectionInfoFile = rhsConnectionInfoFile_
      expectedFile = expectedFile_
      exceptionFile = exceptionFile_
      DKValidate.notNull(id, name)
      this.validate()
      Exception exception = this.parseExceptionFile()
      if(exception != null){
         exceptionClass = exception.class
         exceptionMessage = exception.message
      }
      else {
         exceptionClass=null
         exceptionMessage=null
      }
   }
   
   public File[] getConnectionInfoFiles(){
      def connectionInfoFiles = []
      if(lhsConnectionInfoFile)
         connectionInfoFiles.add(lhsConnectionInfoFile)
      if(rhsConnectionInfoFile)
         connectionInfoFiles.add(rhsConnectionInfoFile)
      if(!connectionInfoFiles)
         connectionInfoFiles.add(this.getDefaultConnectionInfoFile())
      return connectionInfoFiles
   }
   
   private File getDefaultConnectionInfoFile(){
      if(_defaultConnectionInfoFile)
         return _defaultConnectionInfoFile
      File dir = planFile.parentFile
      _defaultConnectionInfoFile = [dir, DEFAULT_CONNECTION_INFO_FILE_NAME]
      return _defaultConnectionInfoFile
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
   
   public Boolean expectDiff(){
      if(_expectedDiff!=null)
         return _expectedDiff
      _expectedDiff = expectedFile.canRead()
      return _expectedDiff
   }
   
   public boolean expectException() {
      return ! (this.expectDiff())
   }
   
   private Exception parseExceptionFile(){
      if(!exceptionFile.canRead())
         return null
      String exceptionString = FileUtils.readFileToString(exceptionFile)
      String[] lines = StringUtils.split(exceptionString, '\n')
      if(!lines.length == 2)
         throw new RuntimeException(String.format("invalid exception file contents [%s]", exceptionString))
      Class exceptionClass = Class.forName(lines[0])
      Constructor exceptionConstructor = exceptionClass.getDeclaredConstructor(String.class)
      return exceptionConstructor.newInstance(lines[1])
   }
   /**
    * the receiver validates itself
    */
   private void validate(){
      this.validateResourceFile('lhsSourceFile',lhsSourceFile)
      this.validateResourceFile('rhsSourceFile', rhsSourceFile)
      this.validateResourceFile('planFile', planFile)
      boolean expectedPresent = expectedFile.canRead()
      boolean exceptionPresent = exceptionFile.canRead()
      // need either one or the other
      if(! (expectedPresent || exceptionPresent) )
         throw new RuntimeException("Missing one or the other of [$expectedFile], [$exceptionFile]")
      if( expectedPresent && exceptionPresent)
         throw new RuntimeException("Cannot specify both files, only one or the other: [$expectedFile], [$exceptionFile]")
   }
   
   private void validateResourceFile(String name_, File resourceFile_){
      if(!resourceFile_)
         throw new RuntimeException("does not allow null value for->$name_")
      if(!resourceFile_.canRead())
         throw new RuntimeException("cannot read resourceFile_->$resourceFile_")
   }
}
