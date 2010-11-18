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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;
import org.diffkit.util.DKSqlUtil;
import org.diffkit.util.DKSqlUtil.ReadType;
import org.diffkit.util.DKSqlUtil.WriteType;

/**
 * @author jpanico
 */
public enum DKDBType {

   ARRAY, BIGINT, BINARY, BIT, BLOB, BOOLEAN, CHAR(false), CLOB(true), DATALINK(true), DATE, DECIMAL(
      false), DISTINCT, DOUBLE, FLOAT(false), INTEGER, JAVA_OBJECT, LONGNVARCHAR(true), LONGVARBINARY(
      true), LONGVARCHAR, NCHAR, NCLOB, NULL, NUMERIC(false), NVARCHAR, OTHER, REAL, REF(
      true), ROWID, SMALLINT, SQLXML, STRUCT, TIME, TIMESTAMP(true), TINYINT, VARBINARY(
      true), VARCHAR(false), _H2_IDENTITY, _H2_UUID, _H2_VARCHAR_IGNORECASE(false), _DB2_LONG_VARCHAR_FOR_BIT_DATA(
      true), _DB2_VARCHAR_00_FOR_BIT_DATA(true), _DB2_CHAR_00_FOR_BIT_DATA, _DB2_LONG_VARCHAR(
      true), _DB2_LONG_VARGRAPHIC(true), _DB2_GRAPHIC, _DB2_VARGRAPHIC, _DB2_DECFLOAT(
      true), _DB2_XML(true), _DB2_DBCLOB, _ORACLE_INTERVALDS(true), _ORACLE_INTERVALYM(
      true), _ORACLE_TIMESTAMP_WITH_LOCAL_TIME_ZONE, _ORACLE_TIMESTAMP_WITH_TIME_ZONE(
      true), _ORACLE_NUMBER, _ORACLE_LONG_RAW, _ORACLE_RAW, _ORACLE_LONG, _ORACLE_VARCHAR2(
      false), _MYSQL_BOOL, _MYSQL_TINYINT_UNSIGNED, _MYSQL_BIGINT_UNSIGNED, _MYSQL_LONG_VARBINARY(
      true), _MYSQL_MEDIUMBLOB, _MYSQL_LONGBLOB, _MYSQL_TINYBLOB, _MYSQL_LONG_VARCHAR(
      true), _MYSQL_MEDIUMTEXT, _MYSQL_LONGTEXT, _MYSQL_TEXT, _MYSQL_TINYTEXT(true), _MYSQL_INTEGER_UNSIGNED(
      true), _MYSQL_INT, _MYSQL_INT_UNSIGNED, _MYSQL_MEDIUMINT, _MYSQL_MEDIUMINT_UNSIGNED(
      true), _MYSQL_SMALLINT_UNSIGNED, _MYSQL_DOUBLE_PRECISION, _MYSQL_ENUM, _MYSQL_SET(
      true), _MYSQL_DATETIME, _SQLSERVER_SQL_VARIANT, _SQLSERVER_UNIQUEIDENTIFIER, _SQLSERVER_NTEXT(
      true), _SQLSERVER_XML, _SQLSERVER_SYSNAME, _SQLSERVER_DATETIME2, _SQLSERVER_DATETIMEOFFSET(
      true), _SQLSERVER_TINYINT_IDENTITY(true), _SQLSERVER_BIGINT_IDENTITY, _SQLSERVER_IMAGE(
      true), _SQLSERVER_TEXT, _SQLSERVER_NUMERIC00_IDENTITY, _SQLSERVER_MONEY, _SQLSERVER_SMALLMONEY(
      true), _SQLSERVER_DECIMAL00_IDENTITY, _SQLSERVER_INT, _SQLSERVER_INT_IDENTITY(true), _SQLSERVER_SMALLINT_IDENTITY(
      true), _SQLSERVER_DATETIME, _SQLSERVER_SMALLDATETIME, _POSTGRES_BOOL, _POSTGRES_BYTEA(
      true), _POSTGRES_NAME, _POSTGRES_INT8, _POSTGRES_BIGSERIAL, _POSTGRES_INT2(true), _POSTGRES_INT2VECTOR(
      true), _POSTGRES_INT4, _POSTGRES_SERIAL, _POSTGRES_REGPROC, _POSTGRES_TEXT(true), _POSTGRES_OID(
      true), _POSTGRES_TID, _POSTGRES_XID, _POSTGRES_CID, _POSTGRES_OIDVECTOR, _POSTGRES_XML(
      true), _POSTGRES_SMGR, _POSTGRES_POINT, _POSTGRES_LSEG, _POSTGRES_PATH(true), _POSTGRES_BOX(
      true), _POSTGRES_POLYGON, _POSTGRES_LINE, _POSTGRES_FLOAT4(true), _POSTGRES_FLOAT8, _POSTGRES_ABSTIME(
      true), _POSTGRES_RELTIME, _POSTGRES_TINTERVAL(true), _POSTGRES_UNKNOWN, _POSTGRES_CIRCLE(
      true), _POSTGRES_MONEY, _POSTGRES_MACADDR, _POSTGRES_INET(true), _POSTGRES_CIDR, _POSTGRES_ACLITEM(
      true), _POSTGRES_BPCHAR, _POSTGRES_TIMESTAMPTZ(true), _POSTGRES_TIMETZ, _POSTGRES_VARBIT(
      true), _POSTGRES_UUID, _POSTGRES_TSVECTOR(true), _POSTGRES_GTSVECTOR(true), _POSTGRES_TSQUERY(
      true), _POSTGRES_TXID_SNAPSHOT, _POSTGRES_CSTRING(true), _POSTGRES_ANY, _POSTGRES_ANYARRAY(
      true), _POSTGRES_VOID, _POSTGRES_INTERNAL(true), _POSTGRES_ANYELEMENT(true), _POSTGRES_ANYNONARRAY(
      true), _POSTGRES_ANYENUM(true), _POSTGRES_INTERVAL, _POSTGRES_RECORD(true), _POSTGRES_CARDINAL_NUMBER(
      true), _POSTGRES_CHARACTER_DATA(true), _POSTGRES_SQL_IDENTIFIER(true);

