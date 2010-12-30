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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKUserException;
import org.diffkit.common.annot.NotThreadSafe;
import org.diffkit.diff.engine.DKColumnModel;
import org.diffkit.diff.engine.DKColumnModel.Type;
import org.diffkit.diff.engine.DKContext;
import org.diffkit.diff.engine.DKSource;
import org.diffkit.diff.engine.DKTableModel;
import org.diffkit.util.DKArrayUtil;
import org.diffkit.util.DKResourceUtil;

/**
 * @author kratnapu
 */
@NotThreadSafe
public class DKSpreadSheetFileSource implements DKSource {

   private final File _file;
   private boolean _hasHeader = true;
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
   private final boolean _isSorted;
   private boolean _sortCompleted;
   private final boolean _validateLazily;
   private final String _sheetName;
   private int _totalRows;
   private transient long _lineCounter = 0;
   private transient boolean _isOpen;
   private transient long _lastIndex = -1;
   private Sheet _sheet;
   private List<Row> _rows;
   private Row _headerRow;
   private static final String COLUMN_NAMES[] = { "A", "B", "C", "D", "E", "F", "G", "H",
      "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
      "Y", "Z" };
   private static final int MAX_DEFAULT_HEADER_COLUMNS = 52;

   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   /**
    * @param readColumnIdxs_
    *           instructs Source to only read a subset of columns. null value
    *           means all Columns will be read and must be modelled
    */
   public DKSpreadSheetFileSource(String filePath_, String excelSheetName_,
                                  DKTableModel model_, int[] readColumnIdxs_)
      throws IOException {
      this(filePath_, model_, null, readColumnIdxs_, false, true, true, excelSheetName_);
   }

   public DKSpreadSheetFileSource(String filePath_, DKTableModel model_,
                                  String[] keyColumnNames_, int[] readColumnIdxs_,
                                  boolean isSorted_, boolean hasHeader_,
                                  boolean validateLazily_) throws IOException {
      this(filePath_, model_, keyColumnNames_, readColumnIdxs_, isSorted_, hasHeader_,
         validateLazily_, null);
   }

   /**
    * @param readColumnIdxs_
    *           instructs Source to only read a subset of columns. null value
    *           means all Columns will be read and must be modelled
    */
   public DKSpreadSheetFileSource(String filePath_, DKTableModel model_,
                                  String[] keyColumnNames_, int[] readColumnIdxs_,
                                  boolean isSorted_, boolean hasHeader_,
                                  boolean validateLazily_, String sheetName_)
      throws IOException {

      _log.debug("filePath_->{}", filePath_);
      _log.debug("model_->{}", model_);
      _log.debug("keyColumnNames_->{}", keyColumnNames_);
      _log.debug("readColumnIdxs_->{}", readColumnIdxs_);
      _log.debug("isSorted_->{}", isSorted_);
      _log.debug("validateLazily_->{}", validateLazily_);
      _log.debug("hasHeader_->{}", hasHeader_);
      _log.debug("sheetName_->{}", sheetName_);

      if ((model_ != null) && (keyColumnNames_ != null))
         throw new RuntimeException(String.format("does not allow both %s and %s params",
            "model_", "keyColumnNames_"));

      _file = this.getFile(filePath_);
      _model = model_;
      _keyColumnNames = keyColumnNames_;
      _readColumnIdxs = readColumnIdxs_;
      if (_readColumnIdxs != null)
         throw new NotImplementedException(String.format(
            "_readColumnIdxs->%s is not currently supported", _readColumnIdxs));

      _isSorted = isSorted_;
      _validateLazily = validateLazily_;
      _sheetName = sheetName_;
      _hasHeader = hasHeader_;
      /*
       * if (!_isSorted) throw new NotImplementedException(String.format(
       * "isSorted_->%s is not currently supported", _isSorted));
       */
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

      DKColumnModel[] columnModels = getColumnModels();
      _model = new DKTableModel("TODO(TARGET-SHEET)", columnModels, keyColumnIndices);
      return _model;
   }

   // Max columns across all rows
   private int getTotalColumns() {
      // Get the maximum number of columns: there is no method in POI which does
      // it
      int maxColumns = 0;
      for (Row row : _rows) {
         if (maxColumns < row.getLastCellNum()) {
            maxColumns = row.getLastCellNum();
         }
      }
      return maxColumns;
   }

