package org.diffkit.diff.testcase

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



import java.net.URL;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern 

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.ClassUtils 

import org.diffkit.common.DKRegexFilenameFilter;
import org.diffkit.common.DKRuntime 
import org.diffkit.common.DKUnjar;
import org.diffkit.common.DKValidate;

import org.diffkit.db.DKDatabase 
import org.diffkit.db.DKDBFlavor;
import org.diffkit.db.DKDBH2Loader 
import org.diffkit.db.DKDBTableLoader 
import org.diffkit.db.tst.DBTestSetup 
import org.diffkit.diff.conf.DKPassthroughPlan;
import org.diffkit.diff.conf.DKPlan 
import org.diffkit.diff.engine.DKSink 
import org.diffkit.diff.engine.DKSource 
import org.diffkit.diff.engine.DKSourceSink 
import org.diffkit.diff.engine.DKSourceSink.Kind
import org.diffkit.diff.sns.DKFileSink 
import org.diffkit.diff.sns.DKFileSource 
import org.diffkit.diff.sns.DKWriterSink 
import org.diffkit.util.DKFileUtil;
import org.diffkit.util.DKResourceUtil;
import org.diffkit.util.DKSpringUtil;
import org.diffkit.util.DKStringUtil 


/**
 * @author jpanico
 */
public class TestCaseRunner implements Runnable {
   
   public static final String TEST_CASE_FILE_NAME = 'testcaserunner.xml'
   public static final String TARGET_DATABASE_TOKEN = "@TargetDatabase@"
   public static final String DEFAULT_TESTCASE_DATABASE = "mem:testcase;DB_CLOSE_DELAY=-1"
   public static final String TEST18_LHS_TARGET_DATABASE_TOKEN = "@Test18LHSTargetDatabase@"
   public static final String TEST18_LHS_TARGET_DATABASE = "mem:testcase18_lhs;DB_CLOSE_DELAY=-1"
   public static final String TEST18_RHS_TARGET_DATABASE_TOKEN = "@Test18RHSTargetDatabase@"
   public static final String TEST18_RHS_TARGET_DATABASE = "mem:testcase18_rhs;DB_CLOSE_DELAY=-1"
   public static final String TEST_CASE_PLAN_FILE_PATTERN = 'test(\\d*)\\.plan\\.xml'
   public static final DKRegexFilenameFilter TEST_CASE_PLAN_FILTER = new DKRegexFilenameFilter(TEST_CASE_PLAN_FILE_PATTERN);
   private static final List TEST_CASE_DATA_SUFFIXES = ['xml', 'diff', 'csv', 'txt', 'exception']
   private static final FileFilter TEST_CASE_DATA_FILTER = new SuffixFileFilter(TEST_CASE_DATA_SUFFIXES)
   private static final String TEST_CASE_DATA_ARCHIVE_NAME = "testcasedata.jar"
   
   private List<Integer> _testCaseNumbers
   private List<DKDBFlavor> _flavors
   private String _dataPath
   private List<TestCase> _allTestCases
   private final Logger _log = LoggerFactory.getLogger(this.getClass())
   
   
   /**
    * @param dataPath_ expressed as a classpath resource
    */
   public TestCaseRunner(List<Integer> testCaseNumbers_, List<DKDBFlavor> flavors_){
      _log.debug("testCaseNumbers_->{}",testCaseNumbers_)
      _log.debug("flavors_->{}",flavors_)
      _testCaseNumbers = testCaseNumbers_
      _flavors = flavors_
      _dataPath =  getDefaultDataPath()
      DKResourceUtil.prependResourceDir(DKRuntime.getInstance().getConfDir());
      DKValidate.notNull(_dataPath)
   }
   
   public void run(){
      _log.debug("_flavors->{}",_flavors)
      if(!_flavors)
         _flavors = [DKDBFlavor.H2]
      _flavors.each { this.run(it) }
   }
   
