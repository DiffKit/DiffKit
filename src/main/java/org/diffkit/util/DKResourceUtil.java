/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jpanico
 */
public class DKResourceUtil {
   private static final Logger LOG = LoggerFactory.getLogger(DKResourceUtil.class);

   private static File[] _resourceDirs;

   private DKResourceUtil() {
   }

   public static synchronized void appendResourceDir(File dir_) {
      if (dir_ == null)
         return;
      _resourceDirs = (File[]) ArrayUtils.add(_resourceDirs, dir_);
   }

   public static synchronized void prependResourceDir(File dir_) {
      if (dir_ == null)
         return;
      _resourceDirs = (File[]) ArrayUtils.add(_resourceDirs, 0, dir_);
   }

   public static synchronized File[] getResourceDirs() {
      return _resourceDirs;
   }

   public static String getResourceContents(String resource_) {
      LOG.debug("resource_->{}", resource_);
      if (resource_ == null)
         return null;
      URL resourceURL = findResource(resource_);
      LOG.debug("resourceURL->{}", resourceURL);

      try {
         InputStream inputStream = resourceURL.openStream();
         return DKStreamUtil.readFullyAsString(inputStream);
      }
      catch (IOException e_) {
         LOG.error(null, e_);
         return null;
      }
   }

   public static byte[] getResourceBytes(String resource_) {
      LOG.debug("resource_->{}", resource_);
      if (resource_ == null)
         return null;
      URL resourceURL = findResource(resource_);
      LOG.debug("resourceURL->{}", resourceURL);

      try {
         InputStream inputStream = resourceURL.openStream();
         return DKStreamUtil.readFully(inputStream);
      }
      catch (IOException e_) {
         LOG.error(null, e_);
         return null;
      }
   }

   /**
    * uses the ClassLoader of the receiver
    */
   public static URL findResource(String resource_) {
      LOG.debug("resource_->{}", resource_);
      if (resource_ == null)
         return null;
      ClassLoader classLoader = DKResourceUtil.class.getClassLoader();
      LOG.debug("classLoader->{}", classLoader);
      if (classLoader instanceof URLClassLoader)
         LOG.debug("classLoader urls->{}",
            Arrays.toString(((URLClassLoader) classLoader).getURLs()));
      URL resource = classLoader.getResource(resource_);
      LOG.debug("resource->{}", resource);
      return resource;
   }

   /**
    * convenience method that calls findResource(String)
    */
   public static boolean resourceExists(String resource_) {
      return (findResource(resource_) != null);
   }

   /**
    * convenience that just calls findResourceAsFile(String)
    */
   public static File[] findResourcesAsFiles(String[] resources_)
      throws URISyntaxException {
      if (ArrayUtils.isEmpty(resources_))
         return null;
      List<File> found = new ArrayList<File>(resources_.length);
      for (String resource : resources_) {
         File resourceFile = findResourceAsFile(resource);
         if (resourceFile == null)
            continue;
         found.add(resourceFile);
      }
      return found.toArray(new File[found.size()]);
   }

   /**
    * assumes that File named fileName_ and the caller are in the same location
    * on the classpath<br/>
    * 
    * uses findResourceAsFile(String)
    */
   public static File findResourceAsFile(String fileName_, Object caller_)
      throws URISyntaxException {
      LOG.debug("fileName_->{}", fileName_);
      LOG.debug("caller_->{}", caller_);
      String resourceFilePath = ClassUtils.getPackageName(caller_.getClass());
      resourceFilePath = DKStringUtil.packageNameToResourcePath(resourceFilePath)
         + fileName_;
      LOG.debug("resourceFilePath->{}", resourceFilePath);
      File sourceFile = findResourceAsFile(resourceFilePath);
      LOG.debug("sourceFile->{}", sourceFile);
      return sourceFile;
   }

   /**
    * uses findResource(String)
    */

   public static File findResourceAsFile(String resource_) throws URISyntaxException {
      File localFile = findLocalResource(resource_);
      if (localFile != null)
         return localFile;

      URL resourceUrl = findResource(resource_);
      LOG.debug("resourceUrl->{}", resourceUrl);
      if (resourceUrl == null)
         return null;
      return new File(resourceUrl.toURI());
   }

   private static File findLocalResource(String resource_) {
      if (resource_ == null)
         return null;
      if (_resourceDirs == null)
         return null;
      for (File resourceDir : _resourceDirs) {
         File resource = new File(resourceDir, resource_);
         if (resource.exists())
            return resource;
      }
      return null;
   }
}
