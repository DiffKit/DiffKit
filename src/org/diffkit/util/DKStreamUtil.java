/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jpanico
 */
public class DKStreamUtil {

   private static final Logger LOG = LoggerFactory.getLogger(DKStreamUtil.class);

   private static final int DEFAULT_BUFFER_SIZE = 1024;

   public static BufferedInputStream ensureBuffered(InputStream inputStream_) {
      if (inputStream_ == null)
         return null;
      if (inputStream_ instanceof BufferedInputStream)
         return (BufferedInputStream) inputStream_;

      return new BufferedInputStream(inputStream_);
   }

   public static BufferedOutputStream ensureBuffered(OutputStream outputStream_) {
      if (outputStream_ == null)
         return null;
      if (outputStream_ instanceof BufferedOutputStream)
         return (BufferedOutputStream) outputStream_;

      return new BufferedOutputStream(outputStream_);
   }

   public static void close(InputStream inputStream_) {
      if (inputStream_ == null)
         return;

      try {
         inputStream_.close();
      }
      catch (Exception e_) {
         LOG.warn(null, e_);
      }
   }

   /**
    * @param inputStream_
    *           closed
    */
   public static String readFullyAsString(InputStream inputStream_) {
      if (inputStream_ == null)
         return null;
      inputStream_ = ensureBuffered(inputStream_);
      if (inputStream_ == null)
         return null;

      InputStreamReader inputReader = new InputStreamReader(inputStream_);
      StringBuffer stringBuffer = new StringBuffer();
      char[] charBuffer = new char[DEFAULT_BUFFER_SIZE];

      try {
         int bytesRead = -1;
         while ((bytesRead = inputReader.read(charBuffer)) != -1) {
            stringBuffer.append(charBuffer, 0, bytesRead);
         }
      }
      catch (IOException e_) {
         LOG.error(null, e_);
      }
      finally {
         close(inputStream_);
      }

      if (stringBuffer.length() == 0)
         return null;
      return stringBuffer.toString();
   }
}
