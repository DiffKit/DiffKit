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
import java.io.FileNotFoundException;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKDistProperties;
import org.diffkit.common.DKRuntime;
import org.diffkit.common.DKUserException;
import org.diffkit.diff.engine.DKContext;
import org.diffkit.diff.engine.DKDiffEngine;
import org.diffkit.diff.engine.DKSink;
import org.diffkit.diff.engine.DKSource;
import org.diffkit.diff.engine.DKTableComparison;
import org.diffkit.util.DKSpringUtil;

/**
 * @author jpanico
 */
public class DKApplication {
   private static final String APPLICATION_NAME = "diffkit-app";
   private static final String VERSION_OPTION_KEY = "version";
   private static final String HELP_OPTION_KEY = "help";
   private static final String TEST_OPTION_KEY = "test";
   private static final String PLAN_FILE_OPTION_KEY = "planfiles";
   private static final String ERROR_ON_DIFF_OPTION_KEY = "errorOnDiff";
   private static final String DEMO_DB_OPTION_KEY = "demoDB";
   private static final Options OPTIONS = new Options();

   private static final String CONF_DIR_NAME = "conf";
   private static final String LOGBACK_FILE_NAME = "logback.xml";
   private static final String LOGBACK_CONFIGURATION_FILE_PROPERTY_KEY = "logback.configurationFile";
   private static Logger _systemLog;
   private static Logger _userLog;
   private static File _confDir;

   static {
      OPTIONS.addOption(new Option(VERSION_OPTION_KEY,
         "print the version information and exit"));
      OPTIONS.addOption(new Option(HELP_OPTION_KEY, "print this message"));
      OPTIONS.addOption(new Option(TEST_OPTION_KEY, "run embedded TestCase suite"));

      OptionBuilder.withArgName("file1[,file2...]");
      OptionBuilder.hasArg();
      OptionBuilder.withDescription("perform diff using given file(s) for plan");
      OPTIONS.addOption(OptionBuilder.create(PLAN_FILE_OPTION_KEY));
      OPTIONS.addOption(new Option(
         ERROR_ON_DIFF_OPTION_KEY,
         "exit with error status code (-1) if diffs are detected. otherwise will always exit with 0 unless an operating Exception was encountered"));
      OPTIONS.addOption(new Option(DEMO_DB_OPTION_KEY, "run embedded demo H2 database"));
   }

   public static void main(String[] args_) {
      initialize();
      Logger systemLog = getSystemLog();
      Logger userLog = getUserLog();
      systemLog.debug("args_->{}", Arrays.toString(args_));

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
            runPlan(line.getOptionValue(PLAN_FILE_OPTION_KEY),
               line.hasOption(ERROR_ON_DIFF_OPTION_KEY));
         else if (line.hasOption(DEMO_DB_OPTION_KEY))
            runDemoDB();
         else
            printInvalidArguments(args_);
      }
      catch (ParseException e_) {
         System.err.println(e_.getMessage());
      }
      catch (Throwable e_) {
         Throwable rootCause = ExceptionUtils.getRootCause(e_);
         if (rootCause == null)
            rootCause = e_;
         if ((rootCause instanceof DKUserException)
            || (rootCause instanceof FileNotFoundException)) {
            systemLog.info(null, e_);
            userLog.info("error->{}", rootCause.getMessage());
         }
         else
            systemLog.error(null, e_);
      }
   }

   private static void printVersion() {
      Logger userLog = getUserLog();
      userLog.info("version->" + DKDistProperties.getPublicVersionString());
      System.exit(0);
   }

   private static void printInvalidArguments(String[] args_) {
      getUserLog().info(
         String.format("Invalid command line arguments: %s", Arrays.toString(args_)));
      printHelp();
   }

   private static void printHelp() {
      // automatically generate the help statement
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("java -jar diffkit-app.jar", OPTIONS);
   }

   private static void runPlan(String planFilesString_, boolean errorOnDiff_)
      throws Exception {
      Logger systemLog = getSystemLog();
      Logger userLog = getUserLog();
      systemLog.info("planFilesString_->{}", planFilesString_);
      String[] planFiles = planFilesString_.split("\\,");
      userLog.info("planfile(s)->{}", planFiles);
      DKPlan plan = (DKPlan) DKSpringUtil.getBean("plan", planFiles,
         DKApplication.class.getClassLoader());
      systemLog.info("plan->{}", plan);
      DKDiffEngine engine = new DKDiffEngine();
      systemLog.info("engine->{}", engine);
      DKSource lhsSource = plan.getLhsSource();
      DKSource rhsSource = plan.getRhsSource();
      DKSink sink = plan.getSink();
      DKTableComparison tableComparison = plan.getTableComparison();
      userLog.info("lhsSource->{}", lhsSource);
      userLog.info("rhsSource->{}", rhsSource);
      userLog.info("sink->{}", sink);
      userLog.info("tableComparison->{}", tableComparison);
      DKContext diffContext = engine.diff(lhsSource, rhsSource, sink, tableComparison);
      userLog.info(sink.generateSummary(diffContext));
      if (plan.getSink().getDiffCount() == 0)
         System.exit(0);
      if (errorOnDiff_)
         System.exit(-1);
      System.exit(0);
   }

   private static void runDemoDB() throws Exception {
      DKDemoDB.run();
   }

   private static void runTestCases() {
      Logger userLog = getUserLog();
      userLog.info("running TestCases");
      DKTestBridge.runTestCases();
   }

   private static void initialize() {
      DKRuntime.getInstance().setApplicationName(APPLICATION_NAME);
      configureLogging();
      getUserLog().info("DiffKit home->" + DKRuntime.getInstance().getDiffKitHome());
   }

   private static void configureLogging() {
      File logbackConfFile = new File(getConfDir(), LOGBACK_FILE_NAME);
      if (!logbackConfFile.canRead())
         System.out.printf(
            "WARNING: logging configuration file '%s' does not exist or can not be read! will stagger on as best as can.",
            logbackConfFile);
      if (System.getProperty(LOGBACK_CONFIGURATION_FILE_PROPERTY_KEY) == null)
         System.setProperty(LOGBACK_CONFIGURATION_FILE_PROPERTY_KEY,
            logbackConfFile.getAbsolutePath());
   }

   private static File getConfDir() {
      if (_confDir != null)
         return _confDir;
      File home = DKRuntime.getInstance().getDiffKitHome();
      _confDir = new File(home, CONF_DIR_NAME);
      if (!_confDir.isDirectory())
         System.out.printf(
            "WARNING: configuration directory '%s' does not exist! will stagger on as best as can.",
            _confDir);
      return _confDir;
   }

   private static Logger getSystemLog() {
      if (_systemLog != null)
         return _systemLog;
      _systemLog = LoggerFactory.getLogger(DKApplication.class);
      return _systemLog;
   }

   private static Logger getUserLog() {
      if (_userLog != null)
         return _userLog;
      _userLog = LoggerFactory.getLogger("user");
      return _userLog;
   }
}