   private static final String LENGTH_SPECIFIER_PATTERN = "\\(\\d*\\)";
   private static final Map<DKDBFlavor, Map<DKDBType, DKDBType>> _typeRemappings;
   private static final Logger LOG = LoggerFactory.getLogger(DKDBType.class);
   private static final boolean IS_DEBUG_ENABLED = LOG.isDebugEnabled();
   private static Pattern _flavorManglePattern;

   private final boolean _ignoresLengthSpecifier;
   private final String _sqlTypeName;

   static {
      _typeRemappings = new HashMap<DKDBFlavor, Map<DKDBType, DKDBType>>();
      // Oracle
      Map<DKDBType, DKDBType> oracleMap = new HashMap<DKDBType, DKDBType>();
      oracleMap.put(VARCHAR, _ORACLE_VARCHAR2);
      oracleMap.put(BIGINT, _ORACLE_NUMBER);
      oracleMap.put(INTEGER, _ORACLE_NUMBER);
      oracleMap.put(REAL, _ORACLE_NUMBER);
      oracleMap.put(DECIMAL, _ORACLE_NUMBER);
      oracleMap.put(TINYINT, _ORACLE_NUMBER);
      oracleMap.put(SMALLINT, _ORACLE_NUMBER);
      _typeRemappings.put(DKDBFlavor.ORACLE, oracleMap);
      // MySQL
      Map<DKDBType, DKDBType> mySQLMap = new HashMap<DKDBType, DKDBType>();
      mySQLMap.put(CLOB, _MYSQL_TEXT);
      mySQLMap.put(BOOLEAN, _MYSQL_BOOL);
      _typeRemappings.put(DKDBFlavor.MYSQL, mySQLMap);
      // SQLServer
      Map<DKDBType, DKDBType> sqlServerMap = new HashMap<DKDBType, DKDBType>();
      sqlServerMap.put(TIMESTAMP, _SQLSERVER_DATETIME);
      sqlServerMap.put(INTEGER, _SQLSERVER_INT);
      sqlServerMap.put(DOUBLE, FLOAT);
      sqlServerMap.put(CLOB, _SQLSERVER_TEXT);
      _typeRemappings.put(DKDBFlavor.SQLSERVER, sqlServerMap);
      // Postgres
      Map<DKDBType, DKDBType> postgresMap = new HashMap<DKDBType, DKDBType>();
      postgresMap.put(BIGINT, _POSTGRES_INT8);
      postgresMap.put(INTEGER, _POSTGRES_INT4);
      postgresMap.put(TINYINT, _POSTGRES_INT2);
      postgresMap.put(SMALLINT, _POSTGRES_INT2);
      postgresMap.put(DECIMAL, NUMERIC);
      postgresMap.put(DOUBLE, _POSTGRES_FLOAT8);
      postgresMap.put(REAL, _POSTGRES_FLOAT4);
      postgresMap.put(BOOLEAN, _POSTGRES_BOOL);
      postgresMap.put(CLOB, _POSTGRES_TEXT);
      _typeRemappings.put(DKDBFlavor.POSTGRES, postgresMap);
   }

