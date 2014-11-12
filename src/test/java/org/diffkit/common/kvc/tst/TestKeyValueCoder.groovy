/**
 * Copyright © 2010, Joseph Panico
 * All rights reserved.
 */
package org.diffkit.common.kvc.tst


import org.diffkit.common.kvc.DKKeyValueCoder;

import groovy.util.GroovyTestCase;

/**
 * @author jpanico
 */
public class TestKeyValueCoder extends GroovyTestCase {
	
	public void testSetValueAtPath(){
		Person grandma = new Person('grandma', false, null, null, null)
		Person mom = ['mom', false, null, null, null]
		Person dad = ['dad', false, null, null, null]
		Person me = ['me', false, null, null, null]
		
		DKKeyValueCoder.instance.setValueAtPath("mother", mom, me)
		DKKeyValueCoder.instance.setValueAtPath("father", dad, me)
		DKKeyValueCoder.instance.setValueAtPath("mother.mother", grandma, me)
		DKKeyValueCoder.instance.setValueAtPath("mother.mother.isDead", true, me)
		DKKeyValueCoder.instance.setValueAtPath("mother.mother.state", 'FL', me)
		
		assert me.father == dad
		assert me.mother == mom
		assert me.mother.mother == grandma
		assert me.mother.mother.isDead 
		assert me.mother.mother.state == 'FL' 
		
	}
	
	public void testGetValueAtPath(){
		Person grandma = new Person('grandma', true, (Person)null, (Person)null, (Set)null)
		grandma.state = 'FL'
		Person mom = ['mom', false, grandma, null, null]
		mom.state = 'CA'
		Person dad = ['dad', true, null, null, null]
		dad.state = 'NJ'
		Person me = ['me', false, mom, dad, null]
		me.state = 'MA'
		
		assert DKKeyValueCoder.instance.getValueAtPath("name", me) == 'me'
		assert ! DKKeyValueCoder.instance.getValueAtPath("isDead", me) 
		assert ! DKKeyValueCoder.instance.getValueAtPath("children", me)
		assert DKKeyValueCoder.instance.getValueAtPath("mother.name", me) == 'mom'
		assert ! DKKeyValueCoder.instance.getValueAtPath("mother.children", me) 
		assert DKKeyValueCoder.instance.getValueAtPath("mother.mother.name", me) == 'grandma'
		assert DKKeyValueCoder.instance.getValueAtPath("mother.mother.isDead", me) 
	}
	
	public void testSetValue(){
		SubTarget target = ['superA', 'superB', 'superC', 'subA', 'subB', 'subC']
		println "target->$target"
		
		DKKeyValueCoder.instance.setValue(null,null,target) 
		DKKeyValueCoder.instance.setValue('junk',null,null) 
		DKKeyValueCoder.instance.setValue('junk','junk',target) 
		
		DKKeyValueCoder.instance.setValue('superVarB','newSuperB',target) 
		assert target._superVarB == 'newSuperB'
		
		DKKeyValueCoder.instance.setValue('pointerToSuperVarC','newSuperC',target) 
		assert target.pointerToSuperVarC == 'newSuperC'
		
		DKKeyValueCoder.instance.setValue('superVarA','newSuperA',target) 
		assert target.superVarA == 'newSuperA'
	}
	
	public void testGetValue(){
		SubTarget target = ['superA', 'superB', 'superC', 'subA', 'subB', 'subC']
		println "target->$target"
		
		assert ! DKKeyValueCoder.instance.getValue(null,target) 
		assert ! DKKeyValueCoder.instance.getValue('junk',null) 
		assert ! DKKeyValueCoder.instance.getValue('junk',target) 
		assert ! DKKeyValueCoder.instance.getValue('superVarA',null) 
		assert DKKeyValueCoder.instance.getValue('superVarA',target)  == 'superA'
		assert DKKeyValueCoder.instance.getValue('superVarB',target)  == 'superB'
		assert DKKeyValueCoder.instance.getValue('pointerToSuperVarC',target)  == 'superC'
		
		assert DKKeyValueCoder.instance.getValue('subVarA',target)  == 'subA'
		assert DKKeyValueCoder.instance.getValue('subVarB',target)  == 'subB'
		assert DKKeyValueCoder.instance.getValue('subVarC',target)  == 'subC'
		assert DKKeyValueCoder.instance.getValue('pointerToSubVarC',target)  == 'subC'
	}
	
