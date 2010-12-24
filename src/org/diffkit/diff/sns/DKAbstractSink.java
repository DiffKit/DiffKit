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
package org.diffkit.diff.sns;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKCountingBag;
import org.diffkit.common.DKRuntime;
import org.diffkit.diff.engine.DKColumnDiff;
import org.diffkit.diff.engine.DKColumnDiffRow;
import org.diffkit.diff.engine.DKContext;
import org.diffkit.diff.engine.DKDiff;
import org.diffkit.diff.engine.DKRowDiff;
import org.diffkit.diff.engine.DKSide;
import org.diffkit.diff.engine.DKSink;
import org.diffkit.util.DKStringUtil;

/**
 * @author jpanico
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class DKAbstractSink implements DKSink {

   private static final String COLUMN_CLUSTER_KEY_SEPARATOR = ".";

   private boolean _isStarted;
   private boolean _isEnded;
   private DKContext _context;
   private long[] _rowDiffCount = new long[2];
   private DKCountingBag _columnDiffCount = new DKCountingBag();
   private long _runningRowStep;
   private DKColumnDiffRow _runningRow;
   private StringBuilder _runningColumnClusterKey;
   private DKCountingBag _columnDiffClusterCount = new DKCountingBag();
   private final String[] _groupByColumnNames;
   private DKCountingBag[] _rowGroupDiffCount = { new DKCountingBag(),
      new DKCountingBag() };
   private DKCountingBag _columnGroupDiffCount = new DKCountingBag();
   private DKCountingBag _columnDiffClusterGroupCount = new DKCountingBag();
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   protected DKAbstractSink(String[] groupByColumnNames_) {
      _groupByColumnNames = groupByColumnNames_;
      _log.debug("_groupByColumnNames->{}", Arrays.toString(_groupByColumnNames));
   }

   public String[] getGroupByColumnNames() {
      return _groupByColumnNames;
   }

   public void open(DKContext context_) throws IOException {
      this.ensureNotStarted();
      this.ensureNotEnded();
      _context = context_;
      _isStarted = true;
      _runningRowStep = -1;
   }

   public void close(DKContext context_) throws IOException {
      this.ensureStarted();
      this.ensureNotEnded();
      _isEnded = true;
      _context = null;

      this.closeAndRecordDiffCluster(context_);
   }

   public void record(DKDiff diff_, DKContext context_) throws IOException {
      boolean receiverNull = (_context == null);
      boolean callerNull = (context_ == null);
      if (receiverNull && callerNull)
         return;
      if (receiverNull || callerNull)
         throw new RuntimeException(String.format(
            "context_->%s does not match _context at open->%s", context_._id,
            _context._id));
      if (_context._id != context_._id)
         throw new RuntimeException(String.format(
            "context_->%s does not match _context at open->%s", context_._id,
            _context._id));
      if (diff_ == null)
         return;
      if (diff_.getKind() == DKDiff.Kind.ROW_DIFF)
         this.recordRowDiff((DKRowDiff) diff_, context_);
      else if (diff_.getKind() == DKDiff.Kind.COLUMN_DIFF)
         this.recordColumnDiff((DKColumnDiff) diff_, context_);
      this.closeRecordAndAppendDiffCluster(diff_, context_);
   }

   private void closeRecordAndAppendDiffCluster(DKDiff diff_, DKContext context_) {
      this.closeAndRecordDiffCluster(context_);
      if (diff_.getKind() != DKDiff.Kind.COLUMN_DIFF)
         return;
      this.appendToDiffCluster((DKColumnDiff) diff_);
   }

   private void closeAndRecordDiffCluster(DKContext context_) {
      DKColumnDiffRow clusterRow = _runningRow;
      String clusterKey = this.closeDiffCluster(context_);
      this.recordDiffCluster(clusterKey);
      this.recordDiffClusterGroup(clusterKey, clusterRow);
   }

   private void recordDiffClusterGroup(String clusterKey_, DKColumnDiffRow clusterRow_) {
      if ((clusterKey_ == null) || (clusterRow_ == null))
         return;
      String groupKey = getGroupKey(_groupByColumnNames,
         clusterRow_.getRowDisplayValues());
      if (groupKey == null)
         return;
      String clusterGroupKey = String.format("%s/%s", clusterKey_, groupKey);
      _columnDiffClusterGroupCount.add(clusterGroupKey);
   }

   /**
    * attempt to close the running cluster
    * 
    * @param context_
    *           never null
    * @return clusterKey String
    */
   private String closeDiffCluster(DKContext context_) {
      if (context_ == null)
         return null;
      if (_runningRowStep == context_._rowStep)
         return null;
      if (_runningColumnClusterKey == null)
         return null;
      String diffClusterKey = _runningColumnClusterKey.toString();
      _runningRowStep = -1;
      _runningRow = null;
      _runningColumnClusterKey = null;
      return diffClusterKey;
   }

   private void appendToDiffCluster(DKColumnDiff columnDiff_) {
      if (columnDiff_ == null)
         return;
      if (_runningRowStep < 0)
         _runningRowStep = columnDiff_.getRowStep();
      if (_runningRow == null)
         _runningRow = columnDiff_.getRow();
      if (_runningColumnClusterKey == null)
         _runningColumnClusterKey = new StringBuilder();
      if (_runningColumnClusterKey.length() != 0)
         _runningColumnClusterKey.append(COLUMN_CLUSTER_KEY_SEPARATOR);
      _runningColumnClusterKey.append(columnDiff_.getColumnName());
   }

   private void recordDiffCluster(String diffClusterKey_) {
      if (diffClusterKey_ == null)
         return;
      _columnDiffClusterCount.add(diffClusterKey_);
   }

   private void recordRowDiff(DKRowDiff rowDiff_, DKContext context_) {
      this.recordRowDiffCount(rowDiff_, context_);
      this.recordRowGroupDiffCount(rowDiff_, context_);
   }

   private void recordColumnDiff(DKColumnDiff columnDiff_, DKContext context_) {
      this.recordColumnDiffCount(columnDiff_, context_);
      this.recordColumnGroupDiffCount(columnDiff_, context_);
   }

   private void recordRowDiffCount(DKRowDiff rowDiff_, DKContext context_) {
      _rowDiffCount[DKSide.getConstantForEnum(rowDiff_.getSide())]++;
   }

   private void recordRowGroupDiffCount(DKRowDiff rowDiff_, DKContext context_) {
      if (_groupByColumnNames == null)
         return;
      OrderedMap displayValues = rowDiff_.getRowDisplayValues();
      if (MapUtils.isEmpty(displayValues))
         return;
      String groupKey = getGroupKey(_groupByColumnNames, rowDiff_.getRowDisplayValues());
      if (groupKey == null)
         return;
      _rowGroupDiffCount[DKSide.getConstantForEnum(rowDiff_.getSide())].add(groupKey);
   }

   private void recordColumnDiffCount(DKColumnDiff columnDiff_, DKContext context_) {
      _columnDiffCount.add(columnDiff_.getColumnName());
   }

   private void recordColumnGroupDiffCount(DKColumnDiff columnDiff_, DKContext context_) {
      if (_groupByColumnNames == null)
         return;
      String diffColumnName = columnDiff_.getColumnName();
      if (!ArrayUtils.contains(_groupByColumnNames, diffColumnName))
         return;
      String displayValue = (String) columnDiff_.getRowDisplayValue(diffColumnName);
      String groupKey = String.format("%s=%s", diffColumnName, displayValue);
      _columnGroupDiffCount.add(groupKey);
   }

   public long getRowDiffCount() {
      return (_rowDiffCount[DKSide.LEFT_INDEX] + _rowDiffCount[DKSide.RIGHT_INDEX]);
   }

   public long getColumnDiffCount() {
      return _columnDiffCount.totalCount();
   }

   public long getDiffCount() {
      return (this.getRowDiffCount() + this.getColumnDiffCount());
   }

   protected void ensureNotStarted() {
      if (_isStarted)
         throw new RuntimeException("already started");
   }

   protected void ensureStarted() {
      if (!_isStarted)
         throw new RuntimeException("not started");
   }

   protected void ensureNotEnded() {
      if (_isEnded)
         throw new RuntimeException("already ended");
   }

   public String generateColumnDiffSummary(DKContext context_) {
      StringBuilder builder = new StringBuilder();
      builder.append("--- column diff summary ---\n");
      List<String> diffColumNames = this.getDiffColumnNames();
      builder.append(String.format("columns having diffs->%s\n",
         DKStringUtil.toSetString(diffColumNames)));
      Iterator columnDiffIterator = _columnDiffCount.iterator();
      while (columnDiffIterator.hasNext()) {
         String columnName = (String) columnDiffIterator.next();
         int diffCount = _columnDiffCount.getCount(columnName);
         builder.append(String.format("%s has %s diffs\n", columnName, diffCount));
      }
      builder.append("---------------------------\n");
      return builder.toString();
   }

   public String generateColumnDiffClusterSummary(DKContext context_) {
      StringBuilder builder = new StringBuilder();
      builder.append("--- column diffs clustered ---\n");
      List<String> clusterKeys = this.getColumnDiffClusterKeys();
      builder.append(String.format("columnClusters having diffs->%s\n",
         DKStringUtil.toSetString(clusterKeys)));
      Iterator clusterIterator = _columnDiffClusterCount.iterator();
      while (clusterIterator.hasNext()) {
         String clusterKey = (String) clusterIterator.next();
         int diffCount = _columnDiffClusterCount.getCount(clusterKey);
         builder.append(String.format("%s has %s diffs\n", clusterKey, diffCount));
      }
      builder.append("---------------------------\n");
      return builder.toString();
   }

   private List<String> getDiffColumnNames() {
      Iterator columnNameIterator = _columnDiffCount.iterator();
      if (columnNameIterator == null)
         return null;
      return (List<String>) IteratorUtils.toList(columnNameIterator);
   }

   private List<String> getColumnDiffClusterKeys() {
      Iterator keyIterator = _columnDiffClusterCount.iterator();
      if (keyIterator == null)
         return null;
      return (List<String>) IteratorUtils.toList(keyIterator);
   }

   public String generateRowDiffSummary(DKContext context_) {
      StringBuilder builder = new StringBuilder();
      builder.append("--- row diff summary ---\n");
      builder.append(String.format("%s row diffs <\n", _rowDiffCount[DKSide.RIGHT_INDEX]));
      builder.append(String.format("%s row diffs >\n", _rowDiffCount[DKSide.LEFT_INDEX]));
      builder.append("------------------------\n");
      return builder.toString();
   }

   public String generateRowDiffGroups(DKContext context_) {
      StringBuilder builder = new StringBuilder();
      builder.append("--- row diff groups ---\n");
      builder.append(String.format("%s groups <\n",
         _rowGroupDiffCount[DKSide.RIGHT_INDEX].size()));
      builder.append(String.format("%s groups >\n",
         _rowGroupDiffCount[DKSide.LEFT_INDEX].size()));
      builder.append("- <\n");
      Iterator rowGroup = _rowGroupDiffCount[DKSide.RIGHT_INDEX].iterator();
      while (rowGroup.hasNext()) {
         String groupKey = (String) rowGroup.next();
         int diffCount = _rowGroupDiffCount[DKSide.RIGHT_INDEX].getCount(groupKey);
         builder.append(String.format("%s has %s diffs\n", groupKey, diffCount));
      }
      builder.append("- >\n");
      rowGroup = _rowGroupDiffCount[DKSide.LEFT_INDEX].iterator();
      while (rowGroup.hasNext()) {
         String groupKey = (String) rowGroup.next();
         int diffCount = _rowGroupDiffCount[DKSide.LEFT_INDEX].getCount(groupKey);
         builder.append(String.format("%s has %s diffs\n", groupKey, diffCount));
      }
      builder.append("------------------------\n");
      return builder.toString();
   }

   public String generateColumnDiffGroups(DKContext context_) {
      StringBuilder builder = new StringBuilder();
      builder.append("--- column diff groups ---\n");
      Iterator columnGroupIterator = _columnGroupDiffCount.iterator();
      while (columnGroupIterator.hasNext()) {
         String groupKey = (String) columnGroupIterator.next();
         int diffCount = _columnGroupDiffCount.getCount(groupKey);
         builder.append(String.format("%s has %s diffs\n", groupKey, diffCount));
      }
      builder.append("------------------------\n");
      return builder.toString();
   }

   public String generateColumnDiffClusterGroups(DKContext context_) {
      StringBuilder builder = new StringBuilder();
      builder.append("--- column diff cluster groups ---\n");
      Iterator groupIterator = _columnDiffClusterGroupCount.iterator();
      while (groupIterator.hasNext()) {
         String groupKey = (String) groupIterator.next();
         int diffCount = _columnDiffClusterGroupCount.getCount(groupKey);
         builder.append(String.format("%s has %s diffs\n", groupKey, diffCount));
      }
      builder.append("------------------------\n");
      return builder.toString();
   }

   public String generateVeryHighLevelSummary(DKContext context_) {
      StringBuilder builder = new StringBuilder();
      builder.append("--- vhl summary ---\n");
      String timeString = (DKRuntime.getInstance().getIsTest() ? "xxx"
         : context_.getElapsedTimeString());
      builder.append(String.format("diff'd %s rows in %s, found:\n",
         (context_._rowStep - 1), timeString));
      if (this.getDiffCount() == 0)
         builder.append("(no diffs)\n");
      else
         builder.append(String.format("!%s row diffs\n@%s column diffs\n",
            this.getRowDiffCount(), this.getColumnDiffCount()));

      builder.append("-------------------\n");
      return builder.toString();
   }

   public String generateSummary(DKContext context_) {
      StringBuilder builder = new StringBuilder();
      builder.append(this.generateVeryHighLevelSummary(context_));
      if (this.getRowDiffCount() > 0)
         builder.append(this.generateRowDiffSummary(context_));
      if (this.getColumnDiffCount() > 0) {
         builder.append(this.generateColumnDiffSummary(context_));
         builder.append(this.generateColumnDiffClusterSummary(context_));
      }
      if (!ArrayUtils.isEmpty(_groupByColumnNames)) {
         if (this.getRowDiffCount() > 0)
            builder.append(this.generateRowDiffGroups(context_));
         if (this.getColumnDiffCount() > 0) {
            builder.append(this.generateColumnDiffGroups(context_));
            builder.append(this.generateColumnDiffClusterGroups(context_));
         }
      }
      return builder.toString();
   }

   private static String getGroupKey(String[] groupByColumnNames_, Map rowDisplayValues_) {
      if (ArrayUtils.isEmpty(groupByColumnNames_) || MapUtils.isEmpty(rowDisplayValues_))
         return null;
      StringBuilder keyBuilder = new StringBuilder();
      for (int i = 0; i < groupByColumnNames_.length; i++) {
         String columnName = groupByColumnNames_[i];
         keyBuilder.append(String.format("%s=%s", columnName,
            rowDisplayValues_.get(columnName)));
         if (i < (groupByColumnNames_.length - 1))
            keyBuilder.append(",");
      }
      if (keyBuilder.length() == 0)
         return null;
      return keyBuilder.toString();
   }
}
