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

import org.diffkit.common.annot.NotThreadSafe;
import org.diffkit.diff.engine.DKColumnDiff;
import org.diffkit.diff.engine.DKColumnDiffRow;
import org.diffkit.diff.engine.DKContext;
import org.diffkit.diff.engine.DKDiff;
import org.diffkit.diff.engine.DKRowDiff;
import org.diffkit.diff.engine.DKSide;

/**
 * @author jpanico
 */
@NotThreadSafe
public class DKDefaultFormatter implements DKDiffFormatter {

   private static final DKDefaultFormatter INSTANCE = new DKDefaultFormatter();
   private DKColumnDiffRow _runningRow;

   public static DKDefaultFormatter getInstance() {
      return INSTANCE;
   }

   public String format(DKDiff diff_, DKContext context_) {
      if (diff_ == null)
         return null;

      switch (diff_.getKind()) {
      case ROW_DIFF:
         return this.formatRowDiff((DKRowDiff) diff_, context_);
      case COLUMN_DIFF:
         return this.formatColumnDiff((DKColumnDiff) diff_, context_);

      default:
         throw new IllegalArgumentException(String.format("unrecognized kind->%s",
            diff_.getKind()));
      }
   }

   private String formatRowDiff(DKRowDiff diff_, DKContext context_) {
      StringBuilder builder = new StringBuilder();
      DKSide side = diff_.getSide();
      builder.append("!" + diff_.getRowDisplayValues().toString() + "\n");
      builder.append((side == DKSide.LEFT ? ">" : "<"));
      return builder.toString();
   }

   private String formatColumnDiff(DKColumnDiff diff_, DKContext context_) {
      StringBuilder builder = new StringBuilder();
      DKColumnDiffRow row = diff_.getRow();
      if (row != _runningRow) {
         _runningRow = row;
         builder.append("@" + row.getRowDisplayValues().toString() + "\n");
      }
      builder.append(diff_.getColumnName() + "\n");
      builder.append("<" + diff_.getLhs() + "\n");
      builder.append(">" + diff_.getRhs() + "");
      return builder.toString();
   }
}
