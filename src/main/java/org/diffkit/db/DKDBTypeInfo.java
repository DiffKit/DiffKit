/**
 * Copyright 2010-2011 Joseph Panico
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
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import org.diffkit.common.DKValidate;
import org.diffkit.util.DKSqlUtil;

/**
 * @author jpanico
 */
public class DKDBTypeInfo implements Comparable<DKDBTypeInfo> {

   private final DKDBType _type;
   private final int _javaSqlType;
   private final int _maxPrecision;
   private final boolean _isCaseSensitive;

   public DKDBTypeInfo(DKDBType type_, int dataType_, int maxPrecision_,
                       boolean isCaseSensitive_) {
      _type = type_;
      _javaSqlType = dataType_;
      _maxPrecision = maxPrecision_;
      _isCaseSensitive = isCaseSensitive_;
      DKValidate.notNull(_type);
   }

   public static DKDBTypeInfo getDefaultTypeInfo(DKDBType dbType_) {
      if (dbType_ == null)
         return null;
      return new DKDBTypeInfo(dbType_, -9999999, 0, false);
   }

   public DKDBType getType() {
      return _type;
   }

   public String getName() {
      return _type.toString();
   }

   public int getJavaSqlType() {
      return _javaSqlType;
   }

   /**
    * convenience method that delegates to underlying .type
    */
   public DKSqlUtil.ReadType getReadType() {
      return _type.getReadType();
   }

   /**
    * convenience method that delegates to underlying .type
    */
   public DKSqlUtil.WriteType getWriteType() {
      return _type.getWriteType();
   }

   public String toString() {
      return String.format("%s[%s:%s]", ClassUtils.getShortClassName(this.getClass()),
         _type, _javaSqlType);
   }

   public boolean equals(Object target_) {
      if (target_ == null)
         return false;
      if (target_ == this)
         return true;
      if (target_.getClass() != getClass())
         return false;

      DKDBTypeInfo rhs = (DKDBTypeInfo) target_;

      EqualsBuilder builder = new EqualsBuilder();
      builder.append(_javaSqlType, rhs._javaSqlType);
      return builder.isEquals();
   }

   public int compareTo(DKDBTypeInfo target_) {
      CompareToBuilder builder = new CompareToBuilder();

      builder.append(_javaSqlType, target_._javaSqlType);

      return builder.toComparison();
   }

   public int hashCode() {
      return new HashCodeBuilder(17, 37).append(_javaSqlType).toHashCode();
   }

   public String getDescription() {
      return ReflectionToStringBuilder.toString(this);
   }
}
