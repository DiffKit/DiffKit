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
package org.diffkit.diff.sns;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ClassUtils;

import org.diffkit.common.annot.ThreadSafe;
import org.diffkit.diff.engine.DKContext;
import org.diffkit.diff.engine.DKDiff;
import org.diffkit.util.DKObjectUtil;
import org.diffkit.util.DKStringUtil;

/**
 * @author jpanico
 */
@ThreadSafe
public class DKListSink extends DKAbstractSink {

   private List<DKDiff> _diffs;

   public DKListSink() {
      super(null);
   }

   public void record(DKDiff diff_, DKContext context_) {
      this.ensureStarted();
      this.ensureNotEnded();
      if (diff_ == null)
         return;
      if (_diffs == null)
         _diffs = new ArrayList<DKDiff>();
      synchronized (_diffs) {
         _diffs.add(diff_);
      }
   }

   // @Override
   public Kind getKind() {
      return Kind.MEMORY;
   }

   public List<DKDiff> getDiffs() {
      return _diffs;
   }

   @Override
   public long getDiffCount() {
      if (_diffs == null)
         return 0;
      return _diffs.size();
   }

   @Override
   public long getRowDiffCount() {
      List<DKDiff> rowDiffs = DKDiffUtil.getRowDiffs(this.getDiffs());
      if (rowDiffs == null)
         return 0;
      return rowDiffs.size();
   }

   @Override
   public long getColumnDiffCount() {
      List<DKDiff> columnDiffs = DKDiffUtil.getColumnDiffs(this.getDiffs());
      if (columnDiffs == null)
         return 0;
      return columnDiffs.size();
   }

   public URI getURI() {
      return DKStringUtil.createURI(String.format("heap://%s",
         DKObjectUtil.getAddressHexString(this)));
   }

   public String toString() {
      return String.format("%s[%s, diffCount=%s]",
         ClassUtils.getShortClassName(this.getClass()), this.getURI(),
         this.getDiffCount());
   }

   public String generateSummary(DKContext context_) {
      return "summary";
   }
}
