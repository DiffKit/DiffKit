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
package org.diffkit.diff.engine;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import org.apache.commons.collections.OrderedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;
import org.diffkit.common.annot.NotThreadSafe;
import org.diffkit.common.annot.Stateless;
import org.diffkit.diff.engine.DKContext.UserKey;

/**
 * @author jpanico
 */
@NotThreadSafe
@Stateless
public class DKDiffEngine {
   private static final long PROGRESS_BATCH_SIZE = 1000;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());
   private static final Logger USER_LOG = LoggerFactory.getLogger("user");
   private final boolean _isDebug = _log.isDebugEnabled();

   public DKContext diff(DKSource lhs_, DKSource rhs_, DKSink sink_,
                         DKTableComparison tableComparison_,
                         Map<UserKey, ?> userDictionary_) throws IOException {
      _log.info("lhs_->{}", lhs_);
      _log.info("rhs_->{}", rhs_);
      _log.info("sink_->{}", sink_);
      _log.info("tableComparison_->{}", tableComparison_.getDescription());
      _log.debug("userDictionary_->{}", userDictionary_);

      DKValidate.notNull(lhs_, rhs_, sink_, tableComparison_);
      DKContext context = new DKContext(lhs_, rhs_, sink_, tableComparison_,
         userDictionary_);
      _log.info("context->{}", context);
      this.diff(context);
      return context;
   }

   private void diff(DKContext context_) throws IOException {
      long maxDiffs = context_._tableComparison.getMaxDiffs();
      _log.info("maxDiffs->{}", maxDiffs);
      context_.open();
      int oneSide = -1;
      Object[][] rows = new Object[2][];
      Comparator<Object[]> rowComparator = context_._tableComparison.getRowComparator();
      _log.info("rowComparator->{}", rowComparator);
      while (context_._sink.getDiffCount() < maxDiffs) {
         if (_isDebug)
            _log.debug("diffCount->{}", context_._sink.getDiffCount());
         boolean oneSided = false;
         context_._rowStep++;
         context_._columnStep = 0;
         if (context_._rowStep % PROGRESS_BATCH_SIZE == 0)
            USER_LOG.info("->{}", context_._rowStep);
         if (rows[DKSide.LEFT_INDEX] == null)
            rows[DKSide.LEFT_INDEX] = context_._lhs.getNextRow();
         if (rows[DKSide.LEFT_INDEX] == null) {
            oneSided = true;
            oneSide = DKSide.RIGHT_INDEX;
         }
         if (rows[DKSide.RIGHT_INDEX] == null)
            rows[DKSide.RIGHT_INDEX] = context_._rhs.getNextRow();
         if (rows[DKSide.RIGHT_INDEX] == null) {
            if (oneSided)
               break;
            oneSided = true;
            oneSide = DKSide.LEFT_INDEX;
         }
         if (_isDebug) {
            _log.debug("oneSided->{}", oneSided);
            _log.debug("oneSide->{}", oneSide);
         }
         if (oneSided) {
            this.recordRowDiff(rows[oneSide], DKSide.flip(oneSide), context_,
               context_._sink);
            rows[oneSide] = null;
            continue;
         }
         assert ((rows[DKSide.LEFT_INDEX] != null) && (rows[DKSide.RIGHT_INDEX] != null));
         int comparison = rowComparator.compare(rows[DKSide.LEFT_INDEX],
            rows[DKSide.RIGHT_INDEX]);
         // LEFT < RIGHT
         if (comparison < 0) {
            this.recordRowDiff(rows[DKSide.LEFT_INDEX], DKSide.RIGHT_INDEX, context_,
               context_._sink);
            rows[DKSide.LEFT_INDEX] = null;
         }
         // LEFT > RIGHT
         else if (comparison > 0) {
            this.recordRowDiff(rows[DKSide.RIGHT_INDEX], DKSide.LEFT_INDEX, context_,
               context_._sink);
            rows[DKSide.RIGHT_INDEX] = null;
         }
         // at this point you know the keys are aligned
         else {
            this.diffRow(rows[DKSide.LEFT_INDEX], rows[DKSide.RIGHT_INDEX], context_,
               context_._sink);
            rows[DKSide.LEFT_INDEX] = null;
            rows[DKSide.RIGHT_INDEX] = null;
         }
      }
      context_.close();
   }

   private void diffRow(Object[] lhs_, Object[] rhs_, DKContext context_, DKSink sink_)
      throws IOException {
      DKDiff.Kind kind = context_._tableComparison.getKind();
      if (kind == DKDiff.Kind.ROW_DIFF)
         return;
      int[] diffIndexes = context_._tableComparison.getDiffIndexes();
      _log.debug("diffIndexes->{}", Arrays.toString(diffIndexes));
      if ((diffIndexes == null) || (diffIndexes.length == 0))
         return;
      DKColumnComparison[] columnComparisons = context_._tableComparison.getMap();
      _log.debug("columnComparisons->{}", Arrays.toString(columnComparisons));
      // not supposed to happen, but play it safe
      if ((columnComparisons == null) || (columnComparisons.length == 0))
         return;
      DKColumnDiffRow diffRow = null;
      for (int i = 0; i < diffIndexes.length; i++) {
         context_._columnStep++;
         if (columnComparisons[diffIndexes[i]].isDiff(lhs_, rhs_, context_)) {
            if (diffRow == null)
               // key side arbitrary; keyValeus guaranteed to match on both
               // sides
               diffRow = new DKColumnDiffRow(context_._rowStep,
                  context_._tableComparison.getRowKeyValues(lhs_, DKSide.LEFT_INDEX),
                  context_._tableComparison.getRowDisplayValues(lhs_, rhs_),
                  context_._tableComparison);
            DKColumnDiff columnDiff = diffRow.createDiff(context_._columnStep,
               columnComparisons[diffIndexes[i]].getLHValue(lhs_),
               columnComparisons[diffIndexes[i]].getRHValue(rhs_));
            sink_.record(columnDiff, context_);
         }
      }
   }

   private void recordRowDiff(Object[] row_, int sideIdx_, DKContext context_,
                              DKSink sink_) throws IOException {
      _log.debug("row_->{} sideIdx_", row_, sideIdx_);
      DKDiff.Kind kind = context_._tableComparison.getKind();
      if (kind == DKDiff.Kind.COLUMN_DIFF)
         return;
      if (row_ == null)
         return;
      DKTableComparison tableComparison = context_.getTableComparison();
      long rowStep = context_.getRowStep();
      Object[] rowKeyValues = tableComparison.getRowKeyValues(row_, sideIdx_);
      OrderedMap rowDisplayValues = tableComparison.getRowDisplayValues(row_, sideIdx_);
      DKSide side = DKSide.getEnumForConstant(sideIdx_);
      DKRowDiff rowDiff = new DKRowDiff(rowStep, rowKeyValues, rowDisplayValues, side,
         tableComparison);
      sink_.record(rowDiff, context_);
   }

}
