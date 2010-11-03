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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKUserException;
import org.diffkit.common.DKValidate;
import org.diffkit.diff.diffor.DKConvertingDiffor;
import org.diffkit.diff.diffor.DKEqualsDiffor;
import org.diffkit.diff.diffor.DKNumberDiffor;
import org.diffkit.diff.diffor.DKTextDiffor;
import org.diffkit.diff.engine.DKColumnComparison;
import org.diffkit.diff.engine.DKColumnModel;
import org.diffkit.diff.engine.DKColumnModel.Type;
import org.diffkit.diff.engine.DKContext.UserKey;
import org.diffkit.diff.engine.DKDiff;
import org.diffkit.diff.engine.DKDiff.Kind;
import org.diffkit.diff.engine.DKDiffor;
import org.diffkit.diff.engine.DKSide;
import org.diffkit.diff.engine.DKSource;
import org.diffkit.diff.engine.DKStandardTableComparison;
import org.diffkit.diff.engine.DKTableComparison;
import org.diffkit.diff.engine.DKTableModel;
import org.diffkit.util.DKArrayUtil;
import org.diffkit.util.DKObjectUtil;

/**
 * @author jpanico
 */
public class DKAutomaticTableComparison implements DKTableComparison {
   private static final DKDiffor DEFAULT_TEXT_DIFFOR = new DKTextDiffor(null);
   private final DKSource _lhsSource;
   private final DKSource _rhsSource;
   private final DKDiff.Kind _kind;
   private final String[] _diffColumnNames;
   private final String[] _ignoreColumnNames;
   private final String[] _displayColumnNames;
   private final Long _maxDiffs;
   private final Float _numberTolerance;
   /**
    * key is columnName, Float is tolerance
    */
   private final Map<String, Float> _toleranceMap;
   private DKStandardTableComparison _standardComparison;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   /**
    * if both diffColumnNames_ and ignoreColumnNames_ are non-null,
    * diffColumnNames will take precedence. If both are null, then all non-key
    * columns are used
    */
   public DKAutomaticTableComparison(DKSource lhsSource_, DKSource rhsSource_,
                                     DKDiff.Kind kind_, String[] diffColumnNames_,
                                     String[] ignoreColumnNames_,
                                     String[] displayColumnNames_, Long maxDiffs_,
                                     Float numberTolerance_,
                                     Map<String, String[]> toleranceMap_) {
      _lhsSource = lhsSource_;
      _rhsSource = rhsSource_;
      _kind = kind_;
      _diffColumnNames = diffColumnNames_;
      _ignoreColumnNames = ignoreColumnNames_;
      _displayColumnNames = displayColumnNames_;
      _maxDiffs = maxDiffs_;
      _numberTolerance = numberTolerance_;
      _toleranceMap = createToleranceMap(toleranceMap_, _lhsSource.getModel());
      DKValidate.notNull(_lhsSource, _rhsSource, _kind, _maxDiffs);
      validateColumnNames(_displayColumnNames, "displayColumnNames", lhsSource_,
         rhsSource_);
      _log.debug("_lhsSource->{}", _lhsSource);
      _log.debug("_rhsSource->{}", _rhsSource);
      _log.debug("_diffColumnNames->{}", Arrays.toString(_diffColumnNames));
      _log.debug("_ignoreColumnNames->{}", Arrays.toString(_ignoreColumnNames));
      _log.debug("_displayColumnNames->{}", Arrays.toString(_displayColumnNames));
      _log.debug("_maxDiffs->{}", _maxDiffs);
      _log.debug("_numberTolerance->{}", _numberTolerance);
      _log.debug("_toleranceMap->{}", _toleranceMap);
   }

   private static void validateColumnNames(String[] columnNames_,
                                           String columnTypeLabel_, DKSource lhsSource_,
                                           DKSource rhsSource_) {
      validateColumnNames(columnNames_, columnTypeLabel_, lhsSource_, "lhsSource");
      validateColumnNames(columnNames_, columnTypeLabel_, rhsSource_, "rhsSource");
   }

   private DKStandardTableComparison getStandardComparison() {
      if (_standardComparison != null)
         return _standardComparison;
      _standardComparison = this.buildStandardComparison();
      return _standardComparison;
   }

