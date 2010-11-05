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

import java.text.SimpleDateFormat;


import org.diffkit.common.DKValidate;
import org.diffkit.db.DKDBFlavor 


/**
 * @author jpanico
 */
public class TestCaseRunnerRun {
   
   private static final SimpleDateFormat DIR_NAME_FORMAT = new SimpleDateFormat('MM.dd.yy.HH.mm.ss');
   
   public final File dir
   public final DKDBFlavor flavor
   public List<TestCaseRun> testCaseRuns
   
   public TestCaseRunnerRun(File parentDir_, DKDBFlavor flavor_){
      DKValidate.notNull(parentDir_, flavor_)
      dir = this.createDir(parentDir_)
      flavor = flavor_
      DKValidate.notNull(dir)
   }
   
   private File createDir(File parentDir){
      String dirName ='tcr.run.'+ DIR_NAME_FORMAT.format(new Date())
      File dirFile = new File(parentDir, dirName)
      if (!dirFile.mkdir() )
         throw new RuntimeException("couldn't create directory->$dirFile")
      return dirFile
   }
   
   public void addRun(TestCaseRun run_){
      if(!run_)
         return;
      if(!testCaseRuns)
         testCaseRuns = new ArrayList<TestCaseRun>()
      testCaseRuns.add(run_)
   }
   
   public Boolean getFailed(){
      for(def run : testCaseRuns){
         if(run.failed)
            return true
      }
      return false
   }
}
