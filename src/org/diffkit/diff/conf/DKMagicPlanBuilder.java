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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;
import org.diffkit.db.DKDBConnectionInfo;
import org.diffkit.db.DKDatabase;

/**
 * @author jpanico
 */
public class DKMagicPlanBuilder {

   private static final Class<?>[] NON_CACHEABLE_CLASSES = { DKDBConnectionInfo.class,
      DKDatabase.class };

   private final DKMagicPlan _providedPlan;
   private final Map<String, Object> _resolutionCache = new HashMap<String, Object>();
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   public DKMagicPlanBuilder(DKMagicPlan providedPlan_) {
      _providedPlan = providedPlan_;
      DKValidate.notNull(_providedPlan);
   }

   public DKPassthroughPlan build() throws IllegalAccessException,
      InstantiationException, InvocationTargetException {
      DKMagicDependency<DKPassthroughPlan> planDependency = new DKMagicDependency<DKPassthroughPlan>(
         null, null, DKPassthroughPlan.class);
      this.resolve(planDependency);
      return planDependency.getResolution();
   }

   @SuppressWarnings("unchecked")
   private void resolve(DKMagicDependency<?> target_) throws IllegalAccessException,
      InstantiationException, InvocationTargetException {
      _log.debug("target_->{}", target_);
      if (target_ == null)
         return;
      Object cachedResolution = this.getCachedResolution(target_);
      if (cachedResolution != null) {
         if (cachedResolution == ObjectUtils.NULL)
            cachedResolution = null;
         target_.resolve(cachedResolution);
         return;
      }

      Object resolution = null;
      DKMagicPlanRule rule = this.getRule(target_);
      _log.debug("rule->{}", rule);
      if (rule != null) {
         resolution = this.applyRule(rule, target_);
         if (resolution instanceof Class) {
            target_.refine((Class) resolution);
            _log.debug("refined dependency->{}", target_);
            this.resolve(target_);
            return;
         }
         else {
            target_.resolve(resolution);
            _log.debug("resolved dependency->{}", target_);
         }
      }
      else {
         DKMagicDependency<?>[] dependencies = null;

         try {
            dependencies = target_.getDependencies();
            _log.debug("dependencies->{}", Arrays.toString(dependencies));
            if (dependencies != null) {
               for (DKMagicDependency<?> dependency : dependencies)
                  try {
                     this.resolve(dependency);
                  }
                  catch (Exception e_) {
                     throw new RuntimeException(String.format(
                        "Could not resolve automatically; need rule for dependency->%s",
                        dependency), e_);
                  }
            }
            resolution = target_.resolve();
         }
         catch (Exception e_) {
            throw new RuntimeException(String.format(
               "Could not resolve automatically; need rule for dependency->%s", target_),
               e_);
         }
         _log.debug("resolved dependency->{}", target_);
      }
      _log.debug("dependency->{} resolution->{}", target_, resolution);
      if (resolution == null)
         resolution = ObjectUtils.NULL;
      this.encacheResolution(target_, resolution);
   }

   private Object getCachedResolution(DKMagicDependency<?> target_) {
      _log.debug("key->{} cachedResolution->{}", this.getCacheKey(target_),
         _resolutionCache.get(this.getCacheKey(target_)));
      return _resolutionCache.get(this.getCacheKey(target_));
   }

   private void encacheResolution(DKMagicDependency<?> target_, Object resolution_) {
      if (!this.isCacheable(target_.getTargetClass()))
         return;
      _log.debug("key->{} resolution->{}", this.getCacheKey(target_), resolution_);
      _resolutionCache.put(this.getCacheKey(target_), resolution_);
   }

   private boolean isCacheable(Class<?> targetClass_) {
      if (ArrayUtils.contains(NON_CACHEABLE_CLASSES, targetClass_))
         return false;
      String targetPackageName = ClassUtils.getPackageName(targetClass_);
      if (targetPackageName.startsWith("org.diffkit"))
         return true;
      return false;
   }

   private String getCacheKey(DKMagicDependency<?> target_) {
      return String.format("%s.%s",
         ClassUtils.getShortClassName(target_.getOriginalTargetClass()),
         target_.getParentConstructorParmName());
   }

   private Object applyRule(DKMagicPlanRule rule_, DKMagicDependency<?> target_) {
      Object resolution = rule_.resolve(target_, _providedPlan);
      _log.debug("resolution->{}", resolution);
      return resolution;
   }

   private DKMagicPlanRule getRule(DKMagicDependency<?> dependency_) {
      List<DKMagicPlanRule> allApplicableRules = this.getRulesApplyingTo(dependency_);
      _log.debug("dependency->{} allApplicableRules->{}", dependency_, allApplicableRules);
      if ((allApplicableRules == null) || (allApplicableRules.isEmpty()))
         return null;

      // find the the exclusive rules that apply to dependency_
      List<DKMagicPlanRule> exclusiveRules = DKMagicPlanRule.getExclusive(allApplicableRules);
      // ensure there is at most 1
      int exclusiveRuleCount = (exclusiveRules == null) ? 0 : exclusiveRules.size();
      if (exclusiveRuleCount > 1)
         throw new RuntimeException(String.format(
            "ambiguous rule set; found more than one rule to handle dependency->%s:%s",
            dependency_, exclusiveRules));
      if (exclusiveRuleCount == 1)
         return exclusiveRules.get(0);
      // if no exclusive rules, look for non-exclusive rules
      List<DKMagicPlanRule> nonExclusiveRules = DKMagicPlanRule.getNonExclusive(allApplicableRules);
      // ensure there is at most 1
      int nonExclusiveRuleCount = (nonExclusiveRules == null) ? 0
         : nonExclusiveRules.size();
      if (nonExclusiveRuleCount > 1)
         throw new RuntimeException(String.format(
            "ambiguous rule set; found more than one rule to handle dependency->%s:%s",
            dependency_, nonExclusiveRules));
      return nonExclusiveRules.get(0);
   }

   private List<DKMagicPlanRule> getRulesApplyingTo(DKMagicDependency<?> dependency_) {
      List<DKMagicPlanRule> rulesApplying = new ArrayList<DKMagicPlanRule>();
      for (DKMagicPlanRule rule : DKMagicPlanRules.RULES) {
         if (rule.applies(dependency_, _providedPlan)) {
            _log.debug("rule->{} applies to dependency->{}", rule, dependency_);
            rulesApplying.add(rule);
         }
      }
      return rulesApplying;
   }

}
