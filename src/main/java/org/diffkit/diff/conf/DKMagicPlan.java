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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.db.DKDBConnectionInfo;
import org.diffkit.diff.engine.DKDiff;
import org.diffkit.diff.engine.DKSink;
import org.diffkit.diff.engine.DKSource;
import org.diffkit.diff.engine.DKStandardTableComparison;
import org.diffkit.diff.engine.DKTableComparison;

/**
 * @author jpanico
 */
public class DKMagicPlan implements DKPlan {

   private final DKPassthroughPlan _providedPlan = new DKPassthroughPlan();
   private String _dbTableName;
   private String _lhsDBTableName;
   private String _rhsDBTableName;
   private String _whereClause;
   private String _lhsWhereClause;
   private String _rhsWhereClause;
   private String _lhsFilePath;
   private String _rhsFilePath;
   private String _lhsSpreadSheetFilePath;
   private String _rhsSpreadSheetFilePath;
   private String _lhsSpreadSheetName;
   private String _rhsSpreadSheetName;
   private Boolean _hasHeader;
   private Boolean _isSorted;
   private String _sinkFilePath;
   private String _sqlPatchFilePath;
   private String _delimiter;
   private DKDBConnectionInfo _dbConnectionInfo;
   private DKDBConnectionInfo _lhsDBConnectionInfo;
   private DKDBConnectionInfo _rhsDBConnectionInfo;
   private DKDiff.Kind _diffKind;
   private String[] _keyColumnNames;
   private String[] _diffColumnNames;
   private String[] _ignoreColumnNames;
   private String[] _displayColumnNames;
   private Float _numberTolerance;
   private Map<String, String[]> _toleranceMap;
   private Long _maxDiffs;
   private Boolean _withSummary;
   private String[] _groupByColumnNames;
   private DKPassthroughPlan _builtPlan;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   public String getDbTableName() {
      return _dbTableName;
   }

   public void setDbTableName(String dbTableName_) {
      _dbTableName = dbTableName_;
   }

   public String getLhsDBTableName() {
      return _lhsDBTableName;
   }

   public void setLhsDBTableName(String lhsDBTableName_) {
      _lhsDBTableName = lhsDBTableName_;
   }

   public String getRhsDBTableName() {
      return _rhsDBTableName;
   }

   public void setRhsDBTableName(String rhsDBTableName_) {
      _rhsDBTableName = rhsDBTableName_;
   }

   public String getWhereClause() {
      return _whereClause;
   }

   public void setWhereClause(String whereClause_) {
      _whereClause = whereClause_;
   }

   public String getLhsWhereClause() {
      return _lhsWhereClause;
   }

   public void setLhsWhereClause(String lhsWhereClause_) {
      _lhsWhereClause = lhsWhereClause_;
   }

   public String getRhsWhereClause() {
      return _rhsWhereClause;
   }

   public void setRhsWhereClause(String rhsWhereClause_) {
      _rhsWhereClause = rhsWhereClause_;
   }

   public String getLhsFilePath() {
      return _lhsFilePath;
   }

   public void setLhsFilePath(String lhsFilePath_) {
      _lhsFilePath = lhsFilePath_;
   }

   public String getRhsFilePath() {
      return _rhsFilePath;
   }

   public void setRhsFilePath(String rhsFilePath_) {
      _rhsFilePath = rhsFilePath_;
   }

   public String getRhsSpreadSheetFilePath() {
      return _rhsSpreadSheetFilePath;
   }

   public void setRhsSpreadSheetFilePath(String rhsSpreadSheetFilePath_) {
      _rhsSpreadSheetFilePath = rhsSpreadSheetFilePath_;
   }

   public String getLhsSpreadSheetFilePath() {
      return _lhsSpreadSheetFilePath;
   }

   public void setLhsSpreadSheetFilePath(String lhsSpreadSheetFilePath_) {
      _lhsSpreadSheetFilePath = lhsSpreadSheetFilePath_;
   }

   public String getRhsSpreadSheetName() {
      return _rhsSpreadSheetName;
   }

   public void setRhsSpreadSheetName(String rhsSpreadSheetName_) {
      _rhsSpreadSheetName = rhsSpreadSheetName_;
   }

   public String getLhsSpreadSheetName() {
      return _lhsSpreadSheetName;
   }

   public void setLhsSpreadSheetName(String lhsSpreadSheetName_) {
      _lhsSpreadSheetName = lhsSpreadSheetName_;
   }

   public Boolean getHasHeader() {
      return _hasHeader;
   }

   public void setHasHeader(Boolean hasHeader_) {
      _hasHeader = hasHeader_;
   }

   public Boolean getIsSorted() {
      return _isSorted;
   }

   public void setIsSorted(Boolean isSorted_) {
      _isSorted = isSorted_;
   }

