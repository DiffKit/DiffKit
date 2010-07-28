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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jpanico
 */
public class DKResourceUtil {
   private static final Logger LOG = LoggerFactory.getLogger(DKResourceUtil.class);

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

   /**
    * uses the ClassLoader of the receiver
    */
   public static URL findResource(String resource_) {
      LOG.debug("resource_->{}", resource_);
      if (resource_ == null)
         return null;
      ClassLoader classLoader = DKResourceUtil.class.getClassLoader();
      LOG.debug("classLoader->{}", classLoader);
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
    * uses findResource(String)
    */
   public static File findResourceAsFile(String resource_) throws URISyntaxException {
      URL resourceUrl = findResource(resource_);
      LOG.debug("resourceUrl->{}", resourceUrl);
      if (resourceUrl == null)
         return null;
      return new File(resourceUrl.toURI());
   }

}
