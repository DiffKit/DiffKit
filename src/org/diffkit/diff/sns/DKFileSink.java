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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKUserException;
import org.diffkit.common.DKValidate;
import org.diffkit.diff.engine.DKContext;
import org.diffkit.util.DKFileUtil;

/**
 * @author jpanico
 */
public class DKFileSink extends DKWriterSink {

   private final File _file;
   private final boolean _withSummary;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   public DKFileSink(String filePath_) throws IOException {
      this(new File(filePath_), false, null);
   }

   public DKFileSink(String newFilePath_, DKFileSink toClone_) throws IOException {
      this(newFilePath_, toClone_.getWithSummary(), toClone_.getGroupByColumnNames());
   }

   public DKFileSink(String filePath_, Boolean withSummary_, String[] groupByColumnNames_)
      throws IOException {
      this(new File(filePath_), withSummary_, groupByColumnNames_);
   }

   private DKFileSink(File file_, boolean withSummary_, String[] groupByColumnNames_) {
      super(groupByColumnNames_);
      DKValidate.notNull(file_);
      if (file_.exists())
         throw new DKUserException(String.format(
            "sink file [%s] already exists! please remove it and try again.", file_));
      _file = file_;
      _withSummary = withSummary_;
      _log.debug("_file->{}", _file);
      _log.debug("_withSummary->{}", _withSummary);
   }

   @Override
   public Kind getKind() {
      return Kind.FILE;
   }

   public File getFile() {
      return _file;
   }

   public Boolean getWithSummary() {
      return _withSummary;
   }

   @Override
   public void open(DKContext context_) throws IOException {
      this.init(new BufferedWriter(new FileWriter(_file)), this.getFormatter());
      super.open(context_);
   }

   public String toString() {
      return String.format("%s@%x[%s]", ClassUtils.getShortClassName(this.getClass()),
         System.identityHashCode(this), _file.getAbsolutePath());
   }

   @Override
   public void close(DKContext context_) throws IOException {
      super.close(context_);
      if (_withSummary)
         DKFileUtil.prepend(this.generateHeader(context_), _file);
   }

   public String generateHeader(DKContext context_) {
      return String.format("%s\n%s", this.generateContextDescription(context_),
         this.generateSummary(context_));
   }

   public String generateContextDescription(DKContext context_) {
      return "context";
   }
}
