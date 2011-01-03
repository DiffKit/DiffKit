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
package org.diffkit.diff.sns;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.poi.ss.usermodel.Row;

import org.diffkit.diff.engine.DKTableModel;

/**
 * @author jpanico
 */
public class DKPoiSheet extends DKAbstractSheet {

   public static final String[] HANDLED_FILE_EXTENSIONS = { "xls", "xlsx" };

   private List<Row> _rows;
   private Row _headerRow;

   public DKPoiSheet(File file_, String name_, boolean isSorted_, boolean hasHeader_,
                     boolean validateLazily_) {
      super(file_, name_, isSorted_, hasHeader_, validateLazily_);
   }

   public Iterator<Object[]> getRowIterator(DKTableModel model_) {
      throw new NotImplementedException();
   }

   protected DKTableModel createModelFromSheet() {
      // TODO Auto-generated method stub
      return null;
   }

   protected String getNameFromSheet(int sheetIndex_) {
      // TODO Auto-generated method stub
      return null;
   }

}
