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

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jpanico
 */
public class DKSqlUtil {
   public static enum ReadType {
      OBJECT, STRING, TEXT;
   }

   private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
   private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat(
      DEFAULT_DATE_PATTERN);
   private static final String DEFAULT_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
   private static final SimpleDateFormat DEFAULT_TIME_FORMAT = new SimpleDateFormat(
      DEFAULT_TIME_PATTERN);
   private static final Map<Integer, String> _sqlTypeToNameMap;
   private static final Logger LOG = LoggerFactory.getLogger(DKSqlUtil.class);

   static {
      _sqlTypeToNameMap = constructSqlTypeToNameMap();
   }

   private DKSqlUtil() {
   }

   public static String formatForSql(Object value_, int dataType_) {
      if (value_ == null)
         return "NULL";
      switch (dataType_) {
      case Types.BIGINT:
         return value_.toString();
      case Types.INTEGER:
         return value_.toString();
      case Types.TINYINT:
         return value_.toString();
      case Types.SMALLINT:
         return value_.toString();
      case Types.DECIMAL:
         return value_.toString();
      case Types.NUMERIC:
         return value_.toString();
      case Types.FLOAT:
         return value_.toString();
      case Types.DOUBLE:
         return value_.toString();

      case Types.CHAR:
         return DKStringUtil.quote(value_.toString(), DKStringUtil.Quote.SINGLE);
      case Types.VARCHAR:
         return DKStringUtil.quote(value_.toString(), DKStringUtil.Quote.SINGLE);
      case Types.LONGVARCHAR:
         return DKStringUtil.quote(value_.toString(), DKStringUtil.Quote.SINGLE);

      case Types.DATE:
         return DKStringUtil.quote(DEFAULT_DATE_FORMAT.format(value_),
            DKStringUtil.Quote.SINGLE);
      case Types.TIME:
         return DKStringUtil.quote(DEFAULT_TIME_FORMAT.format(value_),
            DKStringUtil.Quote.SINGLE);
      case Types.TIMESTAMP:
         return DKStringUtil.quote(DEFAULT_TIME_FORMAT.format(value_),
            DKStringUtil.Quote.SINGLE);

      default:
         throw new RuntimeException(String.format(
            "unrecognized java.sql.Types field->%s", dataType_));
      }
   }

   public static ReadType getReadTypeForSqlType(int dataType_) {
      switch (dataType_) {
      case Types.CLOB:
         return ReadType.TEXT;
      case Types.CHAR:
         return ReadType.STRING;
      case Types.VARCHAR:
         return ReadType.STRING;
      case Types.LONGVARCHAR:
         return ReadType.STRING;
      default:
         return ReadType.OBJECT;
      }
   }

   public static ReadType getReadTypeForSqlType(String sqlTypeName_) {
      if (sqlTypeName_ == null)
         return null;
      Integer sqlType = getSqlTypeForName(sqlTypeName_);
      if (sqlType == null)
         throw new RuntimeException(String.format(
            "couldn't find sqlType for sqlTypeName_ [%s]", sqlTypeName_));
      return getReadTypeForSqlType(sqlType);
   }

   public static String getNameForSqlType(Integer sqlType_) {
      if (sqlType_ == null)
         return null;
      return _sqlTypeToNameMap.get(sqlType_);
   }

   public static Integer getSqlTypeForName(String sqlTypeName_) {
      if (sqlTypeName_ == null)
         return null;
      Set<Map.Entry<Integer, String>> entries = _sqlTypeToNameMap.entrySet();
      for (Map.Entry<Integer, String> entry : entries) {
         if (entry.getValue().equals(sqlTypeName_))
            return entry.getKey();
      }
      return null;
   }

   private static Map<Integer, String> constructSqlTypeToNameMap() {
      // Get all field in java.sql.Types
      Field[] fields = java.sql.Types.class.getFields();
      Map<Integer, String> map = new HashMap<Integer, String>(fields.length);
      for (int i = 0; i < fields.length; i++) {
         try {
            String name = fields[i].getName();
            Integer value = (Integer) fields[i].get(null);
            map.put(value, name);
         }
         catch (IllegalAccessException e_) {
            LOG.error(null, e_);
         }
      }
      // for Oracle
      map.put(new Integer(Types.VARCHAR), "VARCHAR2");
      map.put(new Integer(Types.NUMERIC), "NUMBER");
      return map;
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

   /**
    * does not close resultSet_
    */
   public static List<Map<String, ?>> readRows(ResultSet resultSet_) throws SQLException {
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
         Map<String, ?> map = getRowMap(columnNames, resultSet_);
         LOG.debug("map->{}", map);
         maps.add(map);
      }
      if (maps.isEmpty())
         return null;
      return maps;
   }

   public static Map<String, ?> getRowMap(String[] columnNames_, ResultSet resultSet_)
      throws SQLException {
      if ((columnNames_ == null) || (resultSet_ == null))
         return null;

      Map<String, Object> rowMap = new HashMap<String, Object>();
      for (String columnName : columnNames_)
         rowMap.put(columnName, resultSet_.getObject(columnName));

      return rowMap;
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
         connection_.commit();
         return true;
      }
      catch (Exception e_) {
         LOG.error(null, e_);
         return false;
      }
   }

   /**
    * null safe
    */
   public static ResultSet executeQuery(String sql_, Connection connection_)
      throws SQLException {
      LOG.debug("sql_->{}", sql_);
      if ((sql_ == null) || (connection_ == null))
         return null;
      Statement statement = createStatement(connection_);
      if (statement == null)
         return null;

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
