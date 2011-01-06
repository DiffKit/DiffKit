/**
 * Copyright 2010 Kiran Ratnapu
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

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;
import org.diffkit.common.annot.NotThreadSafe;
import org.diffkit.diff.engine.DKContext;
import org.diffkit.diff.engine.DKSource;
import org.diffkit.diff.engine.DKTableModel;

/**
 * @author kratnapu
 */
@NotThreadSafe
public class DKSpreadSheetFileSource implements DKSource {

   private final DKSheet _sheet;
   private final DKTableModel _model;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());
   private Iterator<Object[]> _rowIterator;
   private boolean _isOpen;

   public DKSpreadSheetFileSource(String filePath_, String sheetName_,
                                  DKTableModel model_, String[] keyColumnNames_,
                                  int[] readColumnIdxs_, boolean isSorted_,
                                  boolean hasHeader_, boolean validateLazily_)
      throws IOException {
      DKValidate.notNull(filePath_);
      _sheet = createSheet(filePath_, sheetName_, isSorted_, hasHeader_, validateLazily_);
      DKValidate.notNull(_sheet);
      _model = determineModel(model_, _sheet, keyColumnNames_);
      DKValidate.notNull(_model);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.diffkit.diff.engine.DKSourceSink#open(org.diffkit.diff.engine.DKContext
    * )
    */
   public void open(DKContext context_) throws IOException {
      throw new NotImplementedException();
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.diffkit.diff.engine.DKSourceSink#close(org.diffkit.diff.engine.DKContext
    * )
    */
   public void close(DKContext context_) throws IOException {
      throw new NotImplementedException();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.diffkit.diff.engine.DKSourceSink#getKind()
    */
   public Kind getKind() {
      return Kind.EXCEL;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.diffkit.diff.engine.DKSource#getModel()
    */
   public DKTableModel getModel() {
      return _model;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.diffkit.diff.engine.DKSource#getURI()
    */
   public URI getURI() throws IOException {
      throw new NotImplementedException();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.diffkit.diff.engine.DKSource#getNextRow()
    */
   public Object[] getNextRow() throws IOException {
      throw new NotImplementedException();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.diffkit.diff.engine.DKSource#getLastIndex()
    */
   public long getLastIndex() {
      throw new NotImplementedException();
   }

   /**
    * if model_, then returns that. Else, extracts the model from sheet_. If
    * extracting from sheet and keyColumnNames_ are specifies, then will attempt
    * to modify the sheet derived model to override the default key from sheet.
    */
   private static DKTableModel determineModel(DKTableModel model_, DKSheet sheet_,
                                              String[] keyColumnNames_) {
      throw new NotImplementedException();
   }

   /**
    * Factory method that uses the file extension on filePath_ to determine
    * which DKSheet implementation class to use. Once it finds a handler class,
    * it calls constructor with args [file_, sheetName_, isSorted_, hasHeader_,
    * validateLazily_]
    * 
    * @return a new instance of a DKSheet implementation
    */
   private static DKSheet createSheet(String filePath_, String sheetName_,
                                      boolean isSorted_, boolean hasHeader_,
                                      boolean validateLazily_) {
      throw new NotImplementedException();
   }
}
