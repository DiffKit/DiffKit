/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.common.kvc.tst;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jpanico
 * 
 */
public class Person {

   public enum Gender {
      MALE, FEMALE;
   }

   private String _name;
   private Gender _gender;
   private Boolean _isDead;
   private Person _mother;
   private Person _father;
   private LinkedHashSet<Person> _children;
   private String _state;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   public Person() {
   }

   public Person(String name_, Boolean isDead_, Person mother_, Person father_,
                 Set<Person> children_) {
      _name = name_;
      _isDead = isDead_;
      _mother = mother_;
      _father = father_;
      _children = (children_ != null ? new LinkedHashSet<Person>(children_)
         : new LinkedHashSet<Person>());
   }

   public Object getId() {
      return _name;
   }

   public String getName() {
      return _name;
   }

   public Gender getGender() {
      return _gender;
   }

   public Person getMother() {
      return _mother;
   }

   public void setMother(Person mother_) {
      _mother = mother_;
   }

   public Person getFather() {
      return _father;
   }

   public String getState() {
      return _state;
   }

   public void setState(String state_) {
      _state = state_;
   }

   public Boolean getIsDead() {
      return _isDead;
   }

   public void logTest() {
      _log.info("test");
   }

   public String toString() {
      return ReflectionToStringBuilder.toString(this);
   }
}
