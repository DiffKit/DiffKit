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
import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import org.diffkit.common.DKDistProperties;
import org.diffkit.diff.engine.DKDiffEngine;

/**
 * @author jpanico
 */
public class DKTool {
   private static final String PLAN_FILE_NAME_REGEX = ".*\\.plan\\.xml";
   private static final Pattern PLAN_FILE_PATTERN = Pattern.compile(PLAN_FILE_NAME_REGEX);
   private static final Logger LOG = LoggerFactory.getLogger(DKTool.class);

   public static void main(String[] args_) {
      LOG.debug("args_->{}", Arrays.toString(args_));
      validateArgs(args_);
      String planFilePath = args_[0];

      AbstractXmlApplicationContext context = getContext(planFilePath);
      LOG.info("context->{}", context);
      context.setClassLoader(DKTool.class.getClassLoader());
      context.refresh();
      DKPlan plan = (DKPlan) context.getBean("plan");
      LOG.info("plan->{}", plan);
      if (plan == null)
         throw new RuntimeException(String.format("no 'plan' bean in config files->",
            Arrays.toString(args_)));
      DKDiffEngine engine = new DKDiffEngine();
      LOG.info("engine->{}", engine);
      try {
         engine.diff(plan.getLhsSource(), plan.getRhsSource(), plan.getSink(),
            plan.getTableComparison());
         System.exit(0);
      }
      catch (Exception e_) {
         LOG.error(null, e_);
         System.exit(-1);
      }
   }

   /**
    * @param planFilePath_
    *           can be either a FS file path (relative or absolute) or it can be
    *           a resource style path that will be resolved via the classpath
    */
   private static AbstractXmlApplicationContext getContext(String planFilePath_) {
      File file = new File(planFilePath_);
      if (file.canRead())
         return new FileSystemXmlApplicationContext(new String[] { planFilePath_ }, false);
      return new ClassPathXmlApplicationContext(planFilePath_);
   }

   private static void validateArgs(String[] args_) {
      if ((ArrayUtils.isEmpty(args_) || args_.length > 1))
         exitWithUsage();
      String arg = StringUtils.trimToNull(args_[0]);
      if (arg == null)
         exitWithUsage();
      if (arg.equals("-version"))
         exitWithVersion();
      if (!PLAN_FILE_PATTERN.matcher(arg).matches())
         exitWithUsage();
   }

   private static void exitWithVersion() {
      System.out.println("version->" + DKDistProperties.getPublicVersionString());
      System.exit(0);
   }

   private static void exitWithUsage() {
      System.err.println("args must be: [-version | <xml plan file name>]");
      System.exit(-1);
   }
}
