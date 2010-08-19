/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jpanico
 */
public class DKFileUtil {

   private static final Logger LOG = LoggerFactory.getLogger(DKFileUtil.class);

   public static boolean isRelative(File target_) {
      if (target_ == null)
         return false;
      String path = target_.getPath();
      if (path == null)
         return false;
      return path.startsWith(".");
   }

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

   /**
    * will throw an exception if file already exists
    */
   public static void writeContents(File file_, byte[] contents_) throws IOException {
      if ((file_ == null) || (contents_ == null))
         return;

      if (!file_.createNewFile())
         throw new IOException(String.format(
            "can't write to '%s', probably a file already exists there.", file_));

      OutputStream outStream = new BufferedOutputStream(new FileOutputStream(file_));
      for (int i = 0; i < contents_.length; i++) {
         outStream.write(contents_[i]);
      }
      outStream.flush();
      outStream.close();
   }
}
