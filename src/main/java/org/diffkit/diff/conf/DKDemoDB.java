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

import java.io.File;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.sql.SQLException;

import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.db.DKDBConnectionInfo;
import org.diffkit.db.DKDatabase;
import org.diffkit.db.DKDBTable;
import org.diffkit.db.DKDBTableDataAccess;
import org.diffkit.util.DKSpringUtil;

/**
 * @author jpanico
 */
public class DKDemoDB {

   private static final String PROBE_TABLE_NAME = "TEST10_LHS_TABLE";
   private static final String CONNECTION_INFO_CONFIG_FILE_PATH = "./dbConnectionInfo.xml";

   private static final Logger USER_LOG = LoggerFactory.getLogger("user");
   private static final Logger LOG = LoggerFactory.getLogger(DKApplication.class);

   public static void run() throws Exception {
      USER_LOG.info("running H2 demo db...");
      Server.createTcpServer("-tcp", "-tcpDaemon", "-web", "-webDaemon").start();
      if (!demoDataIsLoaded())
         DKTestBridge.loadTestCaseData(new File("./"));
      USER_LOG.info("press <return> to exit...");
      LineNumberReader lineReader = new LineNumberReader(new InputStreamReader(System.in));
      lineReader.readLine();
   }

   /**
    * based on presence of PROBE_TABLE_NAME
    */
   private static boolean demoDataIsLoaded() throws SQLException {
      DKDBConnectionInfo connectionInfo = (DKDBConnectionInfo) DKSpringUtil.getBean(
         "connectionInfo", new String[] { CONNECTION_INFO_CONFIG_FILE_PATH },
         DKDemoDB.class.getClassLoader());
      if (connectionInfo == null)
         throw new RuntimeException(String.format(
            "cannot find connectionInfo in Spring config file->%s",
            CONNECTION_INFO_CONFIG_FILE_PATH));
      LOG.info("connectionInfo->{}", connectionInfo);
      DKDatabase connectionSource = new DKDatabase(connectionInfo);
      DKDBTableDataAccess tableDataAccess = new DKDBTableDataAccess(connectionSource);
      DKDBTable table = tableDataAccess.getTable(PROBE_TABLE_NAME);
      LOG.info("table->{}", table);
      return (table != null);
   }
}
