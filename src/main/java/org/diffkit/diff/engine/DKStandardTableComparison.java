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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKComparableComparator;
import org.diffkit.common.DKComparatorChain;
import org.diffkit.common.DKElementComparator;
import org.diffkit.common.DKValidate;
import org.diffkit.diff.engine.DKContext.UserKey;

/**
 * The instructions for how to carry out a complete comparison of one table
 * (lhs) to another table (rhs)
 * 
 * @author jpanico
 */
public class DKStandardTableComparison implements DKTableComparison {
   private final DKTableModel[] _tableModels = new DKTableModel[2];
   /**
    * the Kind of diffs to search for
    */
   private DKDiff.Kind _kind;
   private DKColumnComparison[] _map;
   /**
    * indexes into _map
    */
   private int[] _diffIndexes;
   private int[][] _displayIndexes;
   private long _maxDiffs;
   private Comparator<Object[]> _rowComparator;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   public DKStandardTableComparison(DKTableModel lhs_, DKTableModel rhs_,
                                    DKDiff.Kind kind_, DKColumnComparison[] map_,
                                    int[] diffIndexes_, int[][] displayIndexes_,
                                    long maxDiffs_) {

      _log.debug("lhs_->{}", lhs_);
      _log.debug("rhs_->{}", rhs_);
      _log.debug("kind_->{}", kind_);
      _log.debug("map_->{}", map_);
      _log.debug("diffIndexes_->{}", diffIndexes_);
      _log.debug("displayIndexes_->{}", displayIndexes_);
      _tableModels[DKSide.LEFT_INDEX] = lhs_;
      _tableModels[DKSide.RIGHT_INDEX] = rhs_;
      DKValidate.notNull(_tableModels[DKSide.LEFT_INDEX],
         _tableModels[DKSide.RIGHT_INDEX]);
      _kind = kind_;
      _map = map_;
      _diffIndexes = diffIndexes_;
      _displayIndexes = displayIndexes_;
      _rowComparator = this.buildRowComparator(_tableModels[DKSide.LEFT_INDEX],
         _tableModels[DKSide.RIGHT_INDEX]);
      _maxDiffs = maxDiffs_;
      DKValidate.notNull(_kind, _map, _rowComparator);
      this.validateComparisons(_tableModels[DKSide.LEFT_INDEX],
         _tableModels[DKSide.RIGHT_INDEX], _map);
   }

   public DKDiff.Kind getKind() {
      return _kind;
   }

   public long getMaxDiffs() {
      return _maxDiffs;
   }

   public DKTableModel getLhsModel() {
      return _tableModels[DKSide.LEFT_INDEX];
   }

   public DKTableModel getRhsModel() {
      return _tableModels[DKSide.RIGHT_INDEX];
   }

   public DKColumnComparison[] getMap() {
      return _map;
   }

   public int[] getDiffIndexes() {
      return _diffIndexes;
   }

   public int[][] getDisplayIndexes() {
      return _displayIndexes;
   }

   /**
    * @return a lhs,rhs comparator
    */
   public Comparator<Object[]> getRowComparator() {
      return _rowComparator;
   }

   @SuppressWarnings("unchecked")
   private Comparator<Object[]> buildRowComparator(DKTableModel lhs_, DKTableModel rhs_) {
      this.validateTableModels(lhs_, rhs_);
      DKColumnModel[] lhsColumns = lhs_.getColumns();
      int[] lhsKey = lhs_.getKey();
      DKColumnModel[] rhsColumns = rhs_.getColumns();
      int[] rhsKey = rhs_.getKey();
      DKComparatorChain chain = new DKComparatorChain();
      for (int i = 0; i < lhsKey.length; i++) {
         Comparator<Comparable[]> comparator = new DKElementComparator<Comparable>(
            lhsColumns[lhsKey[i]]._index, rhsColumns[rhsKey[i]]._index,
            DKComparableComparator.getInstance());
         chain.addComparator(comparator);
      }
      return chain;
   }

   /**
    * convenience method that delegates to TableModel
    */
   public Object[] getRowKeyValues(Object[] aRow_, int sideIdx_) {
      if (aRow_ == null)
         return null;
      return _tableModels[sideIdx_].getKeyValues(aRow_);
   }

   public String getColumnName(int columnStep_) {
      DKColumnComparison columnPlan = _map[_diffIndexes[columnStep_ - 1]];
      String lhColumnName = columnPlan._lhsColumn._name;
      String rhColumnName = columnPlan._rhsColumn._name;

      return (lhColumnName.equals(rhColumnName) ? lhColumnName : lhColumnName + ":"
         + rhColumnName);
   }

