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
package org.diffkit.diff.conf;

import java.io.File;
import java.lang.reflect.Constructor;

import org.apache.commons.beanutils.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKProperties;
import org.diffkit.util.DKClassUtil;

/**
 * dynamic bridge into the TestCase package
 * 
 * @author jpanico
 */
public class DKTestBridge {
   private static final String TESTCASERUNNER_CLASS_NAME = "org.diffkit.diff.testcase.TestCaseRunner";
   private static final Logger LOG = LoggerFactory.getLogger(DKTestBridge.class);

   public static void runTestCases() {
      System.setProperty(DKProperties.IS_TEST_PROPERTY, "true");
      try {
         Runnable testCaseRunner = (Runnable) getTestCaseRunner();
         testCaseRunner.run();
      }
      catch (Exception e_) {
         LOG.error(null, e_);
      }
   }

   public static void ensureData(File testcaseDir_) throws Exception {
      Object testCaseRunner = getTestCaseRunner();
      MethodUtils.invokeExactMethod(testCaseRunner, "setupDB", testcaseDir_);
   }

   private static Object getTestCaseRunner() throws Exception {
      Class<?> testCaseRunnerClass = Class.forName(TESTCASERUNNER_CLASS_NAME);
      LOG.info("testCaseRunnerClass->{}", testCaseRunnerClass);
      Constructor<?> constructor = DKClassUtil.findLongestConstructor(testCaseRunnerClass);
      LOG.debug("constructor->{}", constructor);
      return constructor.newInstance((Object) null);
   }
}
