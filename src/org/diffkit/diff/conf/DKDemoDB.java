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
package org.diffkit.diff.conf;

import java.io.File;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jpanico
 */
public class DKDemoDB {

   private static final Logger USER_LOG = LoggerFactory.getLogger("user");

   public static void run() throws Exception {
      USER_LOG.info("running H2 demo db...");
      Server.createTcpServer("-tcp", "-tcpDaemon", "-web", "-webDaemon").start();
      DKTestBridge.ensureData(new File("./"));
      USER_LOG.info("press <return> to exit...");
      LineNumberReader lineReader = new LineNumberReader(new InputStreamReader(System.in));
      lineReader.readLine();
   }

}