   /**
    * @param lhs_
    *           row
    * @param rhs_
    *           row
    * @return OrderedMap ordered, as best as possible, according to
    *         _displayIndexes. keys are String; values are String
    */
   @SuppressWarnings("unchecked")
   public OrderedMap getRowDisplayValues(Object[] lhs_, Object[] rhs_) {

      OrderedMap lhDisplayValues = this.getRowDisplayValues(lhs_, DKSide.LEFT_INDEX);
      OrderedMap rhDisplayValues = this.getRowDisplayValues(rhs_, DKSide.RIGHT_INDEX);
      if (_log.isDebugEnabled()) {
         _log.debug("lhDisplayValues->{}", lhDisplayValues);
         _log.debug("rhDisplayValues->{}", rhDisplayValues);
      }
      MapIterator lhIterator = lhDisplayValues.orderedMapIterator();
      MapIterator rhIterator = rhDisplayValues.orderedMapIterator();
      OrderedMap result = new LinkedMap();
      while (true) {
         boolean lhHasNext = lhIterator.hasNext();
         boolean rhHasNext = rhIterator.hasNext();
         if ((!lhHasNext) && (!rhHasNext))
            break;
         String lhKey = (lhHasNext ? (String) lhIterator.next() : null);
         String rhKey = (rhHasNext ? (String) rhIterator.next() : null);
         if (lhKey == null) {
            result.put(rhKey, ":" + rhDisplayValues.get(rhKey));
         }
         else if (rhKey == null) {
            result.put(lhKey, lhDisplayValues.get(lhKey) + ":");
         }
         else if (lhKey.equals(rhKey)) {
            String lhValue = (String) lhDisplayValues.get(lhKey);
            String rhValue = (String) rhDisplayValues.get(rhKey);
            String displayValue = (lhValue.equals(rhValue) ? lhValue
               : (lhValue + ":" + rhValue));
            result.put(lhKey, displayValue);
         }
         else {
            if (!result.containsKey(lhKey))
               result.put(lhKey, (String) lhDisplayValues.get(lhKey) + ":");
            else {
               String rhValue = (String) result.get(lhKey);
               result.put(lhKey, (String) lhDisplayValues.get(lhKey) + rhValue);
            }
            if (!result.containsKey(rhKey))
               result.put(rhKey, ":" + (String) rhDisplayValues.get(rhKey));
            else {
               String lhValue = (String) result.get(rhKey);
               result.put(rhKey, lhValue + (String) rhDisplayValues.get(rhKey));
            }
         }
      }
      return result;
   }

   /**
    * @return OrderedMap where key is String and value is String; converts nulls
    *         to "<null>"
    */
   @SuppressWarnings("unchecked")
   public OrderedMap getRowDisplayValues(Object[] row_, int sideIdx_) {
      if (row_ == null)
         return null;
      int[] displayIndexes = _displayIndexes[sideIdx_];
      DKColumnModel[] columns = _tableModels[sideIdx_].getColumns();
      OrderedMap result = new LinkedMap(_displayIndexes.length);
      for (int i = 0; i < displayIndexes.length; i++) {
         Object value = row_[displayIndexes[i]];
         String displayValue = (value == null ? "<null>" : value.toString());
         result.put(columns[displayIndexes[i]]._name, displayValue);
      }
      return result;
   }

   /**
    * ensure that keys can be aligned; same number of columns and types
    * 
    * @throws RuntimeException
    *            if the tables don't line up
    */
   private void validateTableModels(DKTableModel lhs_, DKTableModel rhs_) {
      DKValidate.notNull(lhs_, rhs_);
      int[] lhsKey = lhs_.getKey();
      if ((lhsKey == null) || (lhsKey.length == 0))
         throw new RuntimeException(String.format("no key columns for lhs_->%s", lhs_));
      int[] rhsKey = rhs_.getKey();
      if ((rhsKey == null) || (rhsKey.length == 0))
         throw new RuntimeException(String.format("no key columns for rhs_->%s", rhs_));
      if (lhsKey.length != rhsKey.length)
         throw new RuntimeException(String.format(
            "lhsKey length->%s does not match rhsKey length->%s", lhsKey.length,
            rhsKey.length));
      DKColumnModel[] lhsColumns = lhs_.getColumns();
      DKColumnModel[] rhsColumns = rhs_.getColumns();
      // now check that the types match
      for (int i = 0; i < lhsKey.length; i++) {
         if (lhsColumns[lhsKey[i]]._type != rhsColumns[rhsKey[i]]._type)
            throw new RuntimeException(
               String.format(
                  "lhs key component->%s type->%s must match rhs key component->%s type->%s",
                  lhsColumns[lhsKey[i]], lhsColumns[lhsKey[i]]._type,
                  rhsColumns[rhsKey[i]], rhsColumns[rhsKey[i]]._type));
      }
   }

   private void validateComparisons(DKTableModel lhsTable_, DKTableModel rhsTable_,
                                    DKColumnComparison[] comparisons_) {
      for (DKColumnComparison comparison : comparisons_)
         comparison.validate(lhsTable_, rhsTable_);
   }

   public String toString() {
      return String.format("%s[%s<->%s]", ClassUtils.getShortClassName(this.getClass()),
         _tableModels[DKSide.LEFT_INDEX], _tableModels[DKSide.LEFT_INDEX]);
   }

   public String getDescription() {
      ToStringBuilder builder = new ToStringBuilder(this,
         ToStringStyle.SHORT_PREFIX_STYLE);
      builder.append("_tableModels[LEFT]", _tableModels[DKSide.LEFT_INDEX]);
      builder.append("_tableModels[RIGHT]", _tableModels[DKSide.RIGHT_INDEX]);
      builder.append("_kind", _kind);
      builder.append("_map", Arrays.toString(_map));
      builder.append("_diffIndexes", Arrays.toString(_diffIndexes));
      builder.append("_displayIndexes[LEFT]",
         Arrays.toString(_displayIndexes[DKSide.LEFT_INDEX]));
      builder.append("_displayIndexes[RIGHT]",
         Arrays.toString(_displayIndexes[DKSide.RIGHT_INDEX]));
      builder.append("_maxDiffs", _maxDiffs);

      return builder.toString();
   }

   public Map<UserKey, ?> getUserDictionary() {
      return null;
   }

}
