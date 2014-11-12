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
package org.diffkit.common.tst

import org.diffkit.common.DKCalendarSpan;
import org.diffkit.common.DKConstructor 

import groovy.util.GroovyTestCase;
import java.lang.reflect.Constructor 


/**
 * @author jpanico
 */
public class TestConstructor extends GroovyTestCase {
	
	public void testFindParameterNames(){
		
		Constructor constructor = DKCalendarSpan.class.getConstructors()[0]
		assert constructor
		DKConstructor dkConstructor = [constructor]
		println "dkConstructor->$dkConstructor"
		assert dkConstructor._parameterNames
		assert dkConstructor._parameterNames.length == 2
		assert dkConstructor._parameterNames[0] == "quantity_"
		assert dkConstructor._parameterNames[1] == "unit_"
	}
	
}