   public void run(DKDBFlavor flavor_){
      _log.info("flavor_->{}",flavor_)
      def runnerRun = this.setupRunnerRun(flavor_)
      if(!runnerRun) {
         _log.info("can't setup runnerRun; exiting.")
         return
      }
      _allTestCases = this.fetchAllTestCases(runnerRun.dir, flavor_)
      _log.debug("_allTestCases->{}",_allTestCases)
      def List<TestCase> testCases = null
      if( !_testCaseNumbers) {
         _log.info("no TestCase numbers specified; will use all TestCases found at _dataPath->$_dataPath")
         testCases = _allTestCases
      }
      else
         testCases = _allTestCases.findAll {
            _testCaseNumbers.contains(it.id)
         }
      
      if(!testCases) {
         _log.info("could not find any TestCases to run; exiting.")
         return
      }
      
      Collections.sort(testCases)
      _log.info("testCases->{}",testCases)
      testCases.each {
         this.setupAndExecute(it, runnerRun)
      }
      this.report(runnerRun)
      if(runnerRun.failed)
         System.exit(-1)
      System.exit(0)
   }
   
   /**
    * copy the data files into the TestCaseRunnerRun working directory
    */
   private TestCaseRunnerRun setupRunnerRun(DKDBFlavor flavor_){
      _log.debug("flavor_->{}",flavor_)
      if(!validateFlavor(flavor_)) {
         DKRuntime.getInstance().getUserLog().info("couldn't validate flavor->{}, skipping.",flavor_)
         return
      }
      TestCaseRunnerRun runnerRun = [new File('./')]
      def classLoader = this.class.classLoader
      _log.info("classLoader->{}",classLoader)
      URL dataPathUrl = classLoader.getResource(_dataPath)
      _log.info("dataPathUrl->{}",dataPathUrl)
      def substitutionMap = [:]
      substitutionMap.put(TARGET_DATABASE_TOKEN, DEFAULT_TESTCASE_DATABASE)
      substitutionMap.put(TEST18_LHS_TARGET_DATABASE_TOKEN, TEST18_LHS_TARGET_DATABASE)
      substitutionMap.put(TEST18_RHS_TARGET_DATABASE_TOKEN, TEST18_RHS_TARGET_DATABASE)
      if(dataPathUrl.toExternalForm().startsWith("jar:")){
         String testDataArchiveResourcePath = _dataPath + TEST_CASE_DATA_ARCHIVE_NAME
         _log.info("testDataArchiveResourcePath->{}",testDataArchiveResourcePath)
         InputStream archiveInputStream = classLoader.getResourceAsStream(testDataArchiveResourcePath)
         _log.info("archiveInputStream->{}",archiveInputStream)
         if(!archiveInputStream) {
            _log.error("couldn't find archive at path->{}",testDataArchiveResourcePath)
            return null
         }
         JarInputStream jarInputStream = new JarInputStream(archiveInputStream)
         DKUnjar.unjar( jarInputStream, runnerRun.dir, substitutionMap)
      }
      else {
         File dataDir = [dataPathUrl.toURI()]
         DKFileUtil.copyDirectory( dataDir, runnerRun.dir, TEST_CASE_DATA_FILTER, substitutionMap)
      }
      this.installDBConfFiles( flavor_, runnerRun)
      DKResourceUtil.appendResourceDir(runnerRun.dir)
      return runnerRun
   }
   
   private void installDBConfFiles(DKDBFlavor flavor_, TestCaseRunnerRun runnerRun_){
      if(!flavor_ || (flavor_==DKDBFlavor.H2))
         return
      def confFiles = this.getConfFiles(flavor_)
      if(!confFiles) 
         throw new RuntimeException(String.format("couldn't get conf files for flavor_->%s",flavor_))
      confFiles.each {
         String destinationFileName = it.name.replace( '.'+flavor_.toString().toLowerCase(), '' ); 
         FileUtils.copyFile(it, new File(runnerRun_.dir, destinationFileName))
      }
   }
   
