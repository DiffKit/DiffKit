
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
package org.diffkit.diff.conf.tst


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;


import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestCLI extends GroovyTestCase {
   
   public void testParse(){
      Options OPTIONS = []
      OptionBuilder.hasOptionalArgs(2)
      OptionBuilder.withArgName("cases,database")
//      OptionBuilder.withValueSeparator((char)';')
      OptionBuilder.withDescription('hello')
      OPTIONS.addOption(OptionBuilder.create('option'))
      
      CommandLineParser parser = new PosixParser()
      CommandLine line = parser.parse(OPTIONS, (String[])['-option','arg1'])
      println "line->$line"
      assert line.hasOption('option')
      assert line.getOptionValue('option') == 'arg1'
      
      line = parser.parse(OPTIONS, (String[])['-option'])
      println "line->$line"
      assert line.hasOption('option')
      assert !line.getOptionValue('option')
      
      line = parser.parse(OPTIONS, (String[])['-option','cases=1,2,3','databases=oracle,h2'])
      println "line->$line"
      assert line.hasOption('option')
      assert line.getOptionValues('option') == (String[])['cases=1,2,3','databases=oracle,h2']
      
      
   }
}

