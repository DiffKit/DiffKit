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
package org.diffkit.diff.conf;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;
import org.diffkit.common.kvc.DKKeyValueCoder;

/**
 * @author jpanico
 */
public class DKMagicPlanRule {

   private final String _name;
   private final String _description;
   private final Class<?> _targetDependentClass;
   private final Class<?> _targetClass;
   private final String _targetConstructorParmPath;
   private final String _magicPlanKey;
   private final boolean _exclusive;
   private final RuleImplementation _implementation;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   public DKMagicPlanRule(String name_, String description_,
                          Class<?> targetDependentClass_,
                          String targetConstructorParmPath_, String magicPlanKey_,
                          boolean exclusive_, RuleImplementation implementation_) {
      this(name_, description_, targetDependentClass_, null, targetConstructorParmPath_,
         magicPlanKey_, exclusive_, implementation_);
   }

   public DKMagicPlanRule(String name_, String description_,
                          Class<?> targetDependentClass_, Class<?> targetClass_,
                          String targetConstructorParmPath_, String magicPlanKey_,
                          boolean exclusive_, RuleImplementation implementation_) {
      _name = name_;
      _description = description_;
      _targetDependentClass = targetDependentClass_;
      _targetClass = targetClass_;
      _targetConstructorParmPath = targetConstructorParmPath_;
      _magicPlanKey = magicPlanKey_;
      _exclusive = exclusive_;
      _implementation = implementation_;
      DKValidate.notNull(_name, _targetConstructorParmPath, _implementation);
      _implementation.setRule(this);
   }

   public String getName() {
      return _name;
   }

   public Class<?> getTargetDependentClass() {
      return _targetDependentClass;
   }

   public Class<?> getTargetClass() {
      return _targetClass;
   }

   public String getTargetConstructorParmName() {
      return _targetConstructorParmPath;
   }

   public String getMagicPlanKey() {
      return _magicPlanKey;
   }

   public boolean getExclusive() {
      return _exclusive;
   }

   public Logger getLog() {
      return _log;
   }

   public boolean applies(DKMagicDependency<?> dependency_, DKMagicPlan providedPlan_) {
      return _implementation.applies(dependency_, providedPlan_);
   }

   public Object resolve(DKMagicDependency<?> dependency_, DKMagicPlan providedPlan_) {
      return _implementation.resolve(dependency_, providedPlan_);
   }

   /**
    * convenience method
    */
   public static List<DKMagicPlanRule> getExclusive(List<DKMagicPlanRule> target_) {
      if ((target_ == null) || (target_.isEmpty()))
         return target_;
      List<DKMagicPlanRule> exclusive = new ArrayList<DKMagicPlanRule>(target_.size());
      for (DKMagicPlanRule rule : target_) {
         if (rule.getExclusive())
            exclusive.add(rule);
      }
      return exclusive;
   }

   /**
    * convenience method
    */
   public static List<DKMagicPlanRule> getNonExclusive(List<DKMagicPlanRule> target_) {
      if ((target_ == null) || (target_.isEmpty()))
         return target_;
      List<DKMagicPlanRule> nonExclusive = new ArrayList<DKMagicPlanRule>(target_.size());
      for (DKMagicPlanRule rule : target_) {
         if (!rule.getExclusive())
            nonExclusive.add(rule);
      }
      return nonExclusive;
   }

   public String toString() {
      return String.format("%s[%s]", ClassUtils.getShortClassName(this.getClass()), _name);
   }

   public static abstract class RuleImplementation {
      protected DKMagicPlanRule _rule;
      protected final Logger _log = LoggerFactory.getLogger(this.getClass());

      public void setRule(DKMagicPlanRule rule_) {
         _rule = rule_;
      }

      public DKMagicPlanRule getRule() {
         return _rule;
      }

      /**
       * default implementation; subclasses can override <br/>
       * null for _targetDependentClass or _targetClass means wildcard for that
       * value
       */
      public boolean applies(DKMagicDependency<?> dependency_, DKMagicPlan providedPlan_) {
         boolean targetDependentClassMatches = this.targetDependentClassMatches(
            dependency_, providedPlan_);
         boolean targetClassMatches = this.targetClassMatches(dependency_, providedPlan_);
         boolean targetParmNameMatches = this.targetParmNameMatches(dependency_,
            providedPlan_);
         boolean planKeyValuePresent = this.planValuePresent(dependency_,
            providedPlan_);
         _rule.getLog().trace("_rule->{}", _rule);
         _rule.getLog().trace("targetDependentClassMatches->{}",
            targetDependentClassMatches);
         _rule.getLog().trace("targetClassMatches->{}", targetClassMatches);
         _rule.getLog().trace("targetParmNameMatches->{}", targetParmNameMatches);
         _rule.getLog().trace("planKeyValuePresent->{}", planKeyValuePresent);
         return (targetDependentClassMatches && targetClassMatches
            && targetParmNameMatches && planKeyValuePresent);
      }

      protected boolean planValuePresent(DKMagicDependency<?> dependency_,
                                            DKMagicPlan providedPlan_) {
         String planKey = _rule.getMagicPlanKey();
         if (planKey == null)
            return true;
         return (DKKeyValueCoder.getInstance().getValueAtPath(planKey, providedPlan_) != null);
      }

      protected boolean targetParmNameMatches(DKMagicDependency<?> dependency_,
                                              DKMagicPlan providedPlan_) {
         String ruleParmName = _rule.getTargetConstructorParmName();
         String dependencyParmName = (DKKeyValueCoder.isPath(ruleParmName)
            ? dependency_.getParentConstructorParmNamePath()
            : dependency_.getParentConstructorParmName());
         _rule.getLog().trace("ruleParmName->{}", ruleParmName);
         _rule.getLog().trace("dependencyParmName->{}", dependencyParmName);
         return StringUtils.equals(ruleParmName, dependencyParmName);
      }

      protected boolean targetDependentClassMatches(DKMagicDependency<?> dependency_,
                                                    DKMagicPlan providedPlan_) {
         Class<?> targetDependentClass = _rule.getTargetDependentClass();
         // implement wildcard for dependentTargetClass
         if (targetDependentClass == null)
            return true;
         Class<?> dependentClass = dependency_.getDependentTargetClass();
         if (dependentClass == null)
            return false;
         else
            return targetDependentClass.isAssignableFrom(dependentClass);

      }

      protected boolean targetClassMatches(DKMagicDependency<?> dependency_,
                                           DKMagicPlan providedPlan_) {
         Class<?> ruleTargetClass = _rule.getTargetClass();
         // implement wildcard for targetClass
         if (ruleTargetClass == null)
            return true;
         Class<?> dependencyTargetClass = dependency_.getTargetClass();
         if (dependencyTargetClass == null)
            return false;
         else
            return dependencyTargetClass.equals(ruleTargetClass);

      }

      public abstract Object resolve(DKMagicDependency<?> dependency_,
                                     DKMagicPlan providedPlan_);

   }

}
