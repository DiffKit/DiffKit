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
package org.diffkit.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.util.DKStreamUtil;
import org.diffkit.util.DKStringUtil;

/**
 * @author jpanico
 */
public class DKUnjar {
   private static final Logger LOG = LoggerFactory.getLogger(DKUnjar.class);

   /**
    * closes inputStream_ at the end
    */
   public static void unjar(JarInputStream inputStream_, File outputDir_)
      throws IOException {
      DKValidate.notNull(inputStream_, outputDir_);
      if (!outputDir_.isDirectory())
         throw new RuntimeException(String.format("directory does not exist->%s",
            outputDir_));

      JarEntry entry = null;
      while ((entry = inputStream_.getNextJarEntry()) != null) {
         File outFile = new File(outputDir_, entry.getName());
         OutputStream outStream = new BufferedOutputStream(new FileOutputStream(outFile));
         DKStreamUtil.copy(inputStream_, outStream);
         outStream.flush();
         outStream.close();
      }
      inputStream_.close();
   }

   /**
    * N.B. this method is much less efficient than the similar signature without
    * substitutions, so use that if you don't really need substitutions <br>
    * 
    * closes inputStream_ at the end
    */
   public static void unjar(JarInputStream inputStream_, File outputDir_,
                            Map<String, String> substitutions_) throws IOException {
      boolean isDebugEnabled = LOG.isDebugEnabled();
      if (isDebugEnabled)
         LOG.debug("substitutions_->{}", substitutions_);

      if (MapUtils.isEmpty(substitutions_))
         unjar(inputStream_, outputDir_);

      DKValidate.notNull(inputStream_, outputDir_);
      if (!outputDir_.isDirectory())
         throw new RuntimeException(String.format("directory does not exist->%s",
            outputDir_));

      JarEntry entry = null;
      while ((entry = inputStream_.getNextJarEntry()) != null) {
         String contents = IOUtils.toString(inputStream_);
         if (isDebugEnabled)
            LOG.debug("contents->{}", contents);
         contents = DKStringUtil.replaceEach(contents, substitutions_);
         if (isDebugEnabled)
            LOG.debug("substituted contents->{}", contents);
         File outFile = new File(outputDir_, entry.getName());
         FileUtils.writeStringToFile(outFile, contents);
      }
      inputStream_.close();
   }
}
