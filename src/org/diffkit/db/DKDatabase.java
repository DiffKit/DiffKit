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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;
import org.diffkit.util.DKSqlUtil;

/**
 * @author jpanico
 */
public class DKDatabase {

   private static final String USERNAME_KEY = "user";
   private static final String PASSWORD_KEY = "password";
   private final DKDBConnectionInfo _connectionInfo;
   private final boolean _caseSensitive;
   private final DKDBTypeInfoDataAccess _typeInfoDataAccess;
   private final DKSqlGenerator _sqlGenerator;
   private final DKDBTableDataAccess _tableDataAccess;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   public DKDatabase(DKDBConnectionInfo connectionInfo_) {
      _connectionInfo = connectionInfo_;
      _typeInfoDataAccess = new DKDBTypeInfoDataAccess(this);
      _sqlGenerator = new DKSqlGenerator(this);
      _tableDataAccess = new DKDBTableDataAccess(this);
      DKValidate.notNull(_connectionInfo, _typeInfoDataAccess, _sqlGenerator,
         _tableDataAccess);
      _caseSensitive = _connectionInfo.getFlavor()._caseSensitive;
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

   public boolean getCaseSensitive() {
      return _caseSensitive;
   }

   /**
    * convenience that passes through to ConnectionInfo
    */
   public DKDBFlavor getFlavor() {
      return _connectionInfo.getFlavor();
   }

   /**
    * convenience that passes through to TypeInfoDataAccess
    */
   public DKDBType getType(String dbTypeName_) throws SQLException {
      return _typeInfoDataAccess.getType(dbTypeName_);
   }

   /**
    * convenience that passes through to TypeInfoDataAccess
    */
   public DKDBTypeInfo getTypeInfo(String dbTypeName_) throws SQLException {
      return _typeInfoDataAccess.getTypeInfo(dbTypeName_);
   }

   public DKDBTypeInfo getConcreteTypeInfo(String dbTypeName_) throws SQLException {
      DKDBType concreteType = DKDBType.getConcreteType(this.getFlavor(), dbTypeName_);
      return _typeInfoDataAccess.getTypeInfo(concreteType);
   }

   /**
    * convenience that passes through to TypeInfoDataAccess
    */
   public DKDBTypeInfo getTypeInfo(DKDBType dbType_) throws SQLException {
      return _typeInfoDataAccess.getTypeInfo(dbType_);
   }

   /**
    * convenience that calls getTypeInfo(String)
    */
   public boolean supportsType(String dbTypeName_) throws SQLException {
      return (this.getTypeInfo(dbTypeName_) != null);
   }

   /**
    * convenience that calls getTypeInfo(String)
    */
   public boolean supportsType(DKDBType dbType_) throws SQLException {
      return (this.getTypeInfo(dbType_) != null);
   }

   public DKDBTypeInfo[] getColumnTypeInfos(DKDBTable table_) throws SQLException {
      DKDBColumn[] columns = table_.getColumns();
      if (ArrayUtils.isEmpty(columns))
         return null;
      DKDBTypeInfo[] typeInfos = new DKDBTypeInfo[columns.length];
      for (int i = 0; i < columns.length; i++)
         typeInfos[i] = this.getTypeInfo(columns[i].getDBTypeName());
      return typeInfos;
   }

   public DKDBTypeInfo[] getColumnConcreteTypeInfos(DKDBTable table_) throws SQLException {
      DKDBColumn[] columns = table_.getColumns();
      if (ArrayUtils.isEmpty(columns))
         return null;
      DKDBTypeInfo[] typeInfos = new DKDBTypeInfo[columns.length];
      for (int i = 0; i < columns.length; i++)
         typeInfos[i] = this.getConcreteTypeInfo(columns[i].getDBTypeName());
      return typeInfos;
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

   public DKSqlGenerator getSqlGenerator() {
      return _sqlGenerator;
   }

   public DKDBTableDataAccess getTableDataAccess() {
      return _tableDataAccess;
   }

   /**
    * convenience method
    * 
    * @return the newly created table, which might be different from the
    *         requested table, depending on the DB support. For examples,
    *         columns of Type not supported by particular flavor will be quietly
    *         filterd out
    * @throws SQLException
    */
   public DKDBTable createTable(DKDBTable table_) throws SQLException {
      _log.debug("table_->{}", table_);
      if (table_ == null)
         return null;
      String createSql = _sqlGenerator.generateCreateDDL(table_);
      _log.debug("createSql->{}", createSql);
      if (!DKSqlUtil.executeUpdate(createSql, this.getConnection()))
         throw new SQLException("execute was not successful");
      return this.getTable(table_.getCatalog(), table_.getSchema(), table_.getTableName());
   }

   /**
    * convenience method that delegates to underlying TableDataAccess
    * 
    * @throws SQLException
    */
   public DKDBTable getTable(String catalog_, String schema_, String tableName_)
      throws SQLException {
      return _tableDataAccess.getTable(catalog_, schema_, tableName_);
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
      if (_log.isDebugEnabled())
         _log.debug("fetchedTable->{}", fetchedTable);
      if (fetchedTable == null)
         return false;
      return true;
   }

   /**
    * convenience that delegates to underlying SqlGenerator
    */
   public String generateInsertDML(Map<String, ?> row_, DKDBTable table_)
      throws SQLException {
      if ((table_ == null) || (row_ == null))
         return null;
      return _sqlGenerator.generateInsertDML(row_, table_);
   }

   /**
    * convenience that delegates to underlying SqlGenerator
    */
   public String generateInsertDML(Object[] values_, DKDBTypeInfo[] typeInfos_,
                                   String[] columnNames_, String schemaName_,
                                   String tableName_) {
      return _sqlGenerator.generateInsertDML(values_, typeInfos_, columnNames_,
         schemaName_, tableName_);
   }

   /**
    * convenience that delegates to underlying SqlGenerator
    */
   public List<String> generateInsertDML(List<Map<String, ?>> rows_, DKDBTable table_)
      throws SQLException {
      if ((table_ == null) || (CollectionUtils.isEmpty(rows_)))
         return null;
      List<String> dmls = new ArrayList<String>(rows_.size());
      for (Map<String, ?> row : rows_) {
         String dml = this.generateInsertDML(row, table_);
         if (dml == null)
            continue;
         dmls.add(dml);
      }
      return dmls;
   }

   public boolean insertRow(Map<String, ?> row_, DKDBTable table_) throws SQLException {
      if ((table_ == null) || (row_ == null))
         return false;
      String insertSql = _sqlGenerator.generateInsertDML(row_, table_);
      _log.debug("insertSql->{}", insertSql);
      Connection connection = this.getConnection();
      boolean insert = DKSqlUtil.executeUpdate(insertSql, connection);
      // DKSqlUtil.close(connection);
      return insert;
   }

   public List<Map<String, ?>> readAllRows(DKDBTable table_) throws SQLException {
      if (table_ == null)
         return null;
      String selectSql = _sqlGenerator.generateSelectDML(table_);
      _log.debug("selectSql->{}", selectSql);
      Connection connection = this.getConnection();
      List<Map<String, ?>> rows = DKSqlUtil.readRows(selectSql, connection);
      // DKSqlUtil.close(connection);
      return rows;
   }

   public boolean executeUpdate(String sql_) throws SQLException {
      Connection connection = this.getConnection();
      boolean update = DKSqlUtil.executeUpdate(sql_, connection);
      DKSqlUtil.close(connection);
      return update;
   }

   public String toString() {
      return _connectionInfo.toString();
   }
}
