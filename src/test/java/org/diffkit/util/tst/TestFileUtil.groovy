/**
 * Copyright 2010-2011 Joseph Panico
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
package org.diffkit.util.tst



import java.io.File;
import org.apache.commons.io.FileUtils 
import org.apache.commons.io.filefilter.SuffixFileFilter 
import org.diffkit.util.DKFileUtil 
import org.diffkit.util.DKResourceUtil 


/**
 * @author jpanico
 */
public class TestFileUtil extends GroovyTestCase {
   
   /**
    * does it work from relative paths, or just absolute?
    */
   public void testCanRead() {
      assert DKFileUtil.canReadFilePaths((String[]) ['conf/logback.xml'])
   }
   
   public void testPrepend() {
      File sourceTarget = DKResourceUtil.findResourceAsFile("org/diffkit/util/tst/prepend_target.txt")
      assert sourceTarget
      File testTarget = ['./prependTest.tst']
      FileUtils.copyFile( sourceTarget, testTarget)
      def prependString = 'prepend\nprepend\nprepend\n---\n'
      DKFileUtil.prepend( prependString, testTarget)
      
      def sourceText = FileUtils.readFileToString(sourceTarget)
      def prependedText = FileUtils.readFileToString(testTarget)
      
      assert prependedText == prependString + sourceText
   }
   
   public void testCopyWithSubstitution() {
      File sourceFile = DKResourceUtil.findResourceAsFile("org/diffkit/util/tst/copyWithSubstitution_target.txt")
      assert sourceFile
      File copiedFile = ['./copyWithSubstitutionTest.tst']
      def substitutions = ['Beware':'swear', 'frumious':'frumpy']
      DKFileUtil.copyFile( sourceFile, copiedFile, substitutions)
      
      File expectedFile = DKResourceUtil.findResourceAsFile("org/diffkit/util/tst/copyWithSubstitution_expected.txt")
      def expectedText = FileUtils.readFileToString(expectedFile)
      def copiedText = FileUtils.readFileToString(copiedFile)
      
      assert copiedText == expectedText
   }
   
   public void testCopyDirectory() {
      File sourceFile = DKResourceUtil.findResourceAsFile("org/diffkit/util/tst/copyWithSubstitution_target.txt")
      assert sourceFile
      File sourceDirectory = sourceFile.getParentFile()
      assert sourceDirectory
      assert sourceDirectory.isDirectory()
      
      FilenameFilter filenameFilter= new SuffixFileFilter(".txt")
      File[] sourceFiles = sourceDirectory.listFiles((FilenameFilter)filenameFilter)
      assert sourceFiles
      assert sourceFiles.length == 3
      
      File destDir = ['./']
      def substitutions = ['Beware':'swear', 'frumious':'frumpy']
      DKFileUtil.copyDirectory( sourceDirectory, destDir, filenameFilter,substitutions)
      File[] destFiles = destDir.listFiles((FilenameFilter)filenameFilter)
      assert destFiles
      assert destFiles.length == 3
      
      File expectedFile = DKResourceUtil.findResourceAsFile("org/diffkit/util/tst/copyWithSubstitution_expected.txt")
      def expectedText = FileUtils.readFileToString(expectedFile)
      def copiedFile = new File(destDir, sourceFile.name )
      def copiedText = FileUtils.readFileToString(copiedFile)
      
      assert copiedText == expectedText
   }
   
   public void testIsRelative(){
      println "separtor->${File.separator}"
      println "path->${new File('./')}"
      assert DKFileUtil.isRelative(new File("."))
      assert ! DKFileUtil.isRelative(new File(""))
      assert DKFileUtil.isRelative(new File('./'))
      assert DKFileUtil.isRelative(new File("./test/"))
      assert DKFileUtil.isRelative(new File(".\\"))
      assert ! DKFileUtil.isRelative(new File("/Users/joe/tmp"))
   }
}
