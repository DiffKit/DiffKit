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

import java.util.Comparator;
import java.util.Map;

import org.apache.commons.collections.OrderedMap;

import org.diffkit.diff.engine.DKContext.UserKey;

/**
 * The instructions for how to carry out a complete comparison of one table
 * (lhs) to another table (rhs)
 * 
 * @author jpanico
 */
public interface DKTableComparison {

   public DKDiff.Kind getKind();

   public long getMaxDiffs();

   public DKTableModel getLhsModel();

   public DKTableModel getRhsModel();

   public DKColumnComparison[] getMap();

   /**
    * indices into the array from getMap()
    */
   public int[] getDiffIndexes();

   /**
    * indices into the columns of respective TableModels
    */
   public int[][] getDisplayIndexes();

   /**
    * @return a lhs,rhs comparator
    */
   public Comparator<Object[]> getRowComparator();

   public Object[] getRowKeyValues(Object[] aRow_, int sideIdx_);

   public String getColumnName(int columnStep_);

   /**
    * @param lhs_
    *           row
    * @param rhs_
    *           row
    * @return OrderedMap ordered, as best as possible, according to
    *         _displayIndexes. keys are String; values are String
    */
   public OrderedMap getRowDisplayValues(Object[] lhs_, Object[] rhs_);

   /**
    * @return OrderedMap where key is String and value is String; converts nulls
    *         to "<null>"
    */
   public OrderedMap getRowDisplayValues(Object[] row_, int sideIdx_);

   public String getDescription();

   /**
    * contextual information that might be interesting for user reports
    */
   public Map<UserKey, ?> getUserDictionary();
}
