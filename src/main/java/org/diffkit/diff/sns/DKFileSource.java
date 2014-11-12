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
package org.diffkit.diff.sns;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.URI;
import java.text.ParseException;
import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKRuntime;
import org.diffkit.common.DKUserException;
import org.diffkit.common.DKValidate;
import org.diffkit.common.annot.NotThreadSafe;
import org.diffkit.diff.engine.DKColumnModel;
import org.diffkit.diff.engine.DKContext;
import org.diffkit.diff.engine.DKSource;
import org.diffkit.diff.engine.DKTableModel;
import org.diffkit.util.DKArrayUtil;
import org.diffkit.util.DKFileUtil;

/**
 * @author jpanico
 */
@NotThreadSafe
public class DKFileSource implements DKSource {

   private final File _file;
   private final String _delimiter;
   /**
    * read from the first line of actual file
    */
   private String[] _headerColumnNames;
   private DKTableModel _model;
   private final String[] _keyColumnNames;
   /**
    * DKColumnModel indices
    */
   private final int[] _readColumnIdxs;
   private DKColumnModel[] _readColumns;
   private final boolean _isSorted;
   private final boolean _validateLazily;
   private transient boolean _isOpen;
   private transient long _lastIndex = -1;
   private transient LineNumberReader _lineReader;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   /**
    * @param readColumnIdxs_
    *           instructs Source to only read a subset of columns. null value
    *           means all Columns will be read and must be modelled
    */
   public DKFileSource(String filePath_, DKTableModel model_, int[] readColumnIdxs_,
                       String delimiter_) throws IOException {
      this(filePath_, model_, null, readColumnIdxs_, delimiter_, true, true);
   }

   /**
    * @param readColumnIdxs_
    *           instructs Source to only read a subset of columns. null value
    *           means all Columns will be read and must be modelled
    */
   public DKFileSource(String filePath_, DKTableModel model_, String[] keyColumnNames_,
                       int[] readColumnIdxs_, String delimiter_, boolean isSorted_,
                       boolean validateLazily_) throws IOException {

      _log.debug("filePath_->{}", filePath_);
      _log.debug("model_->{}", model_);
      _log.debug("keyColumnNames_->{}", keyColumnNames_);
      _log.debug("readColumnIdxs_->{}", readColumnIdxs_);
      _log.debug("delimiter_->{}", delimiter_);
      _log.debug("isSorted_->{}", isSorted_);
      _log.debug("validateLazily_->{}", validateLazily_);

      if ((model_ != null) && (keyColumnNames_ != null))
         throw new RuntimeException(String.format("does not allow both %s and %s params",
            "model_", "keyColumnNames_"));

      _file = DKFileUtil.findFile(filePath_);
      _delimiter = delimiter_;
      _model = model_;
      _keyColumnNames = keyColumnNames_;
      _readColumnIdxs = readColumnIdxs_;
      if (_readColumnIdxs != null)
         throw new NotImplementedException(String.format(
            "_readColumnIdxs->%s is not currently supported", _readColumnIdxs));

      _isSorted = isSorted_;
      _validateLazily = validateLazily_;
      DKValidate.notNull(_delimiter);
      if (!_isSorted)
         throw new NotImplementedException(String.format(
            "isSorted_->%s is not currently supported", _isSorted));
      if (!_validateLazily) {
         if (_file == null)
            throw new RuntimeException(String.format(
               "could not find file for filePath_->%s", filePath_));
         this.open();
      }
   }

   public File getFile() {
      return _file;
   }

   public String getDelimeter() {
      return _delimiter;
   }

   public DKTableModel getModel() {
      if (_model != null)
         return _model;
      try {
         this.open();
      }
      catch (IOException e_) {
         throw new RuntimeException(e_);
      }
      int[] keyColumnIndices = null;
      if (_keyColumnNames == null)
         keyColumnIndices = new int[] { 0 };
      else
         keyColumnIndices = this.getHeaderColumnNameIndices(_keyColumnNames);

      _model = DKTableModel.createGenericStringModel(_headerColumnNames, keyColumnIndices);
      return _model;
   }

