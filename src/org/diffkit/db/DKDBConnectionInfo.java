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

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.NotImplementedException;

import org.diffkit.common.DKValidate;

/**
 * @author jpanico
 */
public class DKDBConnectionInfo {
   private final String _name;
   private final DKDBFlavor _flavor;
   private final String _database;
   private final String _host;
   private final Long _port;
   private final String _username;
   private final String _password;

   public DKDBConnectionInfo(String name_, DKDBFlavor kind_, String database_,
                             String host_, Long port_, String username_, String password_) {
      _name = name_;
      _flavor = kind_;
      _database = database_;
      _host = host_;
      _port = port_;
      _username = username_;
      _password = password_;

      DKValidate.notNull(_name, _flavor, _database, _username, _password);
   }

   public DKDBFlavor getFlavor() {
      return _flavor;
   }

   public String getJDBCUrl() {
      switch (_flavor) {
      case H2:
         return this.getH2Url();
      case DB2:
         return this.getDB2Url();
      case ORACLE:
         return this.getOracleUrl();

      default:
         throw new NotImplementedException();
      }
   }

   public String getDriverName() {
      return _flavor._driverName;
   }

   private String getH2Url() {
      return "jdbc:h2:" + _database;
   }

   // jdbc:oracle:thin:[username/password]@[//]host_name[:port][/XE]
   private String getOracleUrl() {
      return String.format("jdbc:oracle:thin:@//%s:%s/%s", _host, _port, _database);
   }

   // jdbc:db2://<host>[:<port>]/<database_name>
   private String getDB2Url() {
      return String.format("jdbc:db2://%s:%s/%s", _host, _port, _database);
   }

   public String getUsername() {
      return _username;
   }

   public String getPassword() {
      return _password;
   }

   public String toString() {
      return String.format("%s[%s]", ClassUtils.getShortClassName(this.getClass()),
         this.getJDBCUrl());
   }
}
