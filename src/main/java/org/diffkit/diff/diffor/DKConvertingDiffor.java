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
package org.diffkit.diff.diffor;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.BigDecimalConverter;
import org.apache.commons.beanutils.converters.BooleanConverter;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.DoubleConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.beanutils.converters.LongConverter;
import org.apache.commons.beanutils.converters.ShortConverter;
import org.apache.commons.beanutils.converters.SqlTimeConverter;
import org.apache.commons.beanutils.converters.SqlTimestampConverter;
import org.apache.commons.beanutils.converters.StringConverter;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;
import org.diffkit.diff.engine.DKContext;
import org.diffkit.diff.engine.DKDiffor;

/**
 * converts from String to specified types before diff'ng
 * 
 * @author jpanico
 */
public class DKConvertingDiffor implements DKDiffor {

   private final Class<?> _lhsType;
   private final String _lhsFormat;
   private final Converter _lhsConverter;
   private final String _rhsFormat;
   private final Class<?> _rhsType;
   private final Converter _rhsConverter;
   private final DKDiffor _diffor;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());
   private final boolean _isDebugEnabled = _log.isDebugEnabled();

   public DKConvertingDiffor(Class<?> lhsType_, Class<?> rhsType_, DKDiffor diffor_) {
      this(lhsType_, null, rhsType_, null, diffor_);
   }

   public DKConvertingDiffor(Class<?> lhsType_, String lhsFormat_, Class<?> rhsType_,
                             String rhsFormat_, DKDiffor diffor_) {
      _lhsType = lhsType_;
      _lhsFormat = lhsFormat_;
      _rhsType = rhsType_;
      _rhsFormat = rhsFormat_;
      _lhsConverter = this.getConverter(_lhsType, _lhsFormat);
      _rhsConverter = this.getConverter(_rhsType, _rhsFormat);
      _diffor = diffor_;
      DKValidate.notNull(_diffor);
   }

   /**
    * @see org.diffkit.diff.engine.DKDiffor#isDiff(java.lang.Object,
    *      java.lang.Object, org.diffkit.diff.engine.DKContext)
    */
   public boolean isDiff(Object lhs_, Object rhs_, DKContext context_) {
      boolean lhsNull = (lhs_ == null);
      boolean rhsNull = (rhs_ == null);
      if (lhsNull && rhsNull)
         return false;
      if (lhsNull || rhsNull)
         return true;
      if (_isDebugEnabled) {
         _log.debug("_lhsConverter->{}", _lhsConverter);
         _log.debug("_rhsConverter->{}", _rhsConverter);
         _log.debug("lhs_->{} Class->{}", lhs_, lhs_.getClass());
         _log.debug("rhs_->{} Class->{}", rhs_, rhs_.getClass());
      }
      Object convertedLhs = (_lhsConverter == null ? lhs_ : _lhsConverter.convert(
         _lhsType, lhs_));
      Object convertedRhs = (_rhsConverter == null ? rhs_ : _rhsConverter.convert(
         _rhsType, rhs_));
      if (_isDebugEnabled) {
         _log.debug("convertedLhs->{} Class->{}", convertedLhs, convertedLhs.getClass());
         _log.debug("convertedRhs->{} Class->{}", convertedRhs, convertedRhs.getClass());
      }
      return _diffor.isDiff(convertedLhs, convertedRhs, context_);
   }

   private Converter getConverter(Class<?> type_, String format_) {
      if (type_ == null)
         return null;
      if (type_ == String.class)
         return new StringConverter();
      if (type_ == Boolean.class)
         return new BooleanConverter();
      if (type_ == Short.class)
         return new ShortConverter();
      else if (type_ == Integer.class)
         return new IntegerConverter();
      else if (type_ == Long.class)
         return new LongConverter();
      else if (type_ == Double.class)
         return new DoubleConverter();
      else if (type_ == BigDecimal.class)
         return new BigDecimalConverter();
      else if (type_.isAssignableFrom(Date.class)) {
         DateConverter converter = new DateConverter();
         converter.setPattern(format_);
         return converter;
      }
      else if (type_ == Time.class)
         return new SqlTimeConverter();
      else if (type_ == Timestamp.class)
         return new SqlTimestampConverter();
      throw new RuntimeException(String.format("unrecognized type_->%s", type_));
   }

   public String toString() {
      return String.format("%s(%s,%s)", ClassUtils.getShortClassName(this.getClass()),
         _lhsConverter, _rhsConverter);
   }

   public String getDescription() {
      return ReflectionToStringBuilder.toString(this);
   }
}