   private int[] getHeaderColumnNameIndices(String[] names_) {
      if (names_ == null)
         return null;
      int[] indices = new int[names_.length];
      Arrays.fill(indices, -1);
      for (int i = 0, j = 0; i < names_.length; i++) {
         int foundAt = ArrayUtils.indexOf(_headerColumnNames, names_[i]);
         if (foundAt < 0)
            throw new RuntimeException(String.format(
               "no value in _headerColumnNames for %s", names_[i]));
         indices[j++] = foundAt;
      }
      return DKArrayUtil.compactFill(indices, -1);
   }

   public String[] getKeyColumnNames() {
      return _keyColumnNames;
   }

   public int[] getReadColumnIdxs() {
      return _readColumnIdxs;
   }

   public boolean getIsSorted() {
      return _isSorted;
   }

   public boolean getValidateLazily() {
      return _validateLazily;
   }

   public Kind getKind() {
      return Kind.FILE;
   }

   public URI getURI() throws IOException {
      return _file.toURI();
   }

   public String toString() {
      if (DKRuntime.getInstance().getIsTest())
         return _file.getName();
      return String.format("%s@%x[%s]", ClassUtils.getShortClassName(this.getClass()),
         System.identityHashCode(this), _file.getPath());
   }

   public Object[] getNextRow() throws IOException {
      this.ensureOpen();
      String line = this.readLine();
      if (line == null)
         return null;
      _lastIndex++;
      return createRow(line);
   }

   /**
    * skips blank lines
    * 
    * @return null only when EOF is reached
    */
   private String readLine() throws IOException {
      while (true) {
         String line = _lineReader.readLine();
         if (line == null)
            return null;
         line = StringUtils.trimToNull(line);
         if (line != null)
            return line;
      }
   }

   private Object[] createRow(String line_) throws IOException {
      if (line_ == null)
         return null;
      String[] strings = line_.split(_delimiter, -1);
      DKColumnModel[] readColumns = this.getReadColumns();
      if (strings.length != readColumns.length)
         throw new RuntimeException(String.format(
            "columnCount->%s in row->%s does not match modelled table->%s",
            strings.length, Arrays.toString(strings), _model));
      try {
         Object[] row = new Object[strings.length];
         for (int i = 0; i < strings.length; i++) {
            row[i] = readColumns[i].parseObject(strings[i]);
         }
         return row;
      }
      catch (ParseException e_) {
         _log.error(null, e_);
         throw new RuntimeException(e_);
      }
   }

   private DKColumnModel[] getReadColumns() {
      if (_readColumns != null)
         return _readColumns;
      DKTableModel model = this.getModel();
      if (model == null)
         return null;
      _readColumns = model.getColumns();
      return _readColumns;
   }

   public long getLastIndex() {
      return _lastIndex;
   }

   // @Override
   public void close(DKContext context_) throws IOException {
      this.ensureOpen();
      _lineReader.close();
      _lineReader = null;
      _isOpen = false;
   }

   private void validateFile() {
      if (!_file.canRead())
         throw new DKUserException(String.format("can't read file [%s]", _file));
   }

   // @Override
   public void open(DKContext context_) throws IOException {
      this.open();
   }

   private void open() throws IOException {
      if (_isOpen)
         return;
      _isOpen = true;
      this.validateFile();
      _lineReader = new LineNumberReader(new BufferedReader(new FileReader(_file)));
      this.readHeader();
   }

   private void readHeader() throws IOException {
      String line = this.readLine();
      _log.info("header->{}", line);
      _headerColumnNames = line.split(_delimiter);
   }

   private void ensureOpen() {
      if (!_isOpen)
         throw new RuntimeException("not open!");
   }
}
