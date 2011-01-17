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

import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.BigDecimalConverter;
import org.apache.commons.beanutils.converters.DoubleConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.beanutils.converters.LongConverter;
import org.apache.commons.beanutils.converters.ShortConverter;
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
   private final Converter _lhsConverter;
   private final Class<?> _rhsType;
   private final Converter _rhsConverter;
   private final DKDiffor _diffor;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());
   private final boolean _isDebugEnabled = _log.isDebugEnabled();

   public DKConvertingDiffor(Class<?> lhsType_, Class<?> rhsType_, DKDiffor diffor_) {
      _lhsType = lhsType_;
      _rhsType = rhsType_;
      _lhsConverter = this.getConverter(_lhsType);
      _rhsConverter = this.getConverter(_rhsType);
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
         _log.debug("lhs_ Class->{}", lhs_.getClass());
         _log.debug("rhs_ Class->{}", rhs_.getClass());
      }
      Object convertedLhs = (_lhsConverter == null ? lhs_ : _lhsConverter.convert(
         _lhsType, lhs_));
      Object convertedRhs = (_rhsConverter == null ? rhs_ : _rhsConverter.convert(
         _rhsType, rhs_));
      if (_isDebugEnabled) {
         _log.debug("convertedLhs Class->{}", convertedLhs.getClass());
         _log.debug("convertedRhs Class->{}", convertedRhs.getClass());
      }
      return _diffor.isDiff(convertedLhs, convertedRhs, context_);
   }

   private Converter getConverter(Class<?> type_) {
      if (type_ == null)
         return null;
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
      throw new RuntimeException(String.format("unrecognized type_->%s", type_));
   }

   public String toString() {
      return String.format("%s", ClassUtils.getShortClassName(this.getClass()));
   }

   public String getDescription() {
      return ReflectionToStringBuilder.toString(this);
   }
}