   private DKStandardTableComparison buildStandardComparison() {
      _log.debug("_lhsSource->{}", _lhsSource.getModel().getDescription());
      _log.debug("_rhsSource->{}", _rhsSource.getModel().getDescription());
      DKColumnComparison[] map = this.createDefaultMap(_lhsSource.getModel(),
         _rhsSource.getModel());
      _log.debug("map->{}", Arrays.toString(map));
      if (ArrayUtils.isEmpty(map))
         throw new RuntimeException(String.format(
            "Unable to map any columns from _lhsSource->%s to _rhsSource->%s",
            _lhsSource, _rhsSource));
      int[] diffIndexes = null;
      if (!ArrayUtils.isEmpty(_diffColumnNames))
         diffIndexes = getIndexesOfColumnNames(map, _diffColumnNames, false);
      else if (!ArrayUtils.isEmpty(_ignoreColumnNames))
         diffIndexes = getIndexesOfColumnNames(map, _ignoreColumnNames, true);
      else
         diffIndexes = getNonKeyIndexes(map);
      _log.debug("diffIndexes->{}", diffIndexes);

      int[][] displayIndexes = null;
      if (!ArrayUtils.isEmpty(_displayColumnNames))
         displayIndexes = getIndexesOfColumnNames(_lhsSource.getModel(),
            _rhsSource.getModel(), _displayColumnNames);
      else
         displayIndexes = getKeyIndexes(_lhsSource.getModel(), _rhsSource.getModel());
      _log.debug("displayIndexes->{}", displayIndexes);

      return new DKStandardTableComparison(_lhsSource.getModel(), _rhsSource.getModel(),
         _kind, map, diffIndexes, displayIndexes, _maxDiffs);
   }

   public DKSource getLhsSource() {
      return _lhsSource;
   }

   public DKSource getRhsSource() {
      return _rhsSource;
   }

   /**
    * @see org.diffkit.diff.engine.DKTableComparison#getColumnName(int)
    */
   public String getColumnName(int columnStep_) {
      return this.getStandardComparison().getColumnName(columnStep_);
   }

   /**
    * @see org.diffkit.diff.engine.DKTableComparison#getDescription()
    */
   public String getDescription() {
      return this.getStandardComparison().getDescription();
   }

   /**
    * @see org.diffkit.diff.engine.DKTableComparison#getDiffIndexes()
    */
   public int[] getDiffIndexes() {
      return this.getStandardComparison().getDiffIndexes();
   }

   /**
    * @see org.diffkit.diff.engine.DKTableComparison#getDisplayIndexes()
    */
   public int[][] getDisplayIndexes() {
      return this.getStandardComparison().getDisplayIndexes();
   }

   /**
    * @see org.diffkit.diff.engine.DKTableComparison#getKind()
    */
   public Kind getKind() {
      return this.getStandardComparison().getKind();
   }

   /**
    * @see org.diffkit.diff.engine.DKTableComparison#getLhsModel()
    */
   public DKTableModel getLhsModel() {
      return this.getStandardComparison().getLhsModel();
   }

   /**
    * @see org.diffkit.diff.engine.DKTableComparison#getMap()
    */
   public DKColumnComparison[] getMap() {
      return this.getStandardComparison().getMap();
   }

   /**
    * @see org.diffkit.diff.engine.DKTableComparison#getMaxDiffs()
    */
   public long getMaxDiffs() {
      return this.getStandardComparison().getMaxDiffs();
   }

   /**
    * @see org.diffkit.diff.engine.DKTableComparison#getRhsModel()
    */
   public DKTableModel getRhsModel() {
      return this.getStandardComparison().getRhsModel();
   }

   /**
    * @see org.diffkit.diff.engine.DKTableComparison#getRowComparator()
    */
   public Comparator<Object[]> getRowComparator() {
      return this.getStandardComparison().getRowComparator();
   }

   /**
    * @see org.diffkit.diff.engine.DKTableComparison#getRowDisplayValues(java.lang
    *      .Object[], java.lang.Object[])
    */
   public OrderedMap getRowDisplayValues(Object[] lhs_, Object[] rhs_) {
      return this.getStandardComparison().getRowDisplayValues(lhs_, rhs_);
   }

   /**
    * @see org.diffkit.diff.engine.DKTableComparison#getRowDisplayValues(java.lang
    *      .Object[], int)
    */
   public OrderedMap getRowDisplayValues(Object[] row_, int sideIdx_) {
      return this.getStandardComparison().getRowDisplayValues(row_, sideIdx_);
   }

   /**
    * @see org.diffkit.diff.engine.DKTableComparison#getRowKeyValues(java.lang.Object
    *      [], int)
    */
   public Object[] getRowKeyValues(Object[] aRow_, int sideIdx_) {
      return this.getStandardComparison().getRowKeyValues(aRow_, sideIdx_);
   }

   public Map<UserKey, Object> getUserDictionary() {
      Map<UserKey, Object> userDictionary = new HashMap<UserKey, Object>();
      if (!ArrayUtils.isEmpty(_diffColumnNames))
         userDictionary.put(UserKey.DIFF_COLUMN_NAMES, Arrays.asList(_diffColumnNames));
      if (!ArrayUtils.isEmpty(_ignoreColumnNames))
         userDictionary.put(UserKey.DIFF_COLUMN_NAMES, Arrays.asList(_ignoreColumnNames));
      return userDictionary;
   }

