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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;

import org.diffkit.common.DKOrderedBag;
import org.diffkit.common.DKProperties;
import org.diffkit.diff.engine.DKColumnDiff;
import org.diffkit.diff.engine.DKContext;
import org.diffkit.diff.engine.DKDiff;
import org.diffkit.diff.engine.DKRowDiff;
import org.diffkit.diff.engine.DKSide;
import org.diffkit.diff.engine.DKSink;
import org.diffkit.util.DKStringUtil;

/**
 * @author jpanico
 */
public abstract class DKAbstractSink implements DKSink {

   private boolean _isStarted;
   private boolean _isEnded;
   private DKContext _context;
   private long[] _rowDiffCount = new long[2];
   private DKOrderedBag _columnDiffCount = new DKOrderedBag();

   public void open(DKContext context_) throws IOException {
      this.ensureNotStarted();
      this.ensureNotEnded();
      _context = context_;
      _isStarted = true;
   }

   public void close(DKContext context_) throws IOException {
      this.ensureStarted();
      this.ensureNotEnded();
      _isEnded = true;
      _context = null;
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
         this.recordRowDiff((DKRowDiff) diff_);
      else if (diff_.getKind() == DKDiff.Kind.COLUMN_DIFF)
         this.recordColumnDiff((DKColumnDiff) diff_);
   }

   private void recordRowDiff(DKRowDiff rowDiff_) {
      _rowDiffCount[DKSide.getConstantForEnum(rowDiff_.getSide())]++;
   }

   private void recordColumnDiff(DKColumnDiff columnDiff_) {
      _columnDiffCount.add(columnDiff_.getColumnName());
   }

   public long getRowDiffCount() {
      return (_rowDiffCount[DKSide.LEFT_INDEX] + _rowDiffCount[DKSide.RIGHT_INDEX]);
   }

   public long getColumnDiffCount() {
      return _columnDiffCount.size();
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

   @SuppressWarnings("unchecked")
   private List<String> getDiffColumnNames() {
      Iterator columnNameIterator = _columnDiffCount.iterator();
      if (columnNameIterator == null)
         return null;
      return (List<String>) IteratorUtils.toList(columnNameIterator);
   }

   public String generateRowDiffSummary(DKContext context_) {
      StringBuilder builder = new StringBuilder();
      builder.append("--- row diff summary ---\n");
      builder.append(String.format("%s row diffs <\n", _rowDiffCount[DKSide.LEFT_INDEX]));
      builder.append(String.format("%s row diffs >\n", _rowDiffCount[DKSide.RIGHT_INDEX]));
      builder.append("------------------------\n");
      return builder.toString();
   }

   public String generateVeryHighLevelSummary(DKContext context_) {
      StringBuilder builder = new StringBuilder();
      builder.append("--- vhl summary ---\n");
      String timeString = (DKProperties.IS_TEST ? "xxx" : context_.getElapsedTimeString());
      builder.append(String.format("diff'd %s rows in %s, found:\n", context_._rowStep,
         timeString));
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
      if (this.getColumnDiffCount() > 0)
         builder.append(this.generateColumnDiffSummary(context_));
      return builder.toString();
   }
}
