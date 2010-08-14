package org.diffkit.diff.testcase;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.diffkit.common.DKRegexFilenameFilter;
import org.diffkit.common.DKValidate;
import org.diffkit.db.DKDBConnectionSource;
import org.diffkit.db.DKDBH2Loader;
import org.diffkit.db.DKDBTableLoader;
import org.diffkit.db.tst.DBTestSetup;
import org.diffkit.diff.conf.DKPlan;
import org.diffkit.diff.engine.DKSink;
import org.diffkit.diff.engine.DKSource;
import org.diffkit.diff.engine.DKSourceSink;
import org.diffkit.diff.engine.DKSourceSink.Kind;
import org.diffkit.diff.sns.DKFileSink;
import org.diffkit.diff.sns.DKWriterSink;
import org.diffkit.util.DKResourceUtil;
import org.diffkit.util.DKStringUtil;

/**
 * @author jpanico
 */
public class TestCaseRunner {

   public static final String TEST_CASE_FILE_NAME = "testcaserunner.xml";
   public static final String TEST_CASE_PLAN_FILE_PATTERN = "test(\\d*)\\.plan\\.xml";
   public static final DKRegexFilenameFilter TEST_CASE_PLAN_FILTER = new DKRegexFilenameFilter(
      TEST_CASE_PLAN_FILE_PATTERN);

   private List<Integer> _testCaseNumbers;
   private String _dataPath;
   private List<TestCase> _allTestCases;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   /**
    * @param dataPath_
    *           expressed as a classpath resource
    */
   public TestCaseRunner(List<Integer> testCaseNumbers_, String dataPath_) {
      _testCaseNumbers = testCaseNumbers_;
      _dataPath = dataPath_;
      DKValidate.notNull(dataPath_);
      _allTestCases = this.fetchAllTestCases(_dataPath);
      _log.debug("_allTestCases->{}", _allTestCases);
   }

   public void run() {
      List<TestCase> testCases = null;
      if (_testCaseNumbers == null) {
         _log.info("no TestCase numbers specified; will use all TestCases found at _dataPath->$_dataPath");
         testCases = _allTestCases;
      }
      else {
         testCases = new ArrayList<TestCase>();
         for (TestCase testCase : _allTestCases) {
            if (_testCaseNumbers.contains(testCase.id))
               testCases.add(testCase);
         }
      }

      Collections.sort(testCases);
      _log.info("testCases->{}", testCases);
      TestCaseRunnerRun runnerRun = new TestCaseRunnerRun(new File("./"));
      for (TestCase testCase : testCases)
         this.run(testCase, runnerRun);
      this.report(runnerRun);
   }

   private TestCaseRun run(TestCase testCase_, TestCaseRunnerRun runnerRun_) {
      _log.info("testCase_->{}", testCase_.description);
      this.setupDB(testCase_);
      TestCaseRun run = new TestCaseRun(testCase_, this.getPlan(testCase_));
      runnerRun_.addRun(run);
      _log.debug("run->{}", run);
      this.validate(run);
      this.setup(run, runnerRun_);
      this.execute(run);
      return run;
   }

   private void setupDB(TestCase testCase_) {
      DBTestSetup.setupDB(testCase_.dbSetupPath, testCase_.getLhsSourceFile(),
         testCase_.getRhsSourceFile());
   }

   private DKPlan getPlan(TestCase testCase_) {
      ApplicationContext context = new ClassPathXmlApplicationContext(testCase_.planFile);
      assert (context != null);
      DKPlan plan = (DKPlan) context.getBean("plan");
      _log.debug("plan->{}", plan);
      if (plan == null)
         throw new RuntimeException(String.format("no 'plan' bean in planFile->%s",
            testCase_.planFile));
      return plan;
   }

   private void report(TestCaseRunnerRun runnerRun_) {
      System.out.printf("\nTestCaseRunnerRun -- %s\n==================\n", runnerRun_.dir);
      System.out.printf("\n\tTestCaseRuns\n\t------------\n");
      for (TestCaseRun testCaseRun : runnerRun_.testCaseRuns)
         System.out.printf("\t%s\n", testCaseRun.getReport());
      System.out.println("\n");
   }

   private void execute(TestCaseRun run_) {
      run_.execute();
   }

   /**
    * if it's a File source, set the path on the infile; if it's a Database,
    * load the source table
    */
   private void setup(TestCaseRun run_, TestCaseRunnerRun runnerRun_) {
      DKSink newSink = this.setupSink(run_.plan.getSink(), runnerRun_);
      run_.plan.setSink(newSink);
   }

   private DKSink setupSink(DKSink sink_, TestCaseRunnerRun runnerRun_) {
      if (sink_.getKind() == DKSourceSink.Kind.FILE)
         return this.setupFileSink((DKWriterSink) sink_, runnerRun_);
      else
         throw new RuntimeException("unrecognized sink_.kind->${sink_.kind}");
   }

   private DKSink setupFileSink(DKWriterSink sink_, TestCaseRunnerRun runnerRun_) {
      File newSinkPath = new File(runnerRun_.dir,
         ((DKFileSink) sink_).getFile().getPath());
      _log.debug("newSinkPath->{}", newSinkPath);
      try {
         return new DKFileSink(newSinkPath.getAbsolutePath());
      }
      catch (IOException e_) {
         throw new RuntimeException(e_);
      }
   }

