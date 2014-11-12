/**
 * Copyright 2010-2011 Joseph Panico
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

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jpanico
 */
public class DKDistProperties {
   private static final String FILE_RESOURCE_PATH = "conf/dist.properties";

   private static final Logger LOG = LoggerFactory.getLogger(DKDistProperties.class);

   private static Properties _properties;
   private static String _versionString;
   private static String _publicVersionString;
   private static String _buildDateString;

   public synchronized static String getVersionString() {
      if (_versionString != null)
         return _versionString;

      Properties properties = getProperties();
      if (properties == null) {
         LOG.warn("couldn't find properties");
         return null;
      }
      String versionBuild = properties.getProperty("version.build");
      _versionString = getPublicVersionString() + "-" + versionBuild;

      return _versionString;
   }

   public synchronized static String getPublicVersionString() {
      if (_publicVersionString != null)
         return _publicVersionString;

      Properties properties = getProperties();
      if (properties == null) {
         LOG.warn("couldn't find properties");
         return null;
      }
      String versionMajor = properties.getProperty("version.major");
      String versionMinor = properties.getProperty("version.minor");
      String versionMicro = properties.getProperty("version.micro");
      _publicVersionString = versionMajor + "." + versionMinor + "." + versionMicro;

      return _publicVersionString;
   }

   public synchronized static String getBuildDateString() {
      if (_buildDateString != null)
         return _buildDateString;

      Properties properties = getProperties();
      if (properties == null) {
         LOG.warn("couldn't find properties");
         return null;
      }
      _buildDateString = properties.getProperty("version.date");
      return _buildDateString;
   }

   private static Properties getProperties() {
      if (_properties != null)
         return _properties;

      ClassLoader classLoader = DKDistProperties.class.getClassLoader();
      try {
         InputStream propertiesStream = classLoader.getResourceAsStream(FILE_RESOURCE_PATH);
         if (propertiesStream == null) {
            LOG.warn("couldn't not find properties file: " + FILE_RESOURCE_PATH);
            return null;
         }
         _properties = new Properties();
         _properties.load(propertiesStream);
         LOG.debug("_properties: " + _properties);
         return _properties;
      }
      catch (Exception e_) {
         LOG.error(null, e_);
         return null;
      }
   }
}
