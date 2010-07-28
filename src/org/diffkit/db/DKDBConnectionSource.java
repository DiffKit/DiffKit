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
package org.diffkit.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;

/**
 * @author jpanico
 */
public class DKDBConnectionSource {

   private final DKDBConnectionInfo _connectionInfo;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   public DKDBConnectionSource(DKDBConnectionInfo connectionInfo_) {
      _connectionInfo = connectionInfo_;
      DKValidate.notNull(_connectionInfo);
   }

   public Connection getConnection() throws SQLException {
      try {
         Class.forName(_connectionInfo.getDriverName());
      }
      catch (ClassNotFoundException e_) {
         throw new SQLException(e_);
      }
      String jdbcUrl = _connectionInfo.getJDBCUrl();
      _log.debug("jdbcUrl->{}", jdbcUrl);
      return DriverManager.getConnection(jdbcUrl);
   }

   public DKDBConnectionInfo getConnectionInfo() {
      return _connectionInfo;
   }
}
