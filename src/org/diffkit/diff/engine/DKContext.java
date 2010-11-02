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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.time.StopWatch;

import org.diffkit.common.DKValidate;

/**
 * @author jpanico
 */
public class DKContext {

   public enum UserKey {
      PLAN_FILES, DIFF_COLUMN_NAMES, IGNORE_COLUMN_NAMES;
   }

   public final DKSource _lhs;
   public final DKSource _rhs;
   public final DKSink _sink;
   public final DKTableComparison _tableComparison;
   public final Map<UserKey, Object> _userDictionary = new HashMap<UserKey, Object>();
   public final long _id = UUID.randomUUID().getLeastSignificantBits();
   // current or last
   public long _rowStep;
   // current or last
   public int _columnStep;
   private final StopWatch _stopwatch = new StopWatch();

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

   public DKContext(DKSource lhs_, DKSource rhs_, DKSink sink_, DKTableComparison plan_,
                    Map<UserKey, ?> userDictionary_) {
      _lhs = lhs_;
      _rhs = rhs_;
      _sink = sink_;
      _tableComparison = plan_;
      if (userDictionary_ != null)
         _userDictionary.putAll(userDictionary_);
      DKValidate.notNull(_lhs, _rhs, _sink, _tableComparison);
   }

   public void open() throws IOException {
      _sink.open(this);
      _lhs.open(this);
      _rhs.open(this);
      _stopwatch.start();
   }

   public void close() throws IOException {
      _stopwatch.stop();
      _sink.close(this);
      _lhs.close(this);
      _rhs.close(this);
   }

   public String getElapsedTimeString() {
      return _stopwatch.toString();
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
