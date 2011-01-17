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
package org.diffkit.diff.engine;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.ParseException;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import org.diffkit.common.DKValidate;
import org.diffkit.common.annot.Immutable;

/**
 * @author jpanico
 */
@Immutable
public class DKColumnModel {

   public static final String ROW_NUM_COLUMN_NAME = "<ROW_NUM>";

   public enum Type {
      STRING, INTEGER(true), REAL(true), DECIMAL(true), DATE, TIME, TIMESTAMP, BOOLEAN, TEXT, MIXED;

      public final boolean _isNumber;

      private Type() {
         this(false);
      }

      private Type(boolean isNumber_) {
         _isNumber = isNumber_;
      }
   }

   /**
    * should be ISO 8601 compliant
    */
   private static final String DEFAULT_DATE_FORMAT_STRING = "yyyy-MM-dd";
   /**
    * should be ISO 8601 compliant
    */
   private static final String DEFAULT_TIME_FORMAT_STRING = "hh:mm:ss";
   /**
    * should be ISO 8601 compliant
    */
   private static final String DEFAULT_TIMESTAMP_FORMAT_STRING = DEFAULT_DATE_FORMAT_STRING
      + "T" + DEFAULT_TIME_FORMAT_STRING;

   /**
    * 0's based
    */
   public final int _index;
   public final String _name;
   public final Type _type;
   public final Format _format;
   public final String _formatString;
   private final boolean _isRowNum;
   private DKTableModel _table;

   public DKColumnModel(int index_, String name_, Type type_) {
      this(index_, name_, type_, null);
   }

   public DKColumnModel(int index_, String name_, Type type_, String formatString_) {
      _index = index_;
      _name = name_;
      _type = type_;
      _formatString = formatString_;
      _format = this.createFormat(_type, _formatString);
      DKValidate.notNull(_name, _type);
      _isRowNum = _name.equals(ROW_NUM_COLUMN_NAME);
   }

   public static DKColumnModel createRowNumColumnModel() {
      return new DKColumnModel(0, ROW_NUM_COLUMN_NAME, Type.INTEGER);
   }

   /**
    * create new instance that inherits all member values from receiver
    * <em>except</em> table
    */
   public DKColumnModel copy() {
      return new DKColumnModel(_index, _name, _type, _formatString);
   }

   public DKTableModel getTable() {
      return _table;
   }

   public String getName() {
      return _name;
   }

   public int getIndex() {
      return _index;
   }

   public Type getType() {
      return _type;
   }

   public boolean isRowNum() {
      return _isRowNum;
   }

   /**
    * convenience method
    * 
    * @return true if receiver participates in table.key
    */
   public boolean isInKey() {
      if (_table == null)
         return false;
      return _table.isInKey(this);
   }

   public String toString() {
      return String.format("Column[%s]", _name);
   }

   public String getDescription() {
      return String.format("%s[%s,%s,%s,%s]",
         ClassUtils.getShortClassName(this.getClass()), _index, _name, _type,
         _formatString);
   }

   public Object parseObject(String stringValue_) throws ParseException {
      if (StringUtils.isEmpty(stringValue_))
         return null;
      if (_format == null)
         return stringValue_;
      return _format.parseObject(stringValue_);
   }

   public boolean equals(Object target_) {
      if (target_ == null)
         return false;
      if (target_ == this)
         return true;
      if (target_.getClass() != getClass())
         return false;

      DKColumnModel rhs = (DKColumnModel) target_;

      EqualsBuilder builder = new EqualsBuilder();
      builder.append(_name, rhs._name);
      builder.append(_index, rhs._index);
      builder.append(_type, rhs._type);
      return builder.isEquals();
   }

   public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder(23, 71);
      builder.append(_name);
      builder.append(_index);
      builder.append(_type);
      return builder.toHashCode();
   }

   private Format createFormat(Type type_, String formatString_) {
      switch (type_) {

      case STRING:
         return null;
      case INTEGER: {
         if (formatString_ == null)
            return null;
         return new DecimalFormat(formatString_);
      }
      case REAL: {
         if (formatString_ == null)
            return null;
         return new DecimalFormat(formatString_);
      }
      case DECIMAL: {
         if (formatString_ == null)
            return null;
         return new DecimalFormat(formatString_);
      }
      case DATE: {
         if (formatString_ == null)
            formatString_ = DEFAULT_DATE_FORMAT_STRING;
         return new DecimalFormat(formatString_);
      }
      case TIME: {
         if (formatString_ == null)
            formatString_ = DEFAULT_TIME_FORMAT_STRING;
         return new DecimalFormat(formatString_);
      }
      case TIMESTAMP: {
         if (formatString_ == null)
            formatString_ = DEFAULT_TIMESTAMP_FORMAT_STRING;
         return new DecimalFormat(formatString_);
      }
      case BOOLEAN:
         return null;
      case TEXT:
         return null;
      case MIXED:
         return null;

      default:
         throw new RuntimeException(String.format("unhandled type_->%s", type_));
      }
   }

}
