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
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.list.GrowthList;
import org.apache.commons.collections.list.LazyList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.diff.engine.DKColumnModel;
import org.diffkit.diff.engine.DKColumnModel.Type;
import org.diffkit.diff.engine.DKTableModel;
import org.diffkit.util.DKStreamUtil;

/**
 * @author kratnapu
 */
public class DKPoiSheet extends DKAbstractSheet {

   public static final String[] HANDLED_FILE_EXTENSIONS = { "xls", "xlsx" };
   private static final Map<Integer, Type> _numericCellTypes = new HashMap<Integer, Type>();

   private Workbook _workbook;
   private Sheet _sheet;
   private List<Row> _rows;
   private Row _headerRow;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());
   private final boolean _isDebugEnabled = _log.isDebugEnabled();

   public DKPoiSheet(File file_, String name_, boolean isSorted_, boolean hasHeader_,
                     boolean validateLazily_) {
      super(file_, name_, isSorted_, hasHeader_, validateLazily_);
   }

   public Iterator<Object[]> getRowIterator(DKTableModel model_) {
      throw new NotImplementedException();
   }

   /**
    * the Model created directly from the sheet always contains the <ROW_NUM>
    * pseudo-column
    */
   protected DKTableModel createModelFromSheet() throws IOException {
      List<Type> columnTypes = discoverColumnTypes(this.getRows());
      if (_isDebugEnabled)
         _log.debug("columnTypes->{}", columnTypes);
      String[] columNames = this.getColumnNames(columnTypes.size());
      if (_isDebugEnabled)
         _log.debug("columNames->{}", Arrays.toString(columNames));
      DKColumnModel[] columnModels = new DKColumnModel[columnTypes.size() + 1];
      columnModels[0] = DKColumnModel.createRowNumColumnModel();
      for (int i = 1; i < columnModels.length; i++)
         columnModels[i] = new DKColumnModel(i, columNames[i - 1], columnTypes.get(i - 1));
      int[] key = { 0 };
      return new DKTableModel(this.getNameFromSheet(), columnModels, key);
   }

   private String[] getColumnNames(int columnCount_) throws IOException {
      Row header = this.getHeaderRow();
      if (header == null)
         return createDefaultColumnNames(columnCount_);

      int headerWidth = header.getLastCellNum();
      if (columnCount_ != headerWidth)
         throw new RuntimeException(String.format(
            "headerWidth->%s does not match columnCount_->%s", headerWidth, columnCount_));
      String[] columnNames = new String[columnCount_];
      for (int i = 0; i < columnCount_; i++)
         columnNames[i] = header.getCell(i).getStringCellValue();
      return columnNames;
   }

   private static String[] createDefaultColumnNames(int columnCount_) {
      String[] defaultColumnNames = new String[columnCount_];
      for (int i = 0; i < columnCount_; i++)
         defaultColumnNames[i] = getDefaultColumnName(i);
      return defaultColumnNames;
   }

   private Row getHeaderRow() throws IOException {
      if (!this.hasHeader())
         return null;
      if (_headerRow != null)
         return _headerRow;
      List<Row> rows = this.getRows();
      if (CollectionUtils.isEmpty(rows))
         return null;
      _headerRow = rows.get(0);
      return _headerRow;
   }

   @SuppressWarnings("unchecked")
   private List<Row> getRows() throws IOException {
      if (_rows != null)
         return _rows;
      Sheet sheet = this.getSheet();
      if (sheet == null)
         throw new IOException(String.format("no sheet!"));
      Iterator<Row> rowIterator = sheet.rowIterator();
      _rows = IteratorUtils.toList(rowIterator);
      _log.info("row count->{}", (_rows == null) ? 0 : _rows.size());
      return _rows;
   }

   protected String getNameFromSheet() throws IOException {
      Sheet sheet = this.getSheet();
      _log.debug("sheet->{}", sheet);
      if (sheet == null)
         throw new IOException(String.format("no sheet!"));
      return sheet.getSheetName();
   }

   private Sheet getSheet() throws IOException {
      if (_sheet != null)
         return _sheet;
      Workbook workbook = this.getWorkbook();
      _log.debug("workbook->{}", workbook);
      if (workbook == null)
         throw new IOException("couldn't get workbook");
      String requestedName = this.getRequestedName();
      _log.debug("requestedName->{}", requestedName);
      if (requestedName != null) {
         _sheet = workbook.getSheet(requestedName);
         if (_sheet == null)
            throw new IOException(String.format(
               "couldn't find sheet->'%s' in workbook->'%s'", requestedName,
               this.getFile()));
      }
      else {
         _sheet = workbook.getSheetAt(0);
         _log.debug("no sheet specified, using first sheet");
      }
      _log.debug("_sheet->{}", _sheet);
      return _sheet;
   }

   private Workbook getWorkbook() throws IOException {
      if (_workbook != null)
         return _workbook;
      try {
         _workbook = WorkbookFactory.create(DKStreamUtil.ensureBuffered(FileUtils.openInputStream(this.getFile())));
         return _workbook;
      }
      catch (InvalidFormatException e_) {
         _log.error(null, e_);
         throw new IOException(e_);
      }
   }

   @SuppressWarnings("unchecked")
   private List<Type> discoverColumnTypes(List<Row> rows_) {
      if (CollectionUtils.isEmpty(rows_))
         return null;
      List<Type> columnTypes = GrowthList.decorate(LazyList.decorate(
         new ArrayList<Type>(), FactoryUtils.nullFactory()));
      int start = this.hasHeader() ? 1 : 0;
      for (int i = start; i < rows_.size(); i++) {
         Row aRow = rows_.get(i);
         int width = aRow.getLastCellNum();
         for (int j = 0; j < width; j++) {
            Cell cell = aRow.getCell(j);
            if (cell == null)
               continue;
            if (_isDebugEnabled) {
               _log.debug(String.format("cell->%s formatString->%s format->%s",
                  cell.getColumnIndex(), cell.getCellStyle().getDataFormatString(),
                  cell.getCellStyle().getDataFormat()));
            }
            Type cellType = mapColumnType(cell);
            Type columnType = columnTypes.get(j);
            if (_isDebugEnabled)
               _log.debug("cellType->{} columnType->{}", cellType, columnType);
            if (columnType == null)
               columnTypes.set(j, cellType);
            else if (columnType != cellType)
               columnTypes.set(j, Type.MIXED);
         }
      }
      return columnTypes;
   }

   private static Type mapColumnType(Cell cell_) {
      if (cell_ == null)
         return null;

      switch (cell_.getCellType()) {

      case Cell.CELL_TYPE_STRING:
         return Type.STRING;
      case Cell.CELL_TYPE_NUMERIC:
         return getTypeForNumericCell(cell_);
      case Cell.CELL_TYPE_BOOLEAN:
         return Type.BOOLEAN;
      default:
         return Type.STRING;
      }
   }

   /**
    * guaranteed to not return null-- defaults to DECIMAL as last resort
    */
   private static Type getTypeForNumericCell(Cell cell_) {
      if (cell_ == null)
         return null;
      int formatNumber = cell_.getCellStyle().getDataFormat();
      Type type = _numericCellTypes.get(formatNumber);
      if (type != null)
         return type;
      type = mapTypeForFormatString(cell_.getCellStyle().getDataFormatString());
      if (type == null)
         type = Type.DECIMAL;
      _numericCellTypes.put(formatNumber, type);
      return type;
   }

   /**
    * map the "builtin" formats to Types
    */
   private static Type mapTypeForFormatString(String format_) {
      if (format_ == null)
         return null;
      if (format_.equalsIgnoreCase("general"))
         return Type.DECIMAL;
      else if (format_.equals("0"))
         return Type.INTEGER;
      else if (format_.equals("0.00"))
         return Type.DECIMAL;
      else if (format_.equals("#,##0"))
         return Type.INTEGER;
      else if (format_.equals("#,##0.00"))
         return Type.DECIMAL;
      else if (format_.equals("0%"))
         return Type.INTEGER;
      else if (format_.equals("0.00%"))
         return Type.DECIMAL;
      else if (format_.equalsIgnoreCase("mm/dd/yy"))
         return Type.DATE;
      else if (StringUtils.startsWithIgnoreCase(format_, "hh:mm:ss"))
         return Type.TIME;
      else if (StringUtils.startsWithIgnoreCase(format_, "hh:mm"))
         return Type.TIME;
      else if (StringUtils.startsWithIgnoreCase(format_, "h:mm"))
         return Type.TIME;
      else if (StringUtils.startsWithIgnoreCase(format_, "h:mm:ss"))
         return Type.TIME;
      else if (StringUtils.containsIgnoreCase(format_, "dd/yy")
         && StringUtils.containsIgnoreCase(format_, "hh:mm"))
         return Type.TIMESTAMP;
      else if (format_.equalsIgnoreCase("m/d/yy"))
         return Type.DATE;
      else if (format_.equalsIgnoreCase("d-mmm-yy"))
         return Type.DATE;
      else if (format_.equalsIgnoreCase("d-mmm"))
         return Type.DATE;
      else if (format_.equalsIgnoreCase("mmm-yy"))
         return Type.DATE;
      else if (format_.equalsIgnoreCase("m/d/yy h:mm"))
         return Type.TIMESTAMP;
      else if (format_.equals("$#,##0_);($#,##0)"))
         return Type.INTEGER;
      else if (format_.equals("$#,##0_);[Red]($#,##0)"))
         return Type.INTEGER;
      else if (format_.equals("$#,##0.00);($#,##0.00)"))
         return Type.DECIMAL;
      else if (format_.equals("$#,##0.00_);[Red]($#,##0.00)"))
         return Type.DECIMAL;
      else if (format_.equals("0.00E+00"))
         return Type.DECIMAL;
      else if (format_.equals("# ?/?"))
         return Type.REAL;
      else if (format_.equals("# ??/??"))
         return Type.REAL;
      else if (format_.equals("#,##0_);[Red](#,##0)"))
         return Type.INTEGER;
      else if (format_.equals("#,##0.00_);(#,##0.00)"))
         return Type.DECIMAL;
      else if (format_.equals("#,##0.00_);[Red](#,##0.00)"))
         return Type.DECIMAL;
      else if (format_.equalsIgnoreCase("mm:ss"))
         return Type.TIME;
      else if (format_.equalsIgnoreCase("[h]:mm:ss"))
         return Type.TIME;
      else if (format_.equalsIgnoreCase("mm:ss.0"))
         return Type.TIME;
      else if (format_.equals("##0.0E+0"))
         return Type.INTEGER;
      else
         return Type.DECIMAL;
   }

   /**
    * if hasRowNum_, types_[0] will represent the ROW_NUM column
    */
   private static Object[] readRow(Row row_, Type[] types_, boolean hasRowNum_) {
      if ((row_ == null) || (types_ == null))
         return null;
      Object[] result = new Object[types_.length];
      int start = hasRowNum_ ? 1 : 0;
      if (hasRowNum_)
         result[0] = (Integer) row_.getRowNum();

      for (int i = start; i < types_.length; i++)
         result[i] = readCell(row_.getCell(i - 1), types_[i]);

      return result;
   }

   private static Object readCell(Cell cell_, Type type_) {
      if ((cell_ == null) || (type_ == null))
         return null;
      if (cell_.getCellType() == Cell.CELL_TYPE_BLANK)
         return null;
      switch (type_) {
      case STRING:
         return cell_.getStringCellValue();
      case DATE:
         return cell_.getDateCellValue();
      case DECIMAL:
         return new BigDecimal(cell_.getNumericCellValue());
      case INTEGER:
         return new Long(new Double(cell_.getNumericCellValue()).longValue());
      case REAL:
         return new Double(cell_.getNumericCellValue());
      case BOOLEAN:
         return Boolean.valueOf(cell_.getBooleanCellValue());
      case TIME:
         return readTime(cell_);
      case TIMESTAMP:
         return readTimestamp(cell_);
      case MIXED:
         return cell_.toString();
      default:
         return cell_.toString();
      }
   }

   private static Time readTime(Cell cell_) {
      Date dateValue = cell_.getDateCellValue();
      if (dateValue == null)
         return null;
      return new Time(dateValue.getTime());
   }

   private static Timestamp readTimestamp(Cell cell_) {
      Date dateValue = cell_.getDateCellValue();
      if (dateValue == null)
         return null;
      return new Timestamp(dateValue.getTime());
   }

   private static class RowIterator {
      int _currentIndex = -1;
   }
}