   private DKColumnModel[] getColumnModels() {
      int totalColumns = getTotalColumns();
      if (totalColumns == 0) {
         throw new RuntimeException("No Data in the spreadsheet");
      }
      if (_hasHeader && totalColumns != _headerColumnNames.length) {
         _log.info("Total Columns(" + totalColumns
            + ") across all the rows in the spreasheet and total columns "
            + "for the header (" + _headerColumnNames.length
            + ") row don't match. Defaulting to total Header columns");
         totalColumns = _headerColumnNames.length;
      }
      _log.info("Total Columns:" + totalColumns);

      DKColumnModel[] columns = new DKColumnModel[totalColumns];
      for (int i = 0; i < totalColumns; i++) {
         Type columnType = null;
         for (Row row : _rows) {
            if (_hasHeader && row.getRowNum() == 0) {
               continue; // skip the header
            }
            Cell cell = row.getCell(i);
            if (cell == null) {
               continue; // dont infer type if there is no content in the cell
            }
            Type cellType = mapColumntype(cell);
            // TODO make it work for non empty rows
            if (row.getRowNum() == 1) { // Initialize column type from the first
                                        // data row
               columnType = cellType;
            }
            else if (cellType == null || cellType != columnType) {
               columnType = Type.STRING; // At the first instance of
                                         // unrecognized type
               _log.warn("Mixed cell types for column:'" + _headerColumnNames[i]
                  + "'. Defaulting to String type for this column.");
               break;
            }

         }
         if (columnType == null) {
            columnType = Type.STRING;
         }
         columns[i] = new DKColumnModel(i, _headerColumnNames[i], columnType);
      }
      return columns;
   }

   // Only non null cell as input
   private Type mapColumntype(Cell cell) {

      Type type;
      switch (cell.getCellType()) {

      case Cell.CELL_TYPE_STRING:
         type = Type.STRING;
         break;
      case Cell.CELL_TYPE_NUMERIC:
         if (DateUtil.isCellDateFormatted(cell)) {
            type = Type.DATE;
         }
         else {
            // Format format = _dataFormatter.getDefaultFormat(cell);
            type = Type.DECIMAL;
         }
         break;
      case Cell.CELL_TYPE_BOOLEAN:
         type = Type.BOOLEAN;
         break;
      default:
         type = null;
      }
      return type;
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
      return String.format("%s@%x[%s]", ClassUtils.getShortClassName(this.getClass()),
         System.identityHashCode(this), _file.getAbsolutePath());
   }

   public Object[] getNextRow() throws IOException {
      this.ensureOpen();
      this.ensureSorted();

      Row row = this.readLine();
      if (row == null)
         return null;
      _lastIndex++;
      return createRow(row);
   }

   private void ensureSorted() {
      if (_isSorted || _sortCompleted)
         return;
      _log.debug("_sortCompleted" + _sortCompleted);
      int[] keyColumnIndices = null;
      if (_keyColumnNames == null)
         keyColumnIndices = new int[] { 0 };
      else
         keyColumnIndices = this.getHeaderColumnNameIndices(_keyColumnNames);
      DKTableModel model = getModel();
      sortSpreadSheetRows(_rows, model.getColumns(), keyColumnIndices);
      _sortCompleted = true;
   }

   // Reads all the spreadsheet rows into memory -- skips blank rows
   private void readRows() {
      boolean headerFound = false;
      _rows = new ArrayList<Row>();
      for (int i = 0; i < _sheet.getLastRowNum() + 1; i++) { // LastRowNum is z
                                                             // zero based
                                                             // number
         if (checkIfRowIsEmpty(_sheet.getRow(i))) {
            _log.debug("Empty row at:" + i);
            continue;
         }
         if (_hasHeader && !headerFound) {
            _headerRow = _sheet.getRow(i);
            headerFound = true;
         }
         else {
            _rows.add(_sheet.getRow(i));
         }
      }

   }

   /**
    * @return null only when EOF is reached
    */
   private Row readLine() throws IOException {
      while (true) {
         if (_lineCounter == _rows.size())
            return null;
         Row row = _rows.get((int) _lineCounter);
         _log.debug("row:" + row);
         _lineCounter++;
         return row;
      }
   }

   private Cell[] getCells(Row row) {
      Iterator<Cell> iterator = row.cellIterator();
      if (iterator == null)
         return null;
      _log.debug("No of Cells:" + row.getLastCellNum());
      Cell[] cells = new Cell[row.getLastCellNum()];
      int i = 0;
      while (iterator.hasNext()) {
         Cell cell = iterator.next();
         _log.debug("Cell->", cell);
         cells[i++] = cell;
      }
      _log.debug("Cells->{}", cells);
      return cells;
   }

   private boolean checkIfRowIsEmpty(Row row_) {
      Cell[] cells = getCells(row_);
      if (cells == null) {
         return false;
      }
      for (Cell cell : cells) {
         String content = cell.toString();
         if (content != null && !content.trim().equals("")) {
            return false;
         }
      }
      return true;
   }

