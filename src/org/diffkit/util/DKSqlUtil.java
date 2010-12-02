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
package org.diffkit.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;

/**
 * @author jpanico
 */
public class DKSqlUtil {
   public static final String DATABASE_MAJOR_VERSION_KEY = "DatabaseMajorVersion";
   public static final String DATABASE_MINOR_VERSION_KEY = "DatabaseMinorVersion";
   public static final String DATABASE_PRODUCT_NAME_KEY = "DatabaseProductName";
   public static final String DATABASE_PRODUCT_VERSION_KEY = "DatabaseProductVersion";

   public static enum ReadType {
      OBJECT, STRING, TEXT, TIMESTAMP;
   }

   public static enum WriteType {
      NUMBER, STRING, DATE, TIME, TIMESTAMP;
   }

   public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
   private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat(
      DEFAULT_DATE_PATTERN);
   public static final String DEFAULT_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";
   private static final SimpleDateFormat DEFAULT_TIME_FORMAT = new SimpleDateFormat(
      DEFAULT_TIMESTAMP_PATTERN);

   private static final Logger LOG = LoggerFactory.getLogger(DKSqlUtil.class);
   private static final boolean IS_DEBUG_ENABLED = LOG.isDebugEnabled();

   private DKSqlUtil() {
   }

   public static java.sql.Date createDate(int year_, int month_, int dayOfMonth_) {
      return new java.sql.Date(
         DKTimeUtil.createDate(year_, month_, dayOfMonth_).getTime());
   }

   public static java.sql.Date createDate(int year_, int month_, int dayOfMonth_,
                                          int hourOfDay_, int minute_, int second_,
                                          int millisecond_) {
      return new java.sql.Date(DKTimeUtil.createDate(year_, month_, dayOfMonth_,
         hourOfDay_, minute_, second_, millisecond_).getTime());
   }

   public static Map<String, ?> getDatabaseInfo(Connection connection_)
      throws SQLException {
      if (connection_ == null)
         return null;
      DatabaseMetaData dbMeta = connection_.getMetaData();
      if (dbMeta == null)
         return null;
      Map<String, Object> info = new HashMap<String, Object>();
      info.put(DATABASE_MAJOR_VERSION_KEY, new Integer(dbMeta.getDatabaseMajorVersion()));
      info.put(DATABASE_MINOR_VERSION_KEY, new Integer(dbMeta.getDatabaseMinorVersion()));
      info.put(DATABASE_PRODUCT_NAME_KEY, dbMeta.getDatabaseProductName());
      info.put(DATABASE_PRODUCT_VERSION_KEY, dbMeta.getDatabaseProductVersion());
      return info;
   }

   public static String formatForSql(Object value_, WriteType type_) {
      if (value_ == null)
         return "NULL";
      switch (type_) {
      case NUMBER:
         return value_.toString();
      case STRING:
         return DKStringUtil.quote(value_.toString(), DKStringUtil.Quote.SINGLE);
      case DATE:
         if (value_ instanceof Date)
            return DKStringUtil.quote(DEFAULT_DATE_FORMAT.format(value_),
               DKStringUtil.Quote.SINGLE);
         else
            return DKStringUtil.quote(value_.toString(), DKStringUtil.Quote.SINGLE);
      case TIME:
         if (value_ instanceof Time)
            return DKStringUtil.quote(DEFAULT_TIME_FORMAT.format(value_),
               DKStringUtil.Quote.SINGLE);
         else
            return DKStringUtil.quote(value_.toString(), DKStringUtil.Quote.SINGLE);
      case TIMESTAMP:
         if (value_ instanceof Timestamp)
            return "{ts "
               + DKStringUtil.quote(DEFAULT_TIME_FORMAT.format(value_),
                  DKStringUtil.Quote.SINGLE) + "}";
         else
            return DKStringUtil.quote(value_.toString(), DKStringUtil.Quote.SINGLE);

      default:
         throw new RuntimeException(String.format("unrecognized type_->%s", type_));
      }
   }

