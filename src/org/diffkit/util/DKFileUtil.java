/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jpanico
 */
public class DKFileUtil {

   private static final Logger LOG = LoggerFactory.getLogger(DKFileUtil.class);

   public static boolean exists(String filePath_) {
      if (filePath_ == null)
         return false;
      File file = new File(filePath_);
      return file.exists();
   }

   public static String readFullyAsString(File file_) {
      if (file_ == null)
         return null;

      try {
         FileInputStream fileInputStream = new FileInputStream(file_);
         return DKStreamUtil.readFullyAsString(fileInputStream);
      }
      catch (IOException e_) {
         LOG.error(null, e_);
         return null;
      }
   }
}
