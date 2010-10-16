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
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;
import org.diffkit.util.DKSqlUtil;

/**
 * @author jpanico
 */
public class DKDBDatabase {

   private static final String USERNAME_KEY = "user";
   private static final String PASSWORD_KEY = "password";
   private final DKDBConnectionInfo _connectionInfo;
   private final DKDBTypeInfoDataAccess _typeInfoDataAccess;
   private final DKSqlGenerator _sqlGenerator;
   private final DKDBTableDataAccess _tableDataAccess;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   public DKDBDatabase(DKDBConnectionInfo connectionInfo_) {
      _connectionInfo = connectionInfo_;
      _typeInfoDataAccess = new DKDBTypeInfoDataAccess(this);
      _sqlGenerator = new DKSqlGenerator(this);
      _tableDataAccess = new DKDBTableDataAccess(this);
      DKValidate.notNull(_connectionInfo, _typeInfoDataAccess, _sqlGenerator,
         _tableDataAccess);
   }

   public DKDBTypeInfoDataAccess getTypeInfoDataAccess() {
      return _typeInfoDataAccess;
   }

   public Connection getConnection() throws SQLException {
      try {
         Class.forName(_connectionInfo.getDriverName());
      }
      catch (ClassNotFoundException e_) {
         throw new RuntimeException(e_);
      }
      String jdbcUrl = _connectionInfo.getJDBCUrl();
      _log.debug("jdbcUrl->{}", jdbcUrl);
      Properties properties = new Properties();
      properties.put(USERNAME_KEY, _connectionInfo.getUsername());
      properties.put(PASSWORD_KEY, _connectionInfo.getPassword());
      return DriverManager.getConnection(jdbcUrl, properties);
   }

   public DKDBConnectionInfo getConnectionInfo() {
      return _connectionInfo;
   }

   /**
    * convenience that passes through to ConnectionInfo
    */
   public DKDBFlavor getFlavor() {
      return _connectionInfo.getFlavor();
   }

   public boolean canConnect() {
      try {
         Connection connection = this.getConnection();
         if (connection == null)
            return false;
         Map<String, ?> dbInfo = DKSqlUtil.getDatabaseInfo(connection);
         if (MapUtils.isEmpty(dbInfo))
            return false;
         if (dbInfo.get(DKSqlUtil.DATABASE_PRODUCT_VERSION_KEY) == null)
            return false;
         return true;
      }
      catch (Exception e_) {
         _log.debug(null, e_);
         return false;
      }
   }

   /**
    * convenience method
    * 
    * @throws SQLException
    */
   public boolean createTable(DKDBTable table_) throws SQLException {
      _log.debug("table_->{}", table_);
      if (table_ == null)
         return false;
      String createSql = _sqlGenerator.generateCreateDDL(table_);
      _log.debug("createSql->{}", createSql);
      return DKSqlUtil.executeUpdate(createSql, this.getConnection());
   }

   /**
    * convenience method
    * 
    * @throws SQLException
    */
   public boolean dropTable(DKDBTable table_) throws SQLException {
      _log.debug("table_->{}", table_);
      if (table_ == null)
         return false;
      String dropSql = _sqlGenerator.generateDropDDL(table_);
      _log.debug("dropSql->{}", dropSql);
      return DKSqlUtil.executeUpdate(dropSql, this.getConnection());
   }

   public boolean tableExists(DKDBTable table_) throws SQLException {
      _log.debug("table_->{}", table_);
      if (table_ == null)
         return false;
      DKDBTable fetchedTable = _tableDataAccess.getTable(table_.getCatalog(),
         table_.getSchema(), table_.getTableName());
      if (fetchedTable == null)
         return false;
      return true;
   }

   public boolean insertRow(Map<String, ?> row_, DKDBTable table_) throws SQLException {
      if ((table_ == null) || (row_ == null))
         return false;
      String insertSql = _sqlGenerator.generateInsertDML(row_, table_);
      _log.debug("insertSql->{}", insertSql);
      return DKSqlUtil.executeUpdate(insertSql, this.getConnection());
   }

   public List<Map<String, ?>> readAllRows(DKDBTable table_) throws SQLException {
      if (table_ == null)
         return null;
      String selectSql = _sqlGenerator.generateSelectDML(table_);
      _log.debug("selectSql->{}", selectSql);
      return DKSqlUtil.readRows(selectSql, this.getConnection());
   }

}