   /**
    * does not close connection_
    */
   public static List<Map<String, ?>> readRows(String selectSql_, Connection connection_)
      throws SQLException {
      LOG.debug("selectSql_", selectSql_);
      LOG.debug("connection_->{}", connection_);
      if ((selectSql_ == null) || (connection_ == null))
         return null;
      ResultSet resultSet = executeQuery(selectSql_, connection_);
      if (resultSet == null)
         return null;
      List<Map<String, ?>> rows = readRows(resultSet);
      close(resultSet);
      return rows;
   }

   /**
    * does not close resultSet_
    * 
    * @throws SQLException
    */
   public static Object[] readRow(ResultSet resultSet_, String[] columnNames_,
                                  ReadType[] readTypes_) throws SQLException {
      if (IS_DEBUG_ENABLED)
         LOG.debug("readTypes_->{}",
            readTypes_ == null ? null : Arrays.toString(readTypes_));
      if ((resultSet_ == null) || (columnNames_ == null) || (columnNames_.length == 0)
         || (readTypes_ == null))
         return null;
      Object[] row = new Object[columnNames_.length];
      for (int i = 0; i < columnNames_.length; i++) {
         switch (readTypes_[i]) {
         case TEXT:
            row[i] = resultSet_.getString(columnNames_[i]);
            break;
         case STRING:
            row[i] = resultSet_.getString(columnNames_[i]);
            break;
         case TIMESTAMP:
            row[i] = resultSet_.getTimestamp(columnNames_[i]);
            break;
         case OBJECT:
            row[i] = resultSet_.getObject(columnNames_[i]);
            break;
         default:
            throw new RuntimeException(String.format("unrecognized ReadType->%s",
               readTypes_[i]));
         }
      }
      return row;
   }

   public static List<Map<String, ?>> readRows(ResultSet resultSet_) throws SQLException {
      return readRows(resultSet_, false);
   }

   /**
    * does not close resultSet_
    */
   public static List<Map<String, ?>> readRows(ResultSet resultSet_, boolean keysUpper_)
      throws SQLException {
      if (resultSet_ == null)
         return null;
      SQLWarning warnings = resultSet_.getWarnings();
      if (warnings != null) {
         LOG.warn(null, warnings);
         return null;
      }
      String[] columnNames = getColumnNames(resultSet_);
      if (columnNames == null) {
         LOG.warn(String.format("no columnNames for resultSet_->%s", resultSet_));
         return null;
      }

      List<Map<String, ?>> maps = new ArrayList<Map<String, ?>>();
      while (resultSet_.next()) {
         Map<String, ?> map = getRowMap(columnNames, resultSet_, keysUpper_);
         LOG.debug("map->{}", map);
         maps.add(map);
      }
      if (maps.isEmpty())
         return null;
      return maps;
   }

   public static Map<String, ?> getRowMap(String[] columnNames_, ResultSet resultSet_,
                                          boolean keysUpper_) throws SQLException {
      if ((columnNames_ == null) || (resultSet_ == null))
         return null;

      Map<String, Object> rowMap = new HashMap<String, Object>();
      for (String columnName : columnNames_) {
         Object columnValue = getColumnValue(columnName, resultSet_);
         if (LOG.isDebugEnabled())
            LOG.debug("{}={}", columnName, columnValue);
         if (keysUpper_)
            columnName = columnName.toUpperCase();
         rowMap.put(columnName, columnValue);
      }

      return rowMap;
   }

   public static Object getColumnValue(String columnName_, ResultSet resultSet_) {
      if ((columnName_ == null) || (resultSet_ == null))
         return null;
      if (LOG.isDebugEnabled())
         LOG.debug("columnName_->{}", columnName_);
      try {
         return resultSet_.getObject(columnName_);
      }
      catch (Exception e_) {
         if (LOG.isDebugEnabled())
            LOG.debug(e_.getMessage());
         if (e_.getClass().getName().equalsIgnoreCase(
            "com.microsoft.sqlserver.jdbc.SQLServerException"))
            return null;
         throw new RuntimeException(e_);
      }
   }

