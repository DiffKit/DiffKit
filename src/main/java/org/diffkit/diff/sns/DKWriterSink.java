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

import java.io.IOException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;
import org.diffkit.common.annot.NotThreadSafe;
import org.diffkit.diff.engine.DKContext;
import org.diffkit.diff.engine.DKDiff;

/**
 * @author jpanico
 */
@NotThreadSafe
public class DKWriterSink extends DKAbstractSink {

   private Writer _writer;
   private DKDiffFormatter _formatter;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   protected DKWriterSink(String[] groupByColumnNames_) {
      super(groupByColumnNames_);
      _formatter = null;
   }

   public DKWriterSink(Writer writer_, DKDiffFormatter formatter_) throws IOException {
      super(null);
      this.init(writer_, formatter_);
   }

   /**
    * designated initializer
    */
   protected void init(Writer writer_, DKDiffFormatter formatter_) {
      _writer = writer_;
      _formatter = (formatter_ == null ? DKDefaultFormatter.getInstance() : formatter_);
      DKValidate.notNull(_writer, _formatter);
   }

   // @Override
   public Kind getKind() {
      return Kind.STREAM;
   }

   public Writer getWriter() {
      return _writer;
   }

   public DKDiffFormatter getFormatter() {
      return _formatter;
   }

   public void record(DKDiff diff_, DKContext context_) throws IOException {
      super.record(diff_, context_);
      if (diff_ == null)
         return;
      String diffString = _formatter.format(diff_, context_);
      if (diffString == null) {
         _log.error(String.format("could not format diff->%s", diff_));
         return;
      }
      _writer.write(diffString + "\n");
   }

   public void open(DKContext context_) throws IOException {
      super.open(context_);
      _log.info("_writer->{}", _writer);
   }

   public void close(DKContext context_) throws IOException {
      super.close(context_);
      _writer.close();
      _writer = null;
   }

}
