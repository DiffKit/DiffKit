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
import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import org.diffkit.common.DKDistProperties;
import org.diffkit.diff.engine.DKDiffEngine;
import org.diffkit.util.DKClassUtil;

/**
 * @author jpanico
 */
public class DKApplication {
   private static final String VERSION_OPTION_KEY = "version";
   private static final String HELP_OPTION_KEY = "help";
   private static final String TEST_OPTION_KEY = "test";
   private static final String PLAN_FILE_OPTION_KEY = "planfile";
   private static final Options OPTIONS = new Options();

   private static final String PLAN_FILE_NAME_REGEX = ".*\\.plan\\.xml";
   private static final Pattern PLAN_FILE_PATTERN = Pattern.compile(PLAN_FILE_NAME_REGEX);
   private static final Logger LOG = LoggerFactory.getLogger(DKApplication.class);

   static {
      OPTIONS.addOption(new Option(VERSION_OPTION_KEY,
         "print the version information and exit"));
      OPTIONS.addOption(new Option(HELP_OPTION_KEY, "print this message"));
      OPTIONS.addOption(new Option(TEST_OPTION_KEY, "run embedded TestCase suite"));
      OPTIONS.addOption(new Option(TEST_OPTION_KEY, "run embedded TestCase suite"));

      OptionBuilder.withArgName("file");
      OptionBuilder.hasArg();
      OptionBuilder.withDescription("use given file for plan");
      OPTIONS.addOption(OptionBuilder.create(PLAN_FILE_OPTION_KEY));
   }

   public static void main(String[] args_) {
      LOG.debug("args_->{}", Arrays.toString(args_));

      try {
         CommandLineParser parser = new PosixParser();
         CommandLine line = parser.parse(OPTIONS, args_);
         if (line.hasOption(VERSION_OPTION_KEY))
            printVersion();
         else if (line.hasOption(HELP_OPTION_KEY))
            printHelp();
         else if (line.hasOption(TEST_OPTION_KEY))
            runTestCases();
         else if (line.hasOption(PLAN_FILE_OPTION_KEY))
            runPlan(line.getOptionValue(PLAN_FILE_OPTION_KEY));
         else
            printInvalidArguments(args_);
      }
      catch (ParseException e_) {
         System.err.println(e_.getMessage());
      }
   }

   private static void printVersion() {
      System.out.println("version->" + DKDistProperties.getPublicVersionString());
      System.exit(0);
   }

   private static void printInvalidArguments(String[] args_) {
      System.err.println(String.format("Invalid command line arguments: %s",
         Arrays.toString(args_)));
      printHelp();
   }

   private static void printHelp() {
      // automatically generate the help statement
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("java -jar diffkit-app.jar", OPTIONS);
   }

   @SuppressWarnings("unchecked")
   private static void runTestCases() {
      LOG.debug("running TestCases");
      try {
         Class<?> testCaseRunnerClass = Class.forName("org.diffkit.diff.testcase.TestCaseRunner");
         LOG.info("testCaseRunnerClass->{}", testCaseRunnerClass);
         Constructor<Runnable> constructor = (Constructor<Runnable>) DKClassUtil.findLongestConstructor(testCaseRunnerClass);
         LOG.debug("constructor->{}", constructor);
         Runnable testCaseRunner = constructor.newInstance((Object) null);
         testCaseRunner.run();
      }
      catch (Exception e_) {
         LOG.error(null, e_);
      }
   }

   private static void runPlan(String planFilePath_) {
      LOG.info("planFilePath_->{}", planFilePath_);
      AbstractXmlApplicationContext context = getContext(planFilePath_);
      LOG.info("context->{}", context);
      DKPlan plan = (DKPlan) context.getBean("plan");
      LOG.info("plan->{}", plan);
      if (plan == null)
         throw new RuntimeException(String.format("no 'plan' bean in plan file->",
            planFilePath_));
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
      AbstractXmlApplicationContext context = null;
      if (file.canRead())
         context = new FileSystemXmlApplicationContext(new String[] { planFilePath_ },
            false);
      else
         context = new ClassPathXmlApplicationContext(planFilePath_);
      context.setClassLoader(DKApplication.class.getClassLoader());
      context.refresh();
      return context;
   }

}
