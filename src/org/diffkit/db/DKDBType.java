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

import org.diffkit.util.DKSqlUtil;
import org.diffkit.util.DKSqlUtil.ReadType;
import org.diffkit.util.DKSqlUtil.WriteType;

/**
 * @author jpanico
 */
public enum DKDBType {
   ARRAY, BIGINT, BINARY, BIT, BLOB, BOOLEAN, CHAR, CLOB, DATALINK, DATE, DECIMAL, DISTINCT, DOUBLE, FLOAT, INTEGER, JAVA_OBJECT, LONGNVARCHAR, LONGVARBINARY, LONGVARCHAR, NCHAR, NCLOB, NULL, NUMERIC, NVARCHAR, OTHER, REAL, REF, ROWID, SMALLINT, SQLXML, STRUCT, TIME, TIMESTAMP, TINYINT, VARBINARY, VARCHAR, _H2_IDENTITY, _H2_UUID, _H2_VARCHAR_IGNORECASE;

   public DKSqlUtil.ReadType getReadType() {
      return getReadType(this);
   }

   public DKSqlUtil.WriteType getWriteType() {
      return getWriteType(this);
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
      case FLOAT:
         return WriteType.NUMBER;
      case DOUBLE:
         return WriteType.NUMBER;
      case CHAR:
         return WriteType.STRING;
      case VARCHAR:
         return WriteType.STRING;
      case LONGVARCHAR:
         return WriteType.STRING;
      case DATE:
         return WriteType.DATE;
      case TIME:
         return WriteType.TIME;
      case TIMESTAMP:
         return WriteType.TIME;

      default:
         throw new RuntimeException(String.format(
            "unrecognized java.sql.Types field->%s", dbType_));
      }
   }

   public static DKDBType getType(DKDBFlavor flavor_, String typeName_) {
      if (typeName_ == null)
         return null;
      DKDBType type = forName(typeName_);
      if (type != null)
         return type;
      if (flavor_ == null)
         throw new IllegalArgumentException(String.format("unrecognized typeName_->%s",
            typeName_));
      String qualifiedTypeName = "_" + flavor_.toString() + "_" + typeName_;
      type = forName(qualifiedTypeName);
      if (type != null)
         return type;
      throw new IllegalArgumentException(String.format(
         "unrecognized qualifiedTypeName->%s", qualifiedTypeName));
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
