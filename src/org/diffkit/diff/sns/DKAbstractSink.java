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

import org.diffkit.diff.engine.DKContext;
import org.diffkit.diff.engine.DKDiff;
import org.diffkit.diff.engine.DKSink;

/**
 * @author jpanico
 */
public abstract class DKAbstractSink implements DKSink {

   private boolean _isStarted;
   private boolean _isEnded;
   private DKContext _context;
   private long _rowDiffCount;
   private long _columnDiffCount;

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
         _rowDiffCount++;
      else if (diff_.getKind() == DKDiff.Kind.COLUMN_DIFF)
         _columnDiffCount++;
   }

   public long getRowDiffCount() {
      return _rowDiffCount;
   }

   public long getColumnDiffCount() {
      return _columnDiffCount;
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

}
