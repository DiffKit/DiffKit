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

import org.apache.commons.lang.ArrayUtils;
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

   public static final Class[] HANDLER_CLASSES = { DKPoiSheet.class };

   private final DKSheet _sheet;
   private final DKTableModel _requestedModel;
   private final String[] _requestedKeyColumnNames;
   private DKTableModel _model;
   private Iterator<Object[]> _rowIterator;
   private int _lastIndex = -1;
   private boolean _isOpen;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());
   private final boolean _isDebugEnabled = _log.isDebugEnabled();

   public DKSpreadSheetFileSource(String filePath_, String sheetName_,
                                  DKTableModel requestedModel_,
                                  String[] requestKeyColumnNames_, int[] readColumnIdxs_,
                                  boolean isSorted_, boolean hasHeader_,
                                  boolean validateLazily_) throws IOException {
      if (_isDebugEnabled) {
         _log.debug("filePath_->{}", filePath_);
         _log.debug("sheetName_->{}", sheetName_);
         _log.debug("requestedModel_->{}", requestedModel_);
         _log.debug("requestKeyColumnNames_->{}",
            ArrayUtils.toString(requestKeyColumnNames_));
         _log.debug("readColumnIdxs_->{}", ArrayUtils.toString(readColumnIdxs_));
         _log.debug("isSorted_->{}", isSorted_);
         _log.debug("hasHeader_->{}", hasHeader_);
         _log.debug("validateLazily_->{}", validateLazily_);
      }
      DKValidate.notNull(filePath_);
      _sheet = createSheet(filePath_, sheetName_, isSorted_, hasHeader_, validateLazily_);
      DKValidate.notNull(_sheet);
      _requestedModel = requestedModel_;
      if (!ArrayUtils.isEmpty(readColumnIdxs_))
         throw new NotImplementedException("readColumnIdxs_ not yet supported!");
      _requestedKeyColumnNames = requestKeyColumnNames_;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.diffkit.diff.engine.DKSourceSink#open(org.diffkit.diff.engine.DKContext
    * )
    */
   public void open(DKContext context_) throws IOException {
      _log.debug("context_->{}", context_);
      this.ensureNotOpen();
      _rowIterator = _sheet.getRowIterator(this.getModel());
      _isOpen = true;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.diffkit.diff.engine.DKSourceSink#close(org.diffkit.diff.engine.DKContext
    * )
    */
   public void close(DKContext context_) throws IOException {
      _log.debug("context_->{}", context_);
      this.ensureOpen();
      _sheet.close();
      _rowIterator = null;
      _isOpen = false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.diffkit.diff.engine.DKSourceSink#getKind()
    */
   public Kind getKind() {
      return Kind.SPREADSHEET;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.diffkit.diff.engine.DKSource#getModel()
    */
   public DKTableModel getModel() {
      if (_model != null)
         return _model;
      try {
         _model = determineModel(_requestedModel, _sheet, _requestedKeyColumnNames);
         return _model;
      }
      catch (IOException e_) {
         _log.error(null, e_);
         throw new RuntimeException(e_);
      }
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
      this.ensureOpen();
      if (!_rowIterator.hasNext())
         return null;
      _lastIndex++;
      return _rowIterator.next();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.diffkit.diff.engine.DKSource#getLastIndex()
    */
   public long getLastIndex() {
      return _lastIndex;
   }

   /**
    * if requestedModel_, then returns that. Else, extracts the model from
    * sheet_. If extracting from sheet and keyColumnNames_ are specifies, then
    * will attempt to modify the sheet derived model to override the default key
    * from sheet.
    */
   private static DKTableModel determineModel(DKTableModel requestedModel_,
                                              DKSheet sheet_, String[] keyColumnNames_)
      throws IOException {
      if (requestedModel_ != null)
         return requestedModel_;
      DKTableModel extractedModel = sheet_.getModelFromSheet();
      if (ArrayUtils.isEmpty(keyColumnNames_))
         return extractedModel;
      return extractedModel.copyWithNewKey(keyColumnNames_);
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
      // find the class that handles the extension
      Class handlerClass = findHandlerForWorkbookFile(filePath_);
      // find the designated constructor on that class
      // invoke constructor with args from this method
      throw new NotImplementedException();
   }

   /**
    * guaranteed to not return null-- will throw a RuntimeException if it is
    * unable to find a handler
    */
   private static Class findHandlerForWorkbookFile(String filePath_) {
      throw new NotImplementedException();
   }

   private void ensureOpen() {
      if (!_isOpen)
         throw new RuntimeException("not open!");
   }

   private void ensureNotOpen() {
      if (_isOpen)
         throw new RuntimeException("already open!");
   }
}
