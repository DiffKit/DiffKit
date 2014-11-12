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

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import org.diffkit.diff.engine.DKSink;
import org.diffkit.diff.engine.DKSource;
import org.diffkit.diff.engine.DKTableComparison;

/**
 * @author jpanico
 */
public class DKPassthroughPlan implements DKPlan {

   private DKSource _lhsSource;
   private DKSource _rhsSource;
   private DKSink _sink;
   private DKTableComparison _tableComparison;

   public DKPassthroughPlan() {
   }

   public DKPassthroughPlan(DKPlan plan_) {
      this(plan_.getLhsSource(), plan_.getRhsSource(), plan_.getSink(),
         plan_.getTableComparison());
   }

   public DKPassthroughPlan(DKSource lhsSource_, DKSource rhsSource_, DKSink sink_,
                            DKTableComparison tableComparison_) {
      _lhsSource = lhsSource_;
      _rhsSource = rhsSource_;
      _sink = sink_;
      _tableComparison = tableComparison_;
   }

   public DKSource getLhsSource() {
      return _lhsSource;
   }

   public void setLhsSource(DKSource lhsSource_) {
      _lhsSource = lhsSource_;
   }

   public DKSource getRhsSource() {
      return _rhsSource;
   }

   public void setRhsSource(DKSource rhsSource_) {
      _rhsSource = rhsSource_;
   }

   public DKSink getSink() {
      return _sink;
   }

   public void setSink(DKSink sink_) {
      _sink = sink_;
   }

   public DKTableComparison getTableComparison() {
      return _tableComparison;
   }

   public void setTableComparison(DKTableComparison tableComparison_) {
      _tableComparison = tableComparison_;
   }

   public String toString() {
      return ReflectionToStringBuilder.toString(this);
   }

}