   private Object[] createRow(Row excelRow_) {
      Cell[] cells = getCells(excelRow_);
      DKTableModel model = getModel();
      DKColumnModel[] columnModels = model.getColumns();
      if (cells == null)
         return null;
      Object[] values = new Object[columnModels.length];
      for (int i = 0; i < cells.length; i++) {
         values[i] = getCellValue(cells[i], columnModels[i]);
      }
      return values;

   }

   public long getLastIndex() {
      return _lastIndex;
   }

   // @Override
   public void close(DKContext context_) throws IOException {
      this.ensureOpen();
      _sheet = null;
      _isOpen = false;
   }

   private File getFile(String filePath_) {
      if (filePath_ == null)
         return null;
      File fsFile = new File(filePath_);
      if (fsFile.exists())
         return fsFile;
      try {
         File resourceFile = DKResourceUtil.findResourceAsFile(filePath_);
         if (resourceFile != null)
            return resourceFile;
      }
      catch (URISyntaxException e_) {
         throw new RuntimeException(e_);
      }
      return fsFile;
   }

   private void validateFile() {
      if (!_file.canRead())
         throw new DKUserException(String.format("can't read file [%s]", _file));
      _log.info("File:" + _file.getAbsolutePath());
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
      Workbook workbook = null;
      try {

         workbook = WorkbookFactory.create(new FileInputStream(_file));
      }
      catch (Exception e_) {
         _log.error(null, e_);
         throw new IOException(e_);
      }
      _log.info("workbook: " + workbook);
      if (workbook == null) {
         _log.error("couldn't get Workbook for file: " + _file);
      }
      if (_sheetName != null && _sheetName.trim().equals("")) {
         _sheet = workbook.getSheet(_sheetName);
         if (_sheet == null) {
            throw new RuntimeException("Could not open spreadsheet with name:"
               + _sheetName);
         }
      }
      else {
         _log.info("No sheetName specified. Opening the first sheet");
         _sheet = workbook.getSheetAt(0);
         if (_sheet == null) {
            throw new RuntimeException(
               "No sheetName specified and Could not open the first sheet");
         }
      }
      _log.info("_sheet: " + _sheet);
      _totalRows = _sheet.getLastRowNum() + 1; // LastRowNum is zero based
      _log.info("Total Rows in excel file:" + _file.getAbsolutePath() + " : "
         + _totalRows);
      readRows();
      _log.info("Total Rows in excel file(Skipping header and blank lines):"
         + _file.getAbsolutePath() + " : " + _rows.size());
      this.readHeader();
      _log.debug("_headerColumnNames->{}", _headerColumnNames);

   }

   private void sortSpreadSheetRows(List<Row> rows_, DKColumnModel[] columnModels_,
                                    int[] keyColumnIndices_) {
      ComparatorChain comparatorChain = new ComparatorChain();
      for (int i = 0; i < keyColumnIndices_.length; i++) {
         comparatorChain.addComparator(new RowComparator(keyColumnIndices_[i],
            columnModels_[keyColumnIndices_[i]]));
      }
      _log.debug("Sorting on columns:" + keyColumnIndices_);
      _log.debug("Rows before sorting");
      printRows();
      Collections.sort(rows_, comparatorChain);
      _log.debug("Rows after sorting");
      printRows();
   }

   private void printRows() {
      for (Row row : _rows) {
         printRow(row);
      }
   }

   private void printRow(Row row) {
      if (row == null) {
         _log.debug("Null Row");
      }
      StringBuffer rowString = new StringBuffer();
      for (int i = 0; i < row.getLastCellNum(); i++) {
         rowString.append(row.getCell(i) + ",");
      }
      _log.debug("row(" + row.getRowNum() + "):" + rowString.toString());
   }

   private void readHeader() throws IOException {
      // Row row = _sheet.getRow(0);

      if (!_hasHeader) {
         _headerColumnNames = constructDefaultHeader();
      }
      Cell[] headerRowCells = getCells(_headerRow);
      _headerColumnNames = new String[headerRowCells.length];
      for (int i = 0; i < headerRowCells.length; i++) {
         if (headerRowCells[i] == null) {
            _log.error("no header for header column index:" + i);
            _headerColumnNames[i] = null;
         }
         // Header cells have to be strings
         _headerColumnNames[i] = headerRowCells[i].toString();
      }

   }

   private String[] constructDefaultHeader() {

      String[] header = new String[MAX_DEFAULT_HEADER_COLUMNS];
      for (int i = 0; i < 26; i++) {
         header[i] = COLUMN_NAMES[i];
         header[i + 26] = COLUMN_NAMES[i] + COLUMN_NAMES[i];
      }
      return header;
   }

   private void ensureOpen() {
      if (!_isOpen)
         throw new RuntimeException("not open!");
   }