   /**
    * if fullTypeName_ has a length specifier, this will strip it off<br/>
    * e.g. VARCHAR(128) -> VARCHAR
    */
   public static String getBaseTypeName(String fullTypeName_) {
      if (fullTypeName_ == null)
         return null;
      return fullTypeName_.replaceAll(LENGTH_SPECIFIER_PATTERN, "");
   }

   private static Pattern getFlavorManglePattern() {
      if (_flavorManglePattern != null)
         return _flavorManglePattern;
      _flavorManglePattern = Pattern.compile("^(_.*_)");
      return _flavorManglePattern;
   }

   private DKDBType() {
      this(true);
   }

   private DKDBType(boolean ignoresLengthSpecifier_) {
      _ignoresLengthSpecifier = ignoresLengthSpecifier_;
      _sqlTypeName = this.decodeSqlTypeName();
      DKValidate.notNull(_sqlTypeName);
   }

   public boolean ignoresLengthSpecifier() {
      return _ignoresLengthSpecifier;
   }

   public DKSqlUtil.ReadType getReadType() {
      return getReadType(this);
   }

   public DKSqlUtil.WriteType getWriteType() {
      return getWriteType(this);
   }

   public String getSqlTypeName() {
      return _sqlTypeName;
   }

   private String decodeSqlTypeName() {
      String enumName = this.toString();
      Matcher matcher = getFlavorManglePattern().matcher(enumName);
      if (!matcher.find())
         return enumName;
      return matcher.replaceFirst("");
   }

   /**
    * convenience method that uses getType(DKDBFlavor,String) &
    * getConcreteTypeForAbstractType(DKDBFlavor,DKDBType)
    */
   public static DKDBType getConcreteType(DKDBFlavor flavor_, String abstractSqlTypeName_) {
      DKDBType abstractType = getType(flavor_, abstractSqlTypeName_);
      if (abstractType == null)
         return null;
      return getConcreteTypeForAbstractType(flavor_, abstractType);
   }

   public static DKDBType getConcreteTypeForAbstractType(DKDBFlavor flavor_,
                                                         DKDBType abstractType_) {
      if (abstractType_ == null)
         return null;
      if (flavor_ == null)
         return abstractType_;
      Map<DKDBType, DKDBType> flavorMap = _typeRemappings.get(flavor_);
      if (flavorMap == null)
         return abstractType_;
      DKDBType remappedType = flavorMap.get(abstractType_);
      if (remappedType == null)
         return abstractType_;
      return remappedType;
   }

   public static DKSqlUtil.ReadType getReadType(DKDBType dbType_) {
      if (dbType_ == null)
         return null;

      switch (dbType_) {
      case CLOB:
         return ReadType.TEXT;
      case CHAR:
         return ReadType.STRING;
      case VARCHAR:
         return ReadType.STRING;
      case LONGVARCHAR:
         return ReadType.STRING;
      case TIMESTAMP:
         return ReadType.TIMESTAMP;
      default:
         return ReadType.OBJECT;
      }

   }

