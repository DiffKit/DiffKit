/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
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
    * closes the inputStream before returning
    */
   public static byte[] readFully(InputStream inputStream_) throws IOException {
      if (inputStream_ == null)
         return null;
      inputStream_ = ensureBuffered(inputStream_);
      if (inputStream_ == null)
         return null;

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      copy(inputStream_, outputStream);
      inputStream_.close();
      return outputStream.toByteArray();
   }

   /**
    * calls copy(InputStream, OutputStream, int) with DEFAULT_BUFFER_SIZE
    */
   public static void copy(InputStream input_, OutputStream output_) throws IOException {
      copy(input_, output_, DEFAULT_BUFFER_SIZE);
   }

   /**
    * Copy chars from a <code>InputStream</code> to a <code>OutputStream</code>.
    * 
    * @param bufferSize_
    *           Size of internal buffer to use.
    */
   public static void copy(InputStream input_, OutputStream output_, int bufferSize_)
      throws IOException {
      if ((input_ == null) || (output_ == null))
         return;
      byte[] buffer = new byte[bufferSize_];
      int bytesRead = -1;
      while ((bytesRead = input_.read(buffer, 0, buffer.length)) != -1)
         output_.write(buffer, 0, bytesRead);
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