   private boolean validateFlavor(DKDBFlavor flavor_) {
      if(!flavor_)
         return false
      if(flavor_==DKDBFlavor.H2)
         return true
      def actualConfFiles = this.getConfFiles(flavor_)
      if(!actualConfFiles)
         return false;
      //N.B. actualConfFiles are absolute filePaths, which require a 'file:' prefix in Spring land
      def confFilePaths = actualConfFiles.collect { 'file:'+ it.absolutePath }
      _log.debug("confFilePaths->{}",confFilePaths)
      def connectionInfo = DKSpringUtil.getBean("connectionInfo", (String[])confFilePaths, this.class.classLoader)
      _log.debug("connectionInfo->{}",connectionInfo)
      if(!connectionInfo){
         DKRuntime.getInstance().getUserLog().error("could not get connectionInfo from bean conf files->{}, skipping.",confFilePaths)
         return false
      }
      DKDatabase database = [connectionInfo]
      boolean canConnect = database.canConnect()
      if(!canConnect){
         DKRuntime.getInstance().getUserLog().error("can't connect to database for connectionInfo->{}, skipping.",connectionInfo)
         return false
      }
      return true
   }
   
   private File[] getConfFiles(DKDBFlavor flavor_){
      if(!flavor_)
         return null
      if(flavor_==DKDBFlavor.H2)
         return null
      Logger _userLog = DKRuntime.getInstance().getUserLog()
      def expectedConfFileNames = this.getExpectedConfFileNames(flavor_)
      _log.debug("expectedConfFileNames->{}", Arrays.toString(expectedConfFileNames))
      if(!expectedConfFileNames) {
         DKRuntime.getInstance().getUserLog().error("no expectedConfFileNames for flavor->{}, skipping.",flavor_)
         return null
      }
      def actualConfFiles = DKResourceUtil.findResourcesAsFiles(expectedConfFileNames)
      _log.debug("actualConfFiles->{}",actualConfFiles)
      if(!actualConfFiles || (actualConfFiles.length < expectedConfFileNames.length)) {
         DKRuntime.getInstance().getUserLog().error("could not find all expected conf files for flavor->{}, check your confDirctory->{}.",
               Arrays.toString(expectedConfFileNames),
               DKRuntime.getInstance().getConfDir())
         return null
      }
      return actualConfFiles
   }
   
   /**
    * this is the minimum set-- if there are more, that's ok
    */
   private String[] getExpectedConfFileNames(DKDBFlavor flavor_){
      def expectedFileTemplates = ["dbConnectionInfo.{flavor}.xml", "test18.lhs.dbConnectionInfo.{flavor}.xml", "test18.rhs.dbConnectionInfo.{flavor}.xml"]
      return (String[]) expectedFileTemplates.collect { it.replace('{flavor}',flavor_.toString().toLowerCase())}
   }
   
   
   private void setupDB(TestCase testCase_) {
      DBTestSetup.setupDB(testCase_.dbSetupFile, testCase_.getConnectionInfoFiles(), 
            testCase_.lhsSourceFile, testCase_.rhsSourceFile)
   }
   
   /**
    * perform all of the dbSetup for all TestCases found in file_
    */
   public void setupDB(File dir_) {
      def testCases = this.fetchAllTestCases(dir_)
      _log.debug("testCases->{}", testCases)
      if (!testCases)
         return 
      testCases.each { this.setupDB(it) }
   }
   
   private DKPlan getPlan(TestCase testCase_){
      def configFiles = [testCase_.planFile] 
      configFiles.addAll(testCase_.connectionInfoFiles)
      String[] configFilePaths = configFiles.collect { 'file:'+ it.absolutePath }
      def plan = DKSpringUtil.getBean("plan", configFilePaths ,this.class.getClassLoader())
      _log.debug("plan->{}",plan)
      if(!plan)
         throw new RuntimeException("no 'plan' bean in planFile->${testCase_.planFile}")
      return new DKPassthroughPlan(plan)
   }
   