   public static DKSqlUtil.WriteType getWriteType(DKDBType dbType_) {
      switch (dbType_) {
      case BIGINT:
         return WriteType.NUMBER;
      case INTEGER:
         return WriteType.NUMBER;
      case TINYINT:
         return WriteType.NUMBER;
      case SMALLINT:
         return WriteType.NUMBER;
      case _MYSQL_INT:
         return WriteType.NUMBER;
      case DECIMAL:
         return WriteType.NUMBER;
      case NUMERIC:
         return WriteType.NUMBER;
      case _ORACLE_NUMBER:
         return WriteType.NUMBER;
      case FLOAT:
         return WriteType.NUMBER;
      case DOUBLE:
         return WriteType.NUMBER;
      case REAL:
         return WriteType.NUMBER;
      case BIT:
         return WriteType.NUMBER;
      case _SQLSERVER_INT:
         return WriteType.NUMBER;
      case _POSTGRES_INT2:
         return WriteType.NUMBER;
      case _POSTGRES_INT4:
         return WriteType.NUMBER;
      case _POSTGRES_INT8:
         return WriteType.NUMBER;
      case _POSTGRES_FLOAT4:
         return WriteType.NUMBER;
      case _POSTGRES_FLOAT8:
         return WriteType.NUMBER;
      case _POSTGRES_BOOL:
         return WriteType.NUMBER;
      case CHAR:
         return WriteType.STRING;
      case VARCHAR:
         return WriteType.STRING;
      case _ORACLE_VARCHAR2:
         return WriteType.STRING;
      case LONGVARCHAR:
         return WriteType.STRING;
      case CLOB:
         return WriteType.STRING;
      case _MYSQL_TEXT:
         return WriteType.STRING;
      case _SQLSERVER_TEXT:
         return WriteType.STRING;
      case _POSTGRES_TEXT:
         return WriteType.STRING;
      case _POSTGRES_BPCHAR:
         return WriteType.STRING;
      case DATE:
         return WriteType.DATE;
      case TIME:
         return WriteType.TIME;
      case TIMESTAMP:
         return WriteType.TIMESTAMP;
      case _SQLSERVER_DATETIME:
         return WriteType.TIMESTAMP;

      default:
         throw new RuntimeException(String.format(
            "unrecognized java.sql.Types field->%s", dbType_));
      }
   }

   /**
    * @param sqlTypeName_
    *           the JDBC SQL type name
    */
   public static DKDBType getType(DKDBFlavor flavor_, String sqlTypeName_) {
      if (sqlTypeName_ == null)
         return null;
      sqlTypeName_ = sqlTypeName_.toUpperCase();
      DKDBType type = forName(sqlTypeName_);
      if (type != null)
         return type;
      if (flavor_ == null)
         throw new IllegalArgumentException(String.format("unrecognized typeName_->%s",
            sqlTypeName_));
      String dbTypeName = convertSqlTypeNameToDBTypeName(flavor_, sqlTypeName_);
      type = forName(dbTypeName);
      if (type != null)
         return type;
      if (flavor_._ignoreUnrecognizedTypes)
         return null;
      throw new IllegalArgumentException(String.format(
         "unrecognized qualifiedTypeName->%s", dbTypeName));
   }

   private static String convertSqlTypeNameToDBTypeName(DKDBFlavor flavor_,
                                                        String sqlTypeName_) {
      if (sqlTypeName_ == null)
         return null;
      String mangledName = sqlTypeName_;
      mangledName = StringUtils.replace(mangledName, " ", "_");
      mangledName = StringUtils.replace(mangledName, "(", "0");
      mangledName = StringUtils.replace(mangledName, ")", "0");
      mangledName = "_" + flavor_.toString() + "_" + mangledName;
      return mangledName;
   }

   /**
    * will simply return null if argument is not recognized, instead of throwing
    */
   public static DKDBType forName(String name_) {
      if (IS_DEBUG_ENABLED)
         LOG.debug("name_->{}", name_);
      if (name_ == null)
         return null;

      try {
         DKDBType forName = Enum.valueOf(DKDBType.class, name_);
         if (IS_DEBUG_ENABLED)
            LOG.debug("forName->{}", forName);
         return forName;
      }
      catch (Exception e_) {
         return null;
      }
   }
}
