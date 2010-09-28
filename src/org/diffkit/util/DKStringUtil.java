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
package org.diffkit.util;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;

/**
 * @author jpanico
 */
public class DKStringUtil {
   public enum Quote {
      SINGLE('\''), DOUBLE('"');

      public final char _character;

      private Quote(char character_) {
         _character = character_;
      }
   }

   private static final Logger LOG = LoggerFactory.getLogger(DKStringUtil.class);

   private DKStringUtil() {
   }

   public static Boolean parseBoolean(String target_) {
      if (target_ == null)
         return null;
      if ((target_.equalsIgnoreCase("true")) || (target_.equalsIgnoreCase("yes")))
         return Boolean.TRUE;
      if ((target_.equalsIgnoreCase("false")) || (target_.equalsIgnoreCase("no")))
         return Boolean.FALSE;
      return null;
   }

   /**
    * null and Exception safe
    */
   public static URI createURI(String target_) {
      if (target_ == null)
         return null;
      try {
         return new URI(target_);
      }
      catch (Exception e_) {
         LOG.error(null, e_);
         return null;
      }
   }

   public static String packageNameToResourcePath(String packageName_) {
      if (packageName_ == null)
         return null;
      return packageName_.replace('.', '/') + "/";
   }

   public static String quote(String target_, Quote kind_) {
      if (target_ == null)
         return null;
      DKValidate.notNull(kind_);
      if (!(target_.length() == 0) && (target_.charAt(0) == kind_._character))
         return target_;
      return kind_._character + target_ + kind_._character;
   }

   /**
    * parenthesis bracketed, comma separated
    */
   public static String toSetString(List<String> target_) {
      if (target_ == null)
         return "()";

      StringBuilder builder = new StringBuilder();
      builder.append("(");
      Iterator<String> iterator = target_.iterator();
      while (iterator.hasNext()) {
         builder.append(iterator.next());
         if (iterator.hasNext())
            builder.append(", ");
      }
      builder.append(")");
      return builder.toString();
   }

   /**
    * @param startIndex_
    *           inclusive, as in java.lang.String subrange methods
    * @param endIndex_
    *           exclusive, as in java.lang.String subrange methods
    */
   public static int countMatches(String target_, char match_, int startIndex_,
                                  int endIndex_) {
      if (target_ == null)
         return 0;
      int targetLength = target_.length();
      if (startIndex_ < 0 || startIndex_ >= targetLength)
         throw new IllegalArgumentException(String.format(
            "startIndex_->%s out of bounds", startIndex_));
      if (endIndex_ < 0 || endIndex_ > targetLength)
         throw new IllegalArgumentException(String.format("endIndex_->%s out of bounds",
            startIndex_));
      if (startIndex_ >= endIndex_)
         return 0;

      char[] chars = target_.toCharArray();
      if (chars == null)
         return 0;

      int count = 0;
      for (int i = startIndex_; i < endIndex_; i++)
         if (chars[i] == match_)
            count++;
      return count;
   }

//   public static String replaceEach(String target_, Map<String, String> replacements_) {
//      if (target_ == null)
//         return null;
//      if (MapUtils.isEmpty(replacements_))
//         return target_;
//   }
}
