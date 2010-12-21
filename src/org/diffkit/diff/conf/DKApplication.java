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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKDistProperties;
import org.diffkit.common.DKRuntime;
import org.diffkit.common.DKUserException;
import org.diffkit.db.DKDBFlavor;
import org.diffkit.diff.engine.DKContext;
import org.diffkit.diff.engine.DKContext.UserKey;
import org.diffkit.diff.engine.DKDiffEngine;
import org.diffkit.diff.engine.DKSink;
import org.diffkit.diff.engine.DKSource;
import org.diffkit.diff.engine.DKTableComparison;
import org.diffkit.util.DKMapUtil;
import org.diffkit.util.DKSpringUtil;
import org.diffkit.util.DKStringUtil;

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

   private static final String LOGBACK_FILE_NAME = "logback.xml";
   private static final String LOGBACK_CONFIGURATION_FILE_PROPERTY_KEY = "logback.configurationFile";

   private static Logger _systemLog;

   static {
      OptionGroup optionGroup = new OptionGroup();
      optionGroup.addOption(new Option(VERSION_OPTION_KEY,
         "print the version information and exit"));
      optionGroup.addOption(new Option(HELP_OPTION_KEY, "print this message"));

      OptionBuilder.hasOptionalArgs(2);
      OptionBuilder.withArgName("[cases=?,] [flavors=?,]");
      OptionBuilder.withDescription("run TestCases");
      OPTIONS.addOption(OptionBuilder.create(TEST_OPTION_KEY));

      OptionBuilder.withArgName("file1[,file2...]");
      OptionBuilder.hasArg();
      OptionBuilder.withDescription("perform diff using given file(s) for plan");
      optionGroup.addOption(OptionBuilder.create(PLAN_FILE_OPTION_KEY));
      optionGroup.addOption(new Option(
         ERROR_ON_DIFF_OPTION_KEY,
         "exit with error status code (-1) if diffs are detected. otherwise will always exit with 0 unless an operating Exception was encountered"));
      optionGroup.addOption(new Option(DEMO_DB_OPTION_KEY,
         "run embedded demo H2 database"));
      OPTIONS.addOptionGroup(optionGroup);
   }

   public static void main(String[] args_) {
      initialize();
      Logger systemLog = getSystemLog();
      systemLog.debug("args_->{}", Arrays.toString(args_));

      try {
         CommandLineParser parser = new PosixParser();
         CommandLine line = parser.parse(OPTIONS, args_);
         if (line.hasOption(VERSION_OPTION_KEY))
            printVersion();
         else if (line.hasOption(HELP_OPTION_KEY))
            printHelp();
         else if (line.hasOption(TEST_OPTION_KEY))
            runTestCases(line.getOptionValues(TEST_OPTION_KEY));
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
            DKRuntime.getInstance().getUserLog().info("error->{}", rootCause.getMessage());
         }
         else
            systemLog.error(null, e_);
      }
   }

   private static void printVersion() {
      DKRuntime.getInstance().getUserLog().info(
         "version->" + DKDistProperties.getPublicVersionString());
      System.exit(0);
   }

   private static void printInvalidArguments(String[] args_) {
      DKRuntime.getInstance().getUserLog().info(
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
      Logger userLog = DKRuntime.getInstance().getUserLog();
      systemLog.info("planFilesString_->{}", planFilesString_);
      String[] planFiles = planFilesString_.split("\\,");
      userLog.info("planfile(s)->{}", Arrays.toString(planFiles));
      DKPlan plan = (DKPlan) DKSpringUtil.getBean("plan", planFiles,
         DKApplication.class.getClassLoader());
      systemLog.info("plan->{}", plan);
      DKSource lhsSource = plan.getLhsSource();
      DKSource rhsSource = plan.getRhsSource();
      DKSink sink = plan.getSink();
      DKTableComparison tableComparison = plan.getTableComparison();
      userLog.info("lhsSource->{}", lhsSource);
      userLog.info("rhsSource->{}", rhsSource);
      userLog.info("sink->{}", sink);
      userLog.info("tableComparison->{}", tableComparison);
      Map<UserKey, Object> userDictionary = new HashMap<UserKey, Object>();
      userDictionary.put(UserKey.PLAN_FILES, planFilesString_);
      DKContext diffContext = doDiff(lhsSource, rhsSource, sink, tableComparison,
         userDictionary);
      userLog.info(sink.generateSummary(diffContext));
      if (plan.getSink().getDiffCount() == 0)
         System.exit(0);
      if (errorOnDiff_)
         System.exit(-1);
      System.exit(0);
   }

   @SuppressWarnings("unchecked")
   private static DKContext doDiff(DKSource lhsSource_, DKSource rhsSource_,
                                   DKSink sink_, DKTableComparison tableComparison_,
                                   Map<UserKey, Object> userDictionary_) throws Exception {
      Logger systemLog = getSystemLog();
      DKDiffEngine engine = new DKDiffEngine();
      userDictionary_ = DKMapUtil.combine(userDictionary_,
         tableComparison_.getUserDictionary());
      systemLog.info("engine->{}", engine);
      return engine.diff(lhsSource_, rhsSource_, sink_, tableComparison_, userDictionary_);
   }

   private static void runDemoDB() throws Exception {
      DKDemoDB.run();
   }

   @SuppressWarnings("unchecked")
   private static void runTestCases(String[] args_) {
      Logger systemLog = getSystemLog();
      systemLog.info("args_->{}", Arrays.toString(args_));
      DKRuntime.getInstance().getUserLog().info("running TestCases");
      Map<String, ?> testCaseParams = parseTestCaseArgs(args_);
      systemLog.debug("testCaseParams->{}", testCaseParams);
      DKTestBridge.runTestCases((List<Integer>) testCaseParams.get("cases"),
         (List<DKDBFlavor>) testCaseParams.get("flavors"));
   }

   /**
    * @return guaranteed to be non-null. <br/>
    *         keys: cases, flavors
    */
   @SuppressWarnings("unchecked")
   private static Map<String, ?> parseTestCaseArgs(String[] args_) {
      if (ArrayUtils.isEmpty(args_))
         return new HashMap<String, Object>();
      HashMap<String, Object> parms = new HashMap<String, Object>();
      for (String arg : args_) {
         if (arg.startsWith("cases=")) {
            String[] elements = arg.split("=");
            if (elements.length != 2)
               throw new DKUserException(String.format("unrecognized argument value->%s",
                  arg));
            List<Integer> caseNumbers = DKStringUtil.parseIntegerList(elements[1]);
            parms.put(elements[0], caseNumbers);
         }
         else if (arg.startsWith("flavors=")) {
            String[] elements = arg.split("=");
            if (elements.length != 2)
               throw new DKUserException(String.format("unrecognized argument value->%s",
                  arg));
            List<DKDBFlavor> flavors = (List<DKDBFlavor>) DKStringUtil.parseEnumList(
               elements[1], DKDBFlavor.class);
            parms.put(elements[0], flavors);
         }
         else
            throw new DKUserException(String.format("unrecognized argument value->%s",
               arg));
      }
      return parms;
   }

   private static void initialize() {
      DKRuntime.getInstance().setApplicationName(APPLICATION_NAME);
      configureLogging();
      DKRuntime.getInstance().getUserLog().info(
         "DiffKit home->" + DKRuntime.getInstance().getDiffKitHome());
   }

   private static void configureLogging() {
      File logbackConfFile = new File(DKRuntime.getInstance().getConfDir(),
         LOGBACK_FILE_NAME);
      String logConfPath = null;
      if (!logbackConfFile.canRead()) {
         System.out.printf("no logging configuration file->%s.\n", logbackConfFile);
         // there is a default conf file in the jar that should get picked up
         // with this entry
         logConfPath = "conf/" + LOGBACK_FILE_NAME;
      }
      else {
         logConfPath = logbackConfFile.getAbsolutePath();
      }
      if (System.getProperty(LOGBACK_CONFIGURATION_FILE_PROPERTY_KEY) == null)
         System.setProperty(LOGBACK_CONFIGURATION_FILE_PROPERTY_KEY, logConfPath);
   }

   private static Logger getSystemLog() {
      if (_systemLog != null)
         return _systemLog;
      _systemLog = LoggerFactory.getLogger(DKApplication.class);
      return _systemLog;
   }
}
