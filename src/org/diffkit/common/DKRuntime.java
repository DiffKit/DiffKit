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

import java.io.File;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.BooleanUtils;

/**
 * @author jpanico
 */
public class DKRuntime {
   public static final String IS_TEST_PROPERTY = "isTest";
   public static final Boolean IS_TEST;
   public static final String DIFFKIT_HOME_PROPERTY = "DIFFKIT_HOME";
   private static final String NULL_STRING = new String();

   private static final DKRuntime _instance = new DKRuntime();

   private String _applicationName;
   private File _diffKitHome;

   static {
      IS_TEST = BooleanUtils.toBoolean(System.getProperty(IS_TEST_PROPERTY));
   }

   private DKRuntime() {
   }

   public static DKRuntime getInstance() {
      return _instance;
   }

   public File getDiffKitHome() {
      if (_diffKitHome != null)
         return _diffKitHome;
      _diffKitHome = this.findDiffKitHome();
      return _diffKitHome;
   }

   /**
    * a 3 level cascade <br/>
    * 1) if DIFFKIT_HOME_PROPERTY Java property is set, use that. Property
    * always overrides any other setting <br/>
    * 2) if the receiver was loaded from an executable jar, and the name of that
    * jar matches (contains) the .applicationName, then use the dir that
    * (application) jar is in. <br/>
    * 3) CWD (user.dir).
    */
   private File findDiffKitHome() {
      String propertyValue = System.getProperty(DIFFKIT_HOME_PROPERTY);
      if (propertyValue != null)
         return new File(propertyValue);
      File applicationLocation = this.findApplicationLocation();
      if (applicationLocation != null)
         return applicationLocation;
      return new File(System.getProperty("user.dir"));
   }

   private File findApplicationLocation() {
      String applicationName = this.getApplicationName();
      if (applicationName == null)
         return null;
      URL location = this.getClassLocation();
      if (location == null)
         return null;
      if (!location.toExternalForm().toUpperCase().contains(applicationName.toUpperCase()))
         return null;
      File applicationFile = FileUtils.toFile(location);
      return applicationFile.getParentFile();
   }

   private URL getClassLocation() {
      ProtectionDomain protectionDomain = this.getClass().getProtectionDomain();
      CodeSource codeSource = protectionDomain.getCodeSource();
      return codeSource.getLocation();
   }

   public String getApplicationName() {
      if (_applicationName == NULL_STRING)
         return null;
      return _applicationName;
   }

   public void setApplicationName(String applicationName_) {
      if (_applicationName != null)
         throw new IllegalStateException(String.format(
            "applicationName_ already assigned->%s cannot be reassigned to->%s",
            _applicationName, applicationName_));
      if (applicationName_ == null)
         _applicationName = NULL_STRING;
      else
         _applicationName = applicationName_;
   }
}
