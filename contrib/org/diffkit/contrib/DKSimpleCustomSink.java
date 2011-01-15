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
package org.diffkit.contrib;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKRuntime;
import org.diffkit.diff.engine.DKContext;
import org.diffkit.diff.engine.DKContext.UserKey;
import org.diffkit.diff.sns.DKWriterSink;

/**
 * @author jpanico
 */
public class DKSimpleCustomSink extends DKWriterSink {

   private final File _file;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   public DKSimpleCustomSink(String filePath_) throws IOException {
      super(null);
      _file = new File(filePath_);
      _log.debug("_file->{}", _file);
   }

   @Override
   public Kind getKind() {
      return Kind.FILE;
   }

   public File getFile() {
      return _file;
   }

   @Override
   public void open(DKContext context_) throws IOException {
      this.init(new BufferedWriter(new FileWriter(_file)), this.getFormatter());
      super.open(context_);
      Writer writer = super.getWriter();
      writer.write(String.format("this header placed by->%s\n", this.getClass()));
   }

   public String toString() {
      if (DKRuntime.getInstance().getIsTest())
         return _file.getName();
      return String.format("%s@%x[%s]", ClassUtils.getShortClassName(this.getClass()),
         System.identityHashCode(this), _file.getPath());
   }

   @Override
   public void close(DKContext context_) throws IOException {
      super.close(context_);
   }

   public String generateHeader(DKContext context_) {
      return String.format("%s\n%s", this.generateContextDescription(context_),
         this.generateSummary(context_));
   }

   public String generateContextDescription(DKContext context_) {
      StringBuilder builder = new StringBuilder();
      String startTimeString = DKRuntime.getInstance().getIsTest() ? "xxx"
         : new SimpleDateFormat().format(context_.getDiffStartTime());
      builder.append(String.format("diff start->%s\n", startTimeString));
      String elapsedTimeString = DKRuntime.getInstance().getIsTest() ? "xxx"
         : context_.getElapsedTimeString();
      builder.append(String.format("diff elapsed time->%s\n", elapsedTimeString));
      builder.append(String.format("lhs->%s\n", context_.getLhs()));
      builder.append(String.format("rhs->%s\n", context_.getRhs()));
      builder.append(String.format("sink->%s\n", context_.getSink()));
      builder.append(String.format("kind->%s\n", context_.getTableComparison().getKind()));
      builder.append(String.format("maxDiffs->%s\n",
         context_.getTableComparison().getMaxDiffs()));
      UserKey[] userKeys = DKContext.UserKey.class.getEnumConstants();
      Map<UserKey, ?> userDictionary = context_.getUserDictionary();
      for (UserKey userKey : userKeys) {
         if (userDictionary.containsKey(userKey))
            builder.append(String.format("%s=%s\n", userKey.toString().toLowerCase(),
               userDictionary.get(userKey)));
      }
      return builder.toString();
   }
}
