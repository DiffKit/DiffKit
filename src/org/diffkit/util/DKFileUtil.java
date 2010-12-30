/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;

/**
 * @author jpanico
 */
public class DKFileUtil {

   private static final String HOLD_SUFFIX = ".__hold__";
   private static final Logger LOG = LoggerFactory.getLogger(DKFileUtil.class);

   /**
    * insert the String prepend_ at the beginning of the content in File target_
    */
   public static void prepend(String prepend_, File target_) throws IOException {
      if ((prepend_ == null) || (target_ == null))
         return;
      if (!(target_.canRead() && (target_.canWrite())))
         throw new RuntimeException(String.format("file is not readable/writeable [%s]",
            target_));
      File holdFile = new File(target_.getParentFile(), target_.getName() + HOLD_SUFFIX);
      target_.renameTo(holdFile);
      OutputStream outStream = toBufferedOutputStream(target_);
      Writer outWriter = new OutputStreamWriter(outStream);
      outWriter.write(prepend_);
      outWriter.flush();

      FileInputStream holdInStream = new FileInputStream(holdFile);
      IOUtils.copy(holdInStream, outStream);
      outStream.flush();
      outStream.close();
      holdFile.delete();
   }

   /**
    * silently eats all exceptions, so only call if you know for a fact that the
    * file is writable, etc.
    */
   public static OutputStream toBufferedOutputStream(File target_) {
      if (target_ == null)
         return null;
      try {
         return new BufferedOutputStream(new FileOutputStream(target_));
      }
      catch (Exception e_) {
         LOG.info(null, e_);
         return null;
      }
   }

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

   public static boolean canReadFilePaths(String[] filePaths_) {
      if (ArrayUtils.isEmpty(filePaths_))
         return false;
      for (String filePath : filePaths_) {
         if (!new File(filePath).canRead())
            return false;
      }
      return true;
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

   public static void copyFile(File srcFile_, File destFile_,
                               Map<String, String> substitutions_) throws IOException {
      DKValidate.notNull(srcFile_, destFile_);
      if (MapUtils.isEmpty(substitutions_) || srcFile_.getName().endsWith("jar") ||
    		  srcFile_.getName().endsWith("xls") || srcFile_.getName().endsWith("xlsx")) {
          LOG.info("Copying from:" + srcFile_.getAbsolutePath() + " to:" + destFile_.getAbsolutePath());    	  
         FileUtils.copyFile(srcFile_, destFile_);
         return;
         
      }

      String contents = FileUtils.readFileToString(srcFile_);
      contents = DKStringUtil.replaceEach(contents, substitutions_);
      FileUtils.writeStringToFile(destFile_, contents);
   }

   /**
    * will not recurse
    */
   public static void copyDirectory(File srcDir_, File destDir_, FileFilter filter_,
                                    Map<String, String> substitutions_)
      throws IOException {
      DKValidate.notNull(srcDir_, destDir_);
      if (MapUtils.isEmpty(substitutions_))
         FileUtils.copyDirectory(srcDir_, destDir_, filter_);
      if (!srcDir_.isDirectory())
         return;
      File[] files = srcDir_.listFiles();
      if (ArrayUtils.isEmpty(files))
         return;
      for (int i = 0; i < files.length; i++) {
         File destFile = new File(destDir_, files[i].getName());
         if (files[i].isDirectory())
            continue;
         copyFile(files[i], destFile, substitutions_);
      }
   }
}