   public static String[] getColumnNames(ResultSet resultSet_) throws SQLException {
      if (resultSet_ == null)
         return null;

      ResultSetMetaData metaData = resultSet_.getMetaData();
      int columnCount = metaData.getColumnCount();
      if (columnCount < 1)
         return null;
      String[] columnNames = new String[columnCount];
      for (int i = 1; i <= columnCount; i++) {
         columnNames[i - 1] = metaData.getColumnName(i);
      }
      return columnNames;
   }

   /**
    * null and Exception safe
    */
   public static void close(Statement statement_) {
      if (statement_ == null)
         return;

      try {
         statement_.close();
      }
      catch (Exception e_) {
         LOG.warn(null, e_);
      }
   }

   /**
    * null and Exception safe
    */
   public static void close(ResultSet resultSet_) {
      if (resultSet_ == null)
         return;

      try {
         resultSet_.close();
      }
      catch (Exception e_) {
         LOG.warn(null, e_);
      }
   }

   /**
    * null and Exception safe
    */
   public static void close(Connection connection_) {
      if (connection_ == null)
         return;

      try {
         connection_.close();
      }
      catch (Exception e_) {
         LOG.warn(null, e_);
      }
   }

   public static void rollback(Connection connection_) {
      if (connection_ == null)
         return;

      try {
         LOG.debug("rolling back");
         connection_.rollback();
      }
      catch (Exception e_) {
         LOG.warn(null, e_);
      }
   }

   /**
    * null and Exception safe
    */
   public static boolean executeUpdate(String sql_, Connection connection_) {
      LOG.debug("sql_->{}", sql_);
      if ((sql_ == null) || (connection_ == null))
         return false;
      Statement statement = createStatement(connection_);
      if (statement == null)
         return false;

      try {
         statement.execute(sql_);
         if (!connection_.getAutoCommit())
            connection_.commit();
         return true;
      }
      catch (Exception e_) {
         LOG.error(null, e_);
         return false;
      }
   }

   public static long executeBatchUpdate(List<String> sqlUpdateStrings_,
                                         Connection connection_) {
      if (LOG.isDebugEnabled()) {
         LOG.debug("sqlUpdateStrings_: " + sqlUpdateStrings_.size());
         LOG.debug("connection_: " + connection_);
      }
      DKValidate.notNull(connection_);
      if ((sqlUpdateStrings_ == null) || (sqlUpdateStrings_.size() == 0))
         return 0;

      Statement statement = null;
      int[] rowCounts = null;
      try {
         statement = connection_.createStatement();
         for (String sqlUpdate : sqlUpdateStrings_)
            statement.addBatch(sqlUpdate);

         rowCounts = statement.executeBatch();
         if (LOG.isDebugEnabled())
            LOG.debug("effectedRowCount: " + ArrayUtils.toString(rowCounts));

      }
      catch (Exception e_) {
         LOG.warn(null, e_);
         if (DKObjectUtil.respondsTo(e_, "getNextException", null)) {
            try {
               Exception nextException = (Exception) DKObjectUtil.invoke(e_,
                  "getNextException", null);
               LOG.warn(null, nextException);
            }
            catch (Exception f_) {
               LOG.error(null, f_);
            }
         }
         rollback(connection_);
      }
      finally {
         close(statement);
      }

      return DKArrayUtil.sum(rowCounts);
   }

   /**
    * calls executeQuery(sql_, connection_, 0)
    */
   public static ResultSet executeQuery(String sql_, Connection connection_)
      throws SQLException {
      return executeQuery(sql_, connection_, 0);
   }

   public static ResultSet executeQuery(String sql_, Connection connection_,
                                        int fetchSize_) throws SQLException {
      LOG.debug("sql_->{}", sql_);
      if ((sql_ == null) || (connection_ == null))
         return null;
      Statement statement = createStatement(connection_);
      if (statement == null)
         return null;

      statement.setFetchSize(fetchSize_);
      return statement.executeQuery(sql_);
   }

   /**
    * null and Exception safe
    */
   public static Statement createStatement(Connection connection_) {
      if (connection_ == null)
         return null;
      try {
         return connection_.createStatement();
      }
      catch (Exception e_) {
         LOG.error(null, e_);
         return null;
      }
   }
}