   public String getSinkFilePath() {
      return _sinkFilePath;
   }

   public void setSinkFilePath(String sinkFilePath_) {
      _sinkFilePath = sinkFilePath_;
   }

   public String getSqlPatchFilePath() {
      return _sqlPatchFilePath;
   }

   public void setSqlPatchFilePath(String sqlPatchFilePath_) {
      _sqlPatchFilePath = sqlPatchFilePath_;
   }

   public String getDelimiter() {
      return _delimiter;
   }

   public void setDelimiter(String delimiter_) {
      _delimiter = delimiter_;
   }

   public String[] getDiffColumnNames() {
      return _diffColumnNames;
   }

   public void setDiffColumnNames(String[] diffColumnNames_) {
      _diffColumnNames = diffColumnNames_;
   }

   public String[] getIgnoreColumnNames() {
      return _ignoreColumnNames;
   }

   public void setIgnoreColumnNames(String[] ignoreColumnNames_) {
      _ignoreColumnNames = ignoreColumnNames_;
   }

   public String[] getDisplayColumnNames() {
      return _displayColumnNames;
   }

   public void setDisplayColumnNames(String[] displayColumnNames_) {
      _displayColumnNames = displayColumnNames_;
   }

   public Float getNumberTolerance() {
      return _numberTolerance;
   }

   public void setNumberTolerance(Float numberTolerance_) {
      _numberTolerance = numberTolerance_;
   }

   public Map<String, String[]> getToleranceMap() {
      return _toleranceMap;
   }

   public void setToleranceMap(Map<String, String[]> toleranceMap_) {
      _toleranceMap = toleranceMap_;
   }

   public Long getMaxDiffs() {
      return _maxDiffs;
   }

   public void setMaxDiffs(Long maxDiffs_) {
      _maxDiffs = maxDiffs_;
   }

   public void setWithSummary(Boolean withSummary) {
      _withSummary = withSummary;
   }

   public Boolean getWithSummary() {
      return _withSummary;
   }

   public String[] getGroupByColumnNames() {
      return _groupByColumnNames;
   }

   public void setGroupByColumnNames(String[] groupByColumnNames_) {
      _groupByColumnNames = groupByColumnNames_;
   }

   public DKDBConnectionInfo getDbConnectionInfo() {
      return _dbConnectionInfo;
   }

   public void setDbConnectionInfo(DKDBConnectionInfo dbConnectionInfo_) {
      _dbConnectionInfo = dbConnectionInfo_;
   }

   public DKDBConnectionInfo getLhsDBConnectionInfo() {
      return _lhsDBConnectionInfo;
   }

   public void setLhsDBConnectionInfo(DKDBConnectionInfo lhsDBConnectionInfo_) {
      _lhsDBConnectionInfo = lhsDBConnectionInfo_;
   }

   public DKDBConnectionInfo getRhsDBConnectionInfo() {
      return _rhsDBConnectionInfo;
   }

   public void setRhsDBConnectionInfo(DKDBConnectionInfo rhsDBConnectionInfo_) {
      _rhsDBConnectionInfo = rhsDBConnectionInfo_;
   }

   public void setDiffKind(DKDiff.Kind diffKind) {
      _diffKind = diffKind;
   }

   public DKDiff.Kind getDiffKind() {
      return _diffKind;
   }

   public String[] getKeyColumnNames() {
      return _keyColumnNames;
   }

   public void setKeyColumnNames(String[] keyColumnNames_) {
      _keyColumnNames = keyColumnNames_;
   }

   public void setLhsSource(DKSource lhsSource_) {
      _providedPlan.setLhsSource(lhsSource_);
   }

   public void setRhsSource(DKSource rhsSource_) {
      _providedPlan.setRhsSource(rhsSource_);
   }

   public void setSink(DKSink sink_) {
      _providedPlan.setSink(sink_);
   }

   public void setTableComparison(DKStandardTableComparison tableComparison_) {
      _providedPlan.setTableComparison(tableComparison_);
   }

   public DKPassthroughPlan getProvidedPlan() {
      return _providedPlan;
   }

   public DKSource getLhsSource() {
      return this.getBuiltPlan().getLhsSource();
   }

   public DKSource getRhsSource() {
      return this.getBuiltPlan().getRhsSource();
   }

   public DKSink getSink() {
      return this.getBuiltPlan().getSink();
   }

   public DKTableComparison getTableComparison() {
      return this.getBuiltPlan().getTableComparison();
   }

   private DKPassthroughPlan getBuiltPlan() {
      if (_builtPlan != null)
         return _builtPlan;
      DKMagicPlanBuilder builder = new DKMagicPlanBuilder(this);
      try {
         _builtPlan = builder.build();
      }
      catch (Exception e_) {
         throw new RuntimeException(e_);
      }
      return _builtPlan;
   }
}
