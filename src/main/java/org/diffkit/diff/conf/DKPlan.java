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

import org.diffkit.diff.engine.DKSink;
import org.diffkit.diff.engine.DKSource;
import org.diffkit.diff.engine.DKTableComparison;

/**
 * @author jpanico
 */
public interface DKPlan {

   public DKSource getLhsSource();

   public void setLhsSource(DKSource source_);

   public DKSource getRhsSource();

   public void setRhsSource(DKSource source_);

   public DKSink getSink();

   public void setSink(DKSink sink_);

   public DKTableComparison getTableComparison();

}
