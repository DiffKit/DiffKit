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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;
import org.diffkit.util.DKNumberUtil;
import org.diffkit.util.DKSqlUtil;

/**
 * @author jpanico
 */
public class DKDBTypeInfoDataAccess {

   private final DKDBDatabase _connectionSource;
   private Map<DKDBType, DKDBTypeInfo> _typeToTypeInfoMap;
   private Map<String, DKDBTypeInfo> _nameToTypeInfoMap;
   private Map<Integer, DKDBTypeInfo> _javaSqlTypeToTypeInfoMap;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   public DKDBTypeInfoDataAccess(DKDBDatabase connectionSource_) {
      _connectionSource = connectionSource_;
      DKValidate.notNull(_connectionSource);
   }

   public DKDBFlavor getFlavor() {
      return _connectionSource.getFlavor();
   }

   public String getNameForSqlType(Integer sqlType_) throws SQLException {
      if (sqlType_ == null)
         return null;
      Map<Integer, DKDBTypeInfo> typeInfoMap = this.getJavaSqlTypeToTypeInfoMap();
      DKDBTypeInfo typeInfo = typeInfoMap.get(sqlType_);
      if (typeInfo == null)
         return null;
      return typeInfo.getName();
   }

   public DKDBTypeInfo getTypeInfo(DKDBType type_) throws SQLException {
      if (type_ == null)
         return null;
      Map<DKDBType, DKDBTypeInfo> typeInfoMap = this.getTypeToTypeInfoMap();
      return typeInfoMap.get(type_);
   }

   /**
    * convenience method built on getTypeInfo(String)
    */
   public DKDBType getType(String dbTypeName_) throws SQLException {
      DKDBTypeInfo typeInfo = this.getTypeInfo(dbTypeName_);
      if (typeInfo == null)
         return null;
      return typeInfo.getType();
   }

   public DKDBTypeInfo getTypeInfo(String dbTypeName_) throws SQLException {
      if (dbTypeName_ == null)
         return null;
      Map<String, DKDBTypeInfo> typeInfoMap = this.getNameToTypeInfoMap();
      return typeInfoMap.get(dbTypeName_);
   }

   public Integer getJavaSqlTypeForName(String dbTypeName_) throws SQLException {
      DKDBTypeInfo typeInfo = this.getTypeInfo(dbTypeName_);
      if (typeInfo == null)
         return null;
      return typeInfo.getJavaSqlType();
   }

   /**
    * lazy
    * 
    * @return guaranteed to be non-null
    */
   private Map<DKDBType, DKDBTypeInfo> getTypeToTypeInfoMap() throws SQLException {
      if (_typeToTypeInfoMap != null)
         return _typeToTypeInfoMap;
      this.ensureMaps();
      if (_typeToTypeInfoMap == null)
         _typeToTypeInfoMap = new HashMap<DKDBType, DKDBTypeInfo>();
      return _typeToTypeInfoMap;
   }

   /**
    * lazy
    * 
    * @return guaranteed to be non-null
    */
   private Map<String, DKDBTypeInfo> getNameToTypeInfoMap() throws SQLException {
      if (_nameToTypeInfoMap != null)
         return _nameToTypeInfoMap;
      this.ensureMaps();
      if (_nameToTypeInfoMap == null)
         _nameToTypeInfoMap = new HashMap<String, DKDBTypeInfo>();
      return _nameToTypeInfoMap;
   }

   /**
    * lazy
    * 
    * @return guaranteed to be non-null
    */
   private Map<Integer, DKDBTypeInfo> getJavaSqlTypeToTypeInfoMap() throws SQLException {
      if (_javaSqlTypeToTypeInfoMap != null)
         return _javaSqlTypeToTypeInfoMap;
      this.ensureMaps();
      if (_javaSqlTypeToTypeInfoMap == null)
         _javaSqlTypeToTypeInfoMap = new HashMap<Integer, DKDBTypeInfo>();
      return _javaSqlTypeToTypeInfoMap;
   }

   private void ensureMaps() throws SQLException {
      Connection connection = this.getConnection();
      DatabaseMetaData dbMeta = connection.getMetaData();
      List<Map<String, ?>> entryMaps = this.getTypeInfoMaps(dbMeta);
      _typeToTypeInfoMap = new HashMap<DKDBType, DKDBTypeInfo>();
      _nameToTypeInfoMap = new HashMap<String, DKDBTypeInfo>();
      _javaSqlTypeToTypeInfoMap = new HashMap<Integer, DKDBTypeInfo>();
      if ((entryMaps == null) || (entryMaps.isEmpty()))
         return;
      for (Map<String, ?> entryMap : entryMaps) {
         DKDBTypeInfo typeInfo = this.constructTypeInfo(entryMap);
         if (typeInfo == null)
            continue;
         _typeToTypeInfoMap.put(typeInfo.getType(), typeInfo);
         _nameToTypeInfoMap.put(typeInfo.getName(), typeInfo);
         _javaSqlTypeToTypeInfoMap.put(new Integer(typeInfo.getJavaSqlType()), typeInfo);
      }
   }

   private DKDBTypeInfo constructTypeInfo(Map<String, ?> typeInfoMap_) {
      if (typeInfoMap_ == null)
         return null;
      String typeName = (String) typeInfoMap_.get("TYPE_NAME");
      Number dataType = (Number) typeInfoMap_.get("DATA_TYPE");
      Number maxPrecision = (Number) typeInfoMap_.get("PRECISION");
      Boolean isCaseSensitive = (Boolean) typeInfoMap_.get("CASE_SENSITIVE");
      DKDBType type = DKDBType.getType(_connectionSource.getFlavor(), typeName);
      if (type == null)
         throw new RuntimeException(String.format("couldn't find type for typeName->%s",
            typeName));
      return new DKDBTypeInfo(type, DKNumberUtil.getInt(dataType, -1),
         DKNumberUtil.getInt(maxPrecision, -1), BooleanUtils.toBoolean(isCaseSensitive));
   }

   private List<Map<String, ?>> getTypeInfoMaps(DatabaseMetaData dbMeta_)
      throws SQLException {
      ResultSet typeInfoRS = dbMeta_.getTypeInfo();
      _log.debug("typeInfoRS->{}", typeInfoRS);
      if (typeInfoRS == null)
         throw new RuntimeException(
            String.format("No typeInfo from dbMeta_->%s", dbMeta_));
      List<Map<String, ?>> typeInfoMaps = DKSqlUtil.readRows(typeInfoRS);
      _log.debug("typeInfoMaps->{}", typeInfoMaps);
      return typeInfoMaps;
   }

   private Connection getConnection() throws SQLException {
      return _connectionSource.getConnection();
   }
}
