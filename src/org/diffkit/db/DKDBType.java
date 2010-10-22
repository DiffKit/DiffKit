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

import org.diffkit.common.DKValidate;
import org.diffkit.util.DKSqlUtil;
import org.diffkit.util.DKSqlUtil.ReadType;
import org.diffkit.util.DKSqlUtil.WriteType;

/**
 * @author jpanico
 */
public enum DKDBType {

   ARRAY, BIGINT, BINARY, BIT, BLOB, BOOLEAN, CHAR(false), CLOB(false), DATALINK(true), DATE, DECIMAL(
      false), DISTINCT, DOUBLE, FLOAT(false), INTEGER, JAVA_OBJECT, LONGNVARCHAR(true), LONGVARBINARY(
      true), LONGVARCHAR, NCHAR, NCLOB, NULL, NUMERIC(false), NVARCHAR, OTHER, REAL, REF(
      true), ROWID, SMALLINT, SQLXML, STRUCT, TIME, TIMESTAMP(true), TINYINT, VARBINARY(
      true), VARCHAR(false), _H2_IDENTITY, _H2_UUID, _H2_VARCHAR_IGNORECASE(false), _DB2_LONG_VARCHAR_FOR_BIT_DATA(
      true), _DB2_VARCHAR_00_FOR_BIT_DATA(true), _DB2_CHAR_00_FOR_BIT_DATA, _DB2_LONG_VARCHAR(
      true), _DB2_LONG_VARGRAPHIC(true), _DB2_GRAPHIC, _DB2_VARGRAPHIC, _DB2_DECFLOAT(
      true), _DB2_XML(true), _DB2_DBCLOB, _ORACLE_INTERVALDS(true), _ORACLE_INTERVALYM(
      true), _ORACLE_TIMESTAMP_WITH_LOCAL_TIME_ZONE, _ORACLE_TIMESTAMP_WITH_TIME_ZONE(
      true), _ORACLE_NUMBER, _ORACLE_LONG_RAW, _ORACLE_RAW, _ORACLE_LONG, _ORACLE_VARCHAR2(
      false);

   private static final Map<DKDBFlavor, Map<DKDBType, DKDBType>> _typeRemappings;
   private static Pattern _flavorManglePattern;

   private final boolean _ignoresLengthSpecifier;
   private final String _sqlTypeName;

   static {
      _typeRemappings = new HashMap<DKDBFlavor, Map<DKDBType, DKDBType>>();
      // Oracle
      Map<DKDBType, DKDBType> oracleMap = new HashMap<DKDBType, DKDBType>();
      oracleMap.put(VARCHAR, _ORACLE_VARCHAR2);
      oracleMap.put(BIGINT, _ORACLE_NUMBER);
      _typeRemappings.put(DKDBFlavor.ORACLE, oracleMap);
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
      case DATE:
         return WriteType.DATE;
      case TIME:
         return WriteType.TIME;
      case TIMESTAMP:
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
      if (name_ == null)
         return null;

      try {
         return Enum.valueOf(DKDBType.class, name_);
      }
      catch (Exception e_) {
         return null;
      }
   }
}
