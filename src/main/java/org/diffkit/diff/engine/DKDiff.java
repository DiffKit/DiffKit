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
package org.diffkit.diff.engine;

import org.apache.commons.collections.OrderedMap;

/**
 * @author jpanico
 */
public interface DKDiff extends Comparable<DKDiff> {
   public enum Kind {
      ROW_DIFF, COLUMN_DIFF, BOTH;
   }

   public Object[] getRowKeyValues();

   public OrderedMap getRowDisplayValues();

   public DKTableComparison getTableComparison();

   public Kind getKind();

   public long getRowStep();

   public long getColumnStep();

   /**
    * convenience that formats RowDisplayValues
    */
   public String getRowDisplayString();

}