   private DKDBTableLoader getLoaderForSource(DKDBConnectionSource source_) {
      return new DKDBH2Loader(source_);
   }

   /**
    * make sure that plan has proper characteristics for TestCases
    */
   private void validate(TestCaseRun run_) {
      _log.debug("run_->{}", run_);
      DKSource lhsSource = run_.plan.getLhsSource();
      if (lhsSource == null)
         throw new RuntimeException(String.format("no lhsSoure for plan->%s", run_.plan));
      DKSource rhsSource = run_.plan.getRhsSource();
      if (rhsSource == null)
         throw new RuntimeException(String.format("no rhsSource for plan->%s", run_.plan));
      this.validateSource(run_.plan.getLhsSource());
      this.validateSource(run_.plan.getRhsSource());
      this.validateSink(run_.plan.getSink());
   }

   /**
    * only work with File and DB sources; if file, ensure that data file matches
    * that listed in TestCase
    */
   private void validateSource(DKSource source_) {
      _log.debug("source_->{}", source_);
      Kind kind = source_.getKind();
      if (!((kind == DKSourceSink.Kind.FILE) || (kind == DKSourceSink.Kind.DB)))
         throw new RuntimeException(
            "can only work with Sources of Kind->${[DKSourceSink.Kind.FILE,DKSourceSink.Kind.DB]}");
   }

   /**
    * only work with File and DB sources; if file, ensure that data file matches
    * that listed in TestCase
    */
   private void validateSink(DKSink sink_) {
      _log.debug("sink_->{}", sink_);
      Kind kind = sink_.getKind();
      if (!(kind == DKSourceSink.Kind.FILE))
         throw new RuntimeException(String.format(
            "can only work with Sources of Kind->%s", DKSourceSink.Kind.FILE));
      File sinkFile = ((DKFileSink) sink_).getFile();
      String sinkPath = sinkFile.getPath();
      if (!sinkPath.startsWith("."))
         throw new RuntimeException("sinkPath must be relative path starting with ./");
   }

   private List<TestCase> fetchAllTestCases(String dataPath_) {
      File dataDir = this.getDataDir(dataPath_);
      File[] planFiles = dataDir.listFiles(TEST_CASE_PLAN_FILTER);
      if (planFiles == null)
         return null;
      List<TestCase> testCases = new ArrayList<TestCase>(planFiles.length);
      for (File planFile : planFiles) {
         TestCase testCase = this.createTestCase(planFile, dataPath_);
         if (testCase != null)
            testCases.add(testCase);
      }
      return testCases;
   }

   private TestCase createTestCase(File planFile_, String dataPath_) {
      String planFileName = planFile_.getName();
      Matcher matcher = Pattern.compile(TEST_CASE_PLAN_FILE_PATTERN).matcher(planFileName);
      planFileName = dataPath_ + planFileName;
      matcher.matches();
      String numberString = matcher.group(1);
      _log.debug("numberString->{}", numberString);
      Integer number = Integer.parseInt(numberString);
      String dbSetupFileName = String.format("%stest%s.dbsetup.xml", dataPath_,
         numberString);
      _log.debug("dbSetupFileName->{}", dbSetupFileName);
      if (!DKResourceUtil.resourceExists(dbSetupFileName))
         dbSetupFileName = null;
      String name = String.format("test%s", numberString);
      String lhsSourceFileName = String.format("%stest%s.lhs.csv", dataPath_,
         numberString);
      String rhsSourceFileName = String.format("%stest%s.rhs.csv", dataPath_,
         numberString);
      String expectedFileName = String.format("%stest%s.expected.diff", dataPath_,
         numberString);
      TestCase testCase = new TestCase(number, name, null, dbSetupFileName,
         lhsSourceFileName, rhsSourceFileName, planFileName, expectedFileName);
      _log.debug("testCase->{}", testCase);
      testCase.validate();
      return testCase;
   }

   private File getDataDir(String dataPath_) {
      File dataDir = null;
      try {
         dataDir = DKResourceUtil.findResourceAsFile(dataPath_);
      }
      catch (Exception e_) {
         throw new RuntimeException(e_);
      }
      if (dataDir == null)
         throw new RuntimeException("couldn't find directory for dataPath_->$dataPath_");
      if (!dataDir.canRead())
         throw new RuntimeException("can't read dataDir->$dataDir");
      return dataDir;
   }

   public static void main(String[] args_) {
      System.out.printf("now->%s", new Date());
      System.out.printf("package->%s", TestCaseRunner.class.getPackage());
      String testCasesResourcePath = DKStringUtil.packageNameToResourcePath(TestCaseRunner.class.getPackage().getName())
         + TEST_CASE_FILE_NAME;
      System.out.printf("testCasesResourcePath->%s", testCasesResourcePath);
      AbstractXmlApplicationContext context = new ClassPathXmlApplicationContext(
         new String[] { testCasesResourcePath }, false);
      context.setClassLoader(TestCaseRunner.class.getClassLoader());
      context.refresh();
      assert (context != null);

      TestCaseRunner runner = (TestCaseRunner) context.getBean("runner");
      System.out.printf("runner->%s", runner);
      assert (runner != null);
      runner.run();
   }
}