   private void report(TestCaseRunnerRun runnerRun_){
      println "\nTestCaseRunnerRun -- ${runnerRun_.dir}\n=================="
      println "\n\tTestCaseRuns\n\t------------"
      runnerRun_.testCaseRuns.each { println "\t${it.report}" }
      println "\n"
   }
   
   private void setupAndExecute(TestCase testCase_, TestCaseRunnerRun runnerRun_){
      _log.info("testCase_->{}",testCase_.description)
      DKPlan plan = null
      Exception exception = null
      try{
         this.setupDB( testCase_)
         plan = this.getPlan(testCase_)
      }
      catch(Exception e_){
         _log.info(null,e_)
         exception = e_
      }
      TestCaseRun run = new TestCaseRun(testCase_, plan)
      _log.debug("run->{}",run)
      runnerRun_.addRun( run)
      if(exception){
         run.setException(exception)
         return
      }
      try{
         this.validate(run)
         this.setup(run, runnerRun_)
         run.diff()
         run.setIsExecuted(true)
      }
      catch(Exception e_){
         _log.info(null,e_)
         run.setException(exception)
      }
   }
   
   private void setup(TestCaseRun run_, TestCaseRunnerRun runnerRun_){
      run_.plan.lhsSource = this.setupSource( run_.plan.lhsSource, runnerRun_)
      run_.plan.rhsSource = this.setupSource( run_.plan.rhsSource, runnerRun_)
      run_.plan.sink = this.setupSink( run_.plan.sink, runnerRun_)
   }
   
   private DKSource setupSource(DKSource source_, TestCaseRunnerRun runnerRun_){
      return source_
   }
   
   private DKSink setupSink(DKSink sink_, TestCaseRunnerRun runnerRun_){
      if(sink_.kind == DKSourceSink.Kind.FILE)
         return this.setupFileSink( sink_, runnerRun_)
      else
         throw new RuntimeException("unrecognized sink_.kind->${sink_.kind}")
   }
   
   private DKSource setupFileSource(DKFileSource source_, TestCaseRunnerRun runnerRun_){
      File newSourcePath = [runnerRun_.dir, source_.file.path]
      _log.debug("newSourcePath->{}",newSourcePath)
      return new DKFileSource(newSourcePath.absolutePath, source_.model, 
      source_.keyColumnNames, source_.readColumnIdxs, source_.delimeter, 
      source_.isSorted, source_.validateLazily )
   }
   
   private DKSink setupFileSink(DKWriterSink sink_, TestCaseRunnerRun runnerRun_){
      File newSinkPath = [runnerRun_.dir, sink_.file.path ]
      _log.debug("newSinkPath->{}",newSinkPath)
      return new DKFileSink(newSinkPath.absolutePath, sink_)
   }
   
   private DKDBTableLoader getLoaderForSource(DKDatabase source_){
      return new DKDBH2Loader(source_)
   }
   
   /**
    * make sure that plan has proper characteristics for TestCases
    */
   private void validate(TestCaseRun run_){
      _log.debug("run_->{}",run_)
      def lhsSource = run_.plan.lhsSource
      if(!lhsSource)
         throw new RuntimeException(String.format("no lhsSoure for plan->%s", run_.plan))
      def rhsSource = run_.plan.rhsSource
      if(!rhsSource)
         throw new RuntimeException(String.format("no rhsSource for plan->%s", run_.plan))
      this.validateSource(run_.plan.lhsSource)
      this.validateSource(run_.plan.rhsSource)
      this.validateSink(run_.plan.sink)
   }
   