   private Object getCellValue(Cell cell, DKColumnModel columnModel) {
      if (cell == null)
         return null;
      if (columnModel == null) {
         _log.error("No Column Model");
         return cell.toString();
      }
      Object cellValue;
      switch (columnModel._type) {
      case STRING:
         if (cell.getCellType() != Cell.CELL_TYPE_STRING) {
            _log.error("Wrong Cell type. Expecting cell type as String");
            return cell.toString();
         }
         cellValue = cell.getStringCellValue();
         break;
      case DATE:
         if (!(cell.getCellType() == Cell.CELL_TYPE_NUMERIC && DateUtil.isCellDateFormatted(cell))) {
            _log.error("Wrong Cell type. Expecting cell type as Date");
            // cellValue = cell.toString();
         }
         cellValue = cell.getDateCellValue();
         break;
      case DECIMAL:
         if (cell.getCellType() != Cell.CELL_TYPE_NUMERIC) {
            _log.error("Wrong Cell type. Expecting cell type as Numeric");
            // cellValue = cell.toString();
         }
         cellValue = cell.getNumericCellValue();
         break;
      case INTEGER:
         if (cell.getCellType() != Cell.CELL_TYPE_NUMERIC) {
            _log.error("Wrong Cell type. Expecting cell type as Numeric");
            // cellValue = cell.toString();
         }
         cellValue = (int) cell.getNumericCellValue();
         break;
      case BOOLEAN:
         if (cell.getCellType() != Cell.CELL_TYPE_BOOLEAN) {
            _log.error("Wrong Cell type. Expecting cell type as Boolean");
            // cellValue = cell.toString();
         }
         cellValue = cell.getBooleanCellValue();
         break;
      default:
         cellValue = cell.toString();
      }
      return cellValue;
   }

   public class RowComparator implements java.util.Comparator<Row> {
      private int _compareColumnIndex;
      private DKColumnModel _compareColumnModel;

      public RowComparator(int compareColumnIndex, DKColumnModel compareColumnModel) {
         _compareColumnIndex = compareColumnIndex;
         _compareColumnModel = compareColumnModel;
      }

      public int compare(Row row1, Row row2) {

         Cell cell1 = row1.getCell(_compareColumnIndex);
         Cell cell2 = row2.getCell(_compareColumnIndex);

         _log.debug("Comparing rows of indices:" + row1.getRowNum() + "&"
            + row2.getRowNum());
         _log.debug("Comapring cells of index:" + _compareColumnIndex);
         _log.debug("cell1:" + cell1);
         _log.debug("cell2:" + cell2);

         if (cell1 != null && cell2 != null) {
            Object value1 = getCellValue(cell1, _compareColumnModel);
            Object value2 = getCellValue(cell2, _compareColumnModel);

            _log.debug("value1:" + value1);
            _log.debug("value2:" + value2);

            if (value1 != null && value2 != null) {

               int result = 0;
               switch (_compareColumnModel._type) {
               case STRING:
                  if (value1 instanceof String && value2 instanceof String) {
                     result = ((String) value1).compareTo((String) value2);
                  }
                  else {
                     _log.error("Either of the cell value types are not as expected from model. Expecting String");
                  }
                  break;
               case DATE:
                  if (value1 instanceof Date && value2 instanceof Date) {
                     result = ((Date) value1).compareTo((Date) value2);
                  }
                  else {
                     _log.error("Either of the cell value types are not as expected from model. Expecting Date");
                  }
                  break;
               case DECIMAL:
                  if (value1 instanceof Double && value2 instanceof Double) {
                     result = ((Double) value1).compareTo((Double) value2);
                  }
                  else {
                     _log.error("Either of the cell value types are not as expected from model. Expecting Double");
                  }
                  break;
               case INTEGER:
                  if (value1 instanceof Double && value2 instanceof Double) {
                     result = ((Double) value1).compareTo((Double) value2);
                  }
                  else {
                     _log.error("Either of the cell value types are not as expected from model. Expecting Double");
                  }
                  break;
               case BOOLEAN:
                  if (value1 instanceof Boolean && value2 instanceof Boolean) {
                     result = ((Boolean) value1).compareTo((Boolean) value2);
                  }
                  else {
                     _log.error("Either of the cell value types are not as expected from model. Expecting Boolean");
                  }
                  break;
               default:
                  // TODO decide what to do in such error situation
                  result = 0;
               }
               _log.debug("Compare result:" + result);
               return result;
            }

            _log.warn("Null values in the cell. Assuming null is less than any filled value");
            if (value1 == null && value2 != null) {
               return -1;
            }
            else if (value1 == null && value2 == null) {
               return 0;
            }
            else if (value1 != null && value2 == null) {
               return 1;
            }

         }

         if (cell1 == null && cell2 != null) {
            return -1;
         }
         else if (cell1 == null && cell2 == null) {
            return 0;
         }
         else if (cell1 != null && cell2 == null) {
            return 1;
         }
         return 0;
      }
   }
}