	public void testFirstElement(){
		assert ! DKKeyValueCoder.firstKeyPathElement(null) 
		assert DKKeyValueCoder.firstKeyPathElement("in this age") == 'in this age'
		assert DKKeyValueCoder.firstKeyPathElement("time.out") == 'time'
		assert ! DKKeyValueCoder.firstKeyPathElement(".") 
		assert ! DKKeyValueCoder.firstKeyPathElement("..") 
		assert DKKeyValueCoder.firstKeyPathElement(".hello.") == 'hello'
		assert DKKeyValueCoder.firstKeyPathElement("a.regular.normal.path.hello.") == 'a'
	}
	
	public void testLastElement(){
		assert ! DKKeyValueCoder.lastKeyPathElement(null) 
		assert DKKeyValueCoder.lastKeyPathElement("in this age") == 'in this age'
		assert DKKeyValueCoder.lastKeyPathElement("time.out") == 'out'
		assert ! DKKeyValueCoder.lastKeyPathElement(".") 
		assert ! DKKeyValueCoder.lastKeyPathElement("..") 
		assert DKKeyValueCoder.lastKeyPathElement(".hello.") == 'hello'
		assert DKKeyValueCoder.lastKeyPathElement("a.regular.normal.path.hello.") == 'hello'
	}
	
	public void testPathByRemovingFirst(){
		assert ! DKKeyValueCoder.pathByRemovingFirstKeyPathElement(null) 
		assert ! DKKeyValueCoder.pathByRemovingFirstKeyPathElement("in this age") 
		assert DKKeyValueCoder.pathByRemovingFirstKeyPathElement("time.out") == 'out'
		assert ! DKKeyValueCoder.pathByRemovingFirstKeyPathElement(".") 
		assert ! DKKeyValueCoder.pathByRemovingFirstKeyPathElement("..") 
		assert ! DKKeyValueCoder.pathByRemovingFirstKeyPathElement(".hello.") 
		assert DKKeyValueCoder.pathByRemovingFirstKeyPathElement("a.regular.normal.path.hello.") == 'regular.normal.path.hello'
	}
	
	public void testPathByRemovingLast(){
		assert ! DKKeyValueCoder.pathByRemovingLastKeyPathElement(null) 
		assert !DKKeyValueCoder.pathByRemovingLastKeyPathElement("in this age") 
		assert DKKeyValueCoder.pathByRemovingLastKeyPathElement("time.out") == 'time'
		assert !DKKeyValueCoder.pathByRemovingLastKeyPathElement(".") 
		assert ! DKKeyValueCoder.pathByRemovingLastKeyPathElement("..") 
		assert ! DKKeyValueCoder.pathByRemovingLastKeyPathElement(".hello.") 
		assert DKKeyValueCoder.pathByRemovingLastKeyPathElement("a.regular.normal.path.hello.") == 'a.regular.normal.path'
		
	}
	
	public void testKeyPathElements(){
		assert ! DKKeyValueCoder.keyPathElements(null) 
		assert DKKeyValueCoder.keyPathElements("in this age") == ((String[])['in this age'])
		assert DKKeyValueCoder.keyPathElements("time.out") == ((String[])['time','out'])
		assert ! DKKeyValueCoder.keyPathElements(".")
		assert ! DKKeyValueCoder.keyPathElements("..")
		assert DKKeyValueCoder.keyPathElements(".hello.") == ((String[])['hello'])
		assert DKKeyValueCoder.keyPathElements("a.regular.normal.path.hello.") == ((String[])['a','regular','normal','path','hello' ])
		
	}
	
	public void testKeyPathElementCount(){
		assert DKKeyValueCoder.keyPathElementCount(null) == 0 
		assert DKKeyValueCoder.keyPathElementCount("in this age") == 1 
		assert DKKeyValueCoder.keyPathElementCount("time.out") == 2 
		assert DKKeyValueCoder.keyPathElementCount(".") == 0 
		assert DKKeyValueCoder.keyPathElementCount("..") == 0 
		assert DKKeyValueCoder.keyPathElementCount(".hello.") == 1
		assert DKKeyValueCoder.keyPathElementCount("a.regular.normal.path.hello.") == 5
		
	}
	
}
