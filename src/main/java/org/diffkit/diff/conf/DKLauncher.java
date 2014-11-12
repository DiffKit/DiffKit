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
package org.diffkit.diff.conf;

import com.jdotsoft.jarloader.JarClassLoader;

/**
 * @author jpanico
 */
public class DKLauncher {
   private static final String TOOL_CLASS_NAME = "org.diffkit.diff.conf.DKApplication";

   public static void main(String[] args_) {
      JarClassLoader jcl = new JarClassLoader();
      try {
         jcl.invokeMain(TOOL_CLASS_NAME, args_);
      }
      catch (Throwable e) {
         e.printStackTrace();
      }
   }

}