   /**
    * columnName has to be same on both sides of the comparison for their to be
    * a match
    */
   private static int[] getIndexesOfColumnNames(DKColumnComparison[] target_,
                                                String[] columnNames_, boolean invert_) {
      int[] indexes = new int[target_.length];
      Arrays.fill(indexes, -1);
      for (int i = 0, j = 0; i < target_.length; i++) {
         String columnName = target_[i].getColumnName();
         if (columnName == null)
            continue;
         if (ArrayUtils.contains(columnNames_, columnName) ^ invert_)
            indexes[j++] = i;
      }
      return DKArrayUtil.compactFill(indexes, -1);
   }

   private static int[][] getIndexesOfColumnNames(DKTableModel lhsTarget_,
                                                  DKTableModel rhsTarget_,
                                                  String[] columnNames_) {
      int[][] indexes = new int[2][];
      indexes[DKSide.LEFT_INDEX] = lhsTarget_.getColumnIndexes(columnNames_);
      indexes[DKSide.RIGHT_INDEX] = rhsTarget_.getColumnIndexes(columnNames_);
      return indexes;
   }

   private static int[] getNonKeyIndexes(DKColumnComparison[] target_) {
      int[] indexes = new int[target_.length];
      Arrays.fill(indexes, -1);
      for (int i = 0, j = 0; i < target_.length; i++) {
         if (!target_[i].isInKey())
            indexes[j++] = i;
      }
      return DKArrayUtil.compactFill(indexes, -1);
   }

   private static int[][] getKeyIndexes(DKTableModel lhsTarget_, DKTableModel rhsTarget_) {
      int[][] indexes = new int[2][];
      indexes[DKSide.LEFT_INDEX] = lhsTarget_.getKey();
      indexes[DKSide.RIGHT_INDEX] = rhsTarget_.getKey();
      return indexes;
   }

   /**
    * maps only columns that have same name on both sides. Uses only columns
    * that don't participate in the key for diffIndexes. Uses the diff keys for
    * display
    */
   public DKStandardTableComparison createDefaultTableComparison(DKTableModel lhsModel_,
                                                                 DKTableModel rhsModel_) {
      DKValidate.notNull(lhsModel_, rhsModel_);
      DKColumnComparison[] map = this.createDefaultMap(lhsModel_, rhsModel_);
      List<Integer> diffIndexesValue = new ArrayList<Integer>();
      if (map != null) {
         for (int i = 0; i < map.length; i++) {
            if (!map[i].columnsAreKey())
               diffIndexesValue.add(i);
         }
      }
      int[] diffIndexes = ArrayUtils.toPrimitive(diffIndexesValue.toArray(new Integer[diffIndexesValue.size()]));
      int[][] displayIndexes = new int[2][];
      displayIndexes[DKSide.LEFT_INDEX] = lhsModel_.getKey();
      displayIndexes[DKSide.RIGHT_INDEX] = rhsModel_.getKey();
      return new DKStandardTableComparison(lhsModel_, rhsModel_, DKDiff.Kind.BOTH, map,
         diffIndexes, displayIndexes, Long.MAX_VALUE);
   }

   private DKColumnComparison[] createDefaultMap(DKTableModel lhsModel_,
                                                 DKTableModel rhsModel_) {
      List<DKColumnComparison> columnComparisons = new ArrayList<DKColumnComparison>();
      DKColumnModel[] lhsColumns = lhsModel_.getColumns();
      for (DKColumnModel lhsColumn : lhsColumns) {
         DKColumnModel rhsColumn = rhsModel_.getColumn(lhsColumn.getName());
         if (_log.isDebugEnabled()) {
            _log.debug("lhsColumn->{}", lhsColumn.getDescription());
            _log.debug("rhsColumn->{}", rhsColumn.getDescription());
         }
         if (rhsColumn == null)
            continue;
         DKDiffor diffor = this.getDiffor(lhsColumn, rhsColumn);
         if (_log.isDebugEnabled()) {
            String difforString = DKObjectUtil.respondsTo(diffor, "getDescription", null)
               ? (String) DKObjectUtil.invokeSafe(diffor, "getDescription", null)
               : diffor.toString();
            _log.debug("diffor->{}", difforString);
         }
         DKColumnComparison columnComparison = new DKColumnComparison(lhsColumn,
            rhsColumn, diffor);
         columnComparisons.add(columnComparison);
      }
      if (columnComparisons.isEmpty())
         return null;
      return columnComparisons.toArray(new DKColumnComparison[columnComparisons.size()]);
   }

