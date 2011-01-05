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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKUserException;
import org.diffkit.common.DKValidate;
import org.diffkit.diff.engine.DKTableModel;

/**
 * @author kratnapu
 */
public abstract class DKAbstractSheet implements DKSheet {

   private final File _file;
   private String _requestedName;
   private final boolean _isSorted;
   private final boolean _hasHeader;
   private final boolean _validateLazily;
   private DKTableModel _modelFromSheet;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   public DKAbstractSheet(File file_, String requestedName_, boolean isSorted_,
                          boolean hasHeader_, boolean validateLazily_) {
      _log.debug("file_->{}", file_);
      _log.debug("requestedName_->{}", requestedName_);
      _log.debug("isSorted_->{}", isSorted_);
      _log.debug("hasHeader_->{}", hasHeader_);
      _log.debug("validateLazily_->{}", validateLazily_);
      _file = file_;
      _requestedName = StringUtils.trimToNull(requestedName_);
      _isSorted = isSorted_;
      _hasHeader = hasHeader_;
      _validateLazily = validateLazily_;
      DKValidate.notNull(_file);
      if (!_validateLazily)
         this.validate();
   }

   protected void validate() {
      if (!_file.canRead())
         throw new DKUserException(String.format("can't read file->%s", _file));
   }

   /**
    * e.g. A...Z,AA..AZ,BA...BZ...
    */
   public static String getDefaultColumnName(int columnIndex_) {
      if (columnIndex_ < 0)
         return null;
      char major = (columnIndex_ / 26 - 1) >= 0 ? (char) ('A' + (columnIndex_ / 26 - 1))
         : '\0';
      char minor = (char) ('A' + columnIndex_ % 26);
      if (major != '\0')
         return "" + major + minor;
      return "" + minor;
   }

   public File getFile() {
      return _file;
   }

   protected String getRequestedName() {
      return _requestedName;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.diffkit.diff.sns.DKSheet#getName()
    */
   public String getName() throws IOException {
      return this.getNameFromSheet();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.diffkit.diff.sns.DKSheet#getModel()
    */
   public DKTableModel getModelFromSheet() throws IOException {
      if (_modelFromSheet != null)
         return _modelFromSheet;
      _modelFromSheet = this.createModelFromSheet();
      return _modelFromSheet;
   }

   protected boolean isSorted() {
      return _isSorted;
   }

   protected boolean hasHeader() {
      return _hasHeader;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.diffkit.diff.sns.DKSheet#getRowIterator()
    */
   public abstract Iterator<Object[]> getRowIterator(DKTableModel model_)
      throws IOException;

   protected abstract String getNameFromSheet() throws IOException;

   protected abstract DKTableModel createModelFromSheet() throws IOException;
}
