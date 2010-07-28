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
package org.diffkit.diff.engine;

import java.util.UUID;

import org.diffkit.common.DKValidate;

/**
 * @author jpanico
 */
public class DKContext {

   public final DKSource _lhs;
   public final DKSource _rhs;
   public final DKSink _sink;
   public final DKTableComparison _tableComparison;
   public final long _id = UUID.randomUUID().getLeastSignificantBits();
   // current or last
   public long _rowStep;
   // current or last
   public int _columnStep;

   /**
    * for testing only
    */
   @SuppressWarnings("unused")
   private DKContext() {
      _lhs = null;
      _rhs = null;
      _sink = null;
      _tableComparison = null;
   }

   public DKContext(DKSource lhs_, DKSource rhs_, DKSink sink_, DKTableComparison plan_) {
      _lhs = lhs_;
      _rhs = rhs_;
      _sink = sink_;
      _tableComparison = plan_;
      DKValidate.notNull(_lhs, _rhs, _sink, _tableComparison);
   }

   public DKSource getLhs() {
      return _lhs;
   }

   public DKSource getRhs() {
      return _rhs;
   }

   public DKSink getSink() {
      return _sink;
   }

   public DKTableComparison getTableComparison() {
      return _tableComparison;
   }

   public long getRowStep() {
      return _rowStep;
   }

   public int getColumnStep() {
      return _columnStep;
   }
}