   private DKDiffor getDiffor(DKColumnModel lhsColumn_, DKColumnModel rhsColumn_) {
      DKDiffor baseDiffor = this.getBaseDiffor(lhsColumn_, rhsColumn_);
      return this.getConvertingDiffor(lhsColumn_, rhsColumn_, baseDiffor);
   }

   private DKDiffor getBaseDiffor(DKColumnModel lhsColumn_, DKColumnModel rhsColumn_) {
      if ((lhsColumn_._type == DKColumnModel.Type.TEXT)
         && (rhsColumn_._type == DKColumnModel.Type.TEXT))
         return DEFAULT_TEXT_DIFFOR;
      if ((_numberTolerance == null) && (_toleranceMap == null))
         return DKEqualsDiffor.getInstance();
      Float tolerance = (_toleranceMap != null ? _toleranceMap.get(lhsColumn_._name)
         : null);
      if (tolerance != null)
         return new DKNumberDiffor(tolerance, true);
      if ((!lhsColumn_._type._isNumber) && (!rhsColumn_._type._isNumber))
         return DKEqualsDiffor.getInstance();
      if (_numberTolerance == null)
         return DKEqualsDiffor.getInstance();
      return new DKNumberDiffor(_numberTolerance, true);
   }

   private DKDiffor getConvertingDiffor(DKColumnModel lhsColumn_,
                                        DKColumnModel rhsColumn_, DKDiffor baseDiffor_) {
      DKColumnModel.Type lhsType = lhsColumn_.getType();
      DKColumnModel.Type rhsType = rhsColumn_.getType();
      if (lhsType == rhsType)
         return baseDiffor_;
      if ((lhsType == Type.INTEGER) && (rhsType == Type.STRING))
         return new DKConvertingDiffor(null, Long.class, baseDiffor_);
      else if ((lhsType == Type.STRING) && (rhsType == Type.INTEGER))
         return new DKConvertingDiffor(Long.class, null, baseDiffor_);
      else if ((lhsType == Type.REAL) && (rhsType == Type.STRING))
         return new DKConvertingDiffor(null, Double.class, baseDiffor_);
      else if ((lhsType == Type.STRING) && (rhsType == Type.REAL))
         return new DKConvertingDiffor(Double.class, null, baseDiffor_);
      else if ((lhsType == Type.DECIMAL) && (rhsType == Type.STRING))
         return new DKConvertingDiffor(null, BigDecimal.class, baseDiffor_);
      else if ((lhsType == Type.STRING) && (rhsType == Type.DECIMAL))
         return new DKConvertingDiffor(BigDecimal.class, null, baseDiffor_);
      else
         throw new RuntimeException(String.format(
            "unhandled conversion needed: lhsType->%s rhsType->%s", lhsType, rhsType));
   }

   private static void validateColumnNames(String[] columnNames_,
                                           String columnTypeLabel_, DKSource source_,
                                           String sourceLabel_) {
      if (ArrayUtils.isEmpty(columnNames_))
         return;
      DKTableModel model = source_.getModel();
      if (model == null)
         throw new RuntimeException(String.format("no model from source_ [%s]", source_));
      for (String columnName : columnNames_) {
         if (!model.containsColumn(columnName))
            throw new DKUserException(String.format(
               "source [%s] does not contain column [%s] for type [%s]", source_,
               columnName, columnTypeLabel_));
      }
   }

   /**
    * @param rawMap_
    *           in the form of key = tolerance (as a String), and value = list
    *           of columnNames using that tolerance
    * @return Map where key = columnName and value = tolerance
    */
   private static Map<String, Float> createToleranceMap(Map<String, String[]> rawMap_,
                                                        DKTableModel tableModel_) {
      if ((rawMap_ == null) || (rawMap_.isEmpty()))
         return null;
      Map<String, Float> processedMap = new HashMap<String, Float>();
      Set<Map.Entry<String, String[]>> entries = rawMap_.entrySet();
      for (Map.Entry<String, String[]> entry : entries) {
         String toleranceString = entry.getKey();
         Float tolerance = NumberUtils.createFloat(toleranceString);
         String[] columnNames = entry.getValue();
         if ((columnNames == null) || (columnNames.length == 0))
            continue;
         for (String columnName : columnNames) {
            if (!tableModel_.containsColumn(columnName))
               throw new RuntimeException(
                  String.format(
                     "columnName->%s specified in toleranceMap->%s is not present in tableModel_->%s",
                     columnName, rawMap_, tableModel_));
            processedMap.put(columnName, tolerance);
         }
      }
      if (processedMap.isEmpty())
         return null;
      return processedMap;
   }

}
