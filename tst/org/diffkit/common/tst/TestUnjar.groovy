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
package org.diffkit.common.tst



import java.io.File;
import java.net.URL;
import java.util.jar.JarInputStream;

import org.apache.commons.io.FileUtils;

import org.diffkit.common.DKUnjar;

import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestUnjar extends GroovyTestCase {
   
   public void testUnjar(){
      URL jarURL = this.getClass().classLoader.getResource('org/diffkit/common/tst/unjar_test.jar')
      println "jarURL->$jarURL"
      assert jarURL
      JarInputStream jarInStream = new JarInputStream(jarURL.openStream())
      File outDir = new File("./tstscratch/testUnjar")
      outDir.mkdirs()
      DKUnjar.unjar( jarInStream, outDir)
      
      String[] fileNames = outDir.list()
      assert fileNames
      assert fileNames.length==3
      assert fileNames == (String[])['unjar1.txt', 'unjar2.txt', 'unjar3.txt']
   }
   
   public void testUnjarWithSubstitutions(){
      URL jarURL = this.getClass().classLoader.getResource('org/diffkit/common/tst/unjar_test.jar')
      println "jarURL->$jarURL"
      assert jarURL
      JarInputStream jarInStream = new JarInputStream(jarURL.openStream())
      File outDir = new File("./tstscratch/testUnjarWithSubs")
      outDir.mkdirs()
      
      def substitutions = ['Beware':'swear', 'frumious':'frumpy']
      DKUnjar.unjar( jarInStream, outDir, substitutions)
      
      String[] fileNames = outDir.list()
      assert fileNames
      assert fileNames.length==3
      assert fileNames == (String[])['unjar1.txt', 'unjar2.txt', 'unjar3.txt']
      assert FileUtils.readFileToString(new File(outDir, fileNames[0])) == FileUtils.readFileToString(new File(outDir, fileNames[2]))
   }
}