   /**
    * only work with File and DB sources; if file, ensure that data file matches that listed in TestCase
    */
   private void validateSource(DKSource source_){
      _log.debug("source_->{}",source_)
      Kind kind = source_.kind
      if(!((kind == DKSourceSink.Kind.FILE)||(kind == DKSourceSink.Kind.DB)))
         throw new RuntimeException("can only work with Sources of Kind->${[DKSourceSink.Kind.FILE,DKSourceSink.Kind.DB]}")
   }
   /**
    * only work with File and DB sources; if file, ensure that data file matches that listed in TestCase
    */
   private void validateSink(DKSink sink_){
      _log.debug("sink_->{}",sink_)
      Kind kind = sink_.kind
      if(!(kind == DKSourceSink.Kind.FILE))
         throw new RuntimeException("can only work with Sources of Kind->${[DKSourceSink.Kind.FILE]}")
      File sinkFile = sink_.file
      if(!DKFileUtil.isRelative(sinkFile) )
         throw new RuntimeException("sinkPath '$sinkFile' must be relative path!")
   }
   
   public  List<TestCase> fetchAllTestCases(File dir_, DKDBFlavor flavor_){
      File[] planFiles = dir_.listFiles(TEST_CASE_PLAN_FILTER)
      if(!planFiles)
         return null
      def testCases = new ArrayList(planFiles.length)
      planFiles.each {
         def testCase = this.createTestCase(it, dir_, flavor_)
         if(testCase)
            testCases.add(testCase)
      }
      return testCases
   }
   
   private TestCase createTestCase(File planFile_, File dir_, DKDBFlavor flavor_){
      def matcher = Pattern.compile(TEST_CASE_PLAN_FILE_PATTERN).matcher(planFile_.name)
      matcher.matches()
      def numberString = matcher.group(1)
      _log.debug("numberString->{}",numberString)		
      def number = Integer.parseInt(numberString)
      def dbSetupFile = new File(dir_, "test${numberString}.dbsetup.xml")
      _log.debug("dbSetupFile->{}",dbSetupFile)
      if(!dbSetupFile.exists())
         dbSetupFile = null
      def lhsConnectionInfoFile = new File(dir_, "test${numberString}.lhs.dbConnectionInfo.xml")
      _log.debug("lhsConnectionInfoFile->{}",lhsConnectionInfoFile)
      if(!lhsConnectionInfoFile.exists())
         lhsConnectionInfoFile = null
      def rhsConnectionInfoFile = new File(dir_, "test${numberString}.rhs.dbConnectionInfo.xml")
      _log.debug("rhsConnectionInfoFile->{}",rhsConnectionInfoFile)
      if(!rhsConnectionInfoFile.exists())
         rhsConnectionInfoFile = null
      def flavorString = flavor_.toString().toLowerCase()
      def expectedFile = new File(dir_, "test${numberString}.expected.${flavorString}.diff")
      _log.debug("expectedFile->{}",expectedFile)
      if(!expectedFile.exists())
         expectedFile = new File(dir_, "test${numberString}.expected.diff")
      
      def name = "test$numberString"
      def lhsSourceFile = new File(dir_, "test${numberString}.lhs.csv")
      def rhsSourceFile = new File(dir_, "test${numberString}.rhs.csv")
      def exceptionFile = new File(dir_, "test${numberString}.exception")
      def testCase= new TestCase(number,name, null, dbSetupFile, lhsSourceFile, 
            rhsSourceFile, lhsConnectionInfoFile, rhsConnectionInfoFile, 
            planFile_, expectedFile, exceptionFile)
      return testCase
   }
   
   private static String getDefaultDataPath(){
      return DKStringUtil.packageNameToResourcePath(ClassUtils.getPackageName(TestCaseRunner.class) )
   }
   
   public static void main(String[] args_){
      println "now->${new Date()}"
      println "class->${TestCaseRunner.class}"
      println "DiffKit home->${DKRuntime.instance.diffKitHome}"
      def testCasesResourcePath =  getDefaultDataPath() + TEST_CASE_FILE_NAME
      println "testCasesResourcePath->$testCasesResourcePath"
      def runner = DKSpringUtil.getBean( 'runner', (String[]) [ testCasesResourcePath ], TestCaseRunner.class.getClassLoader())
      println "runner->$runner"
      assert runner
      runner.run()
   }
}

