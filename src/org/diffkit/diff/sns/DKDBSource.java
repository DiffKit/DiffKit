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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;
import org.diffkit.common.annot.NotThreadSafe;
import org.diffkit.db.DKDBConnectionSource;
import org.diffkit.db.DKDBPrimaryKey;
import org.diffkit.db.DKDBTable;
import org.diffkit.db.DKDBTableDataAccess;
import org.diffkit.diff.engine.DKColumnModel;
import org.diffkit.diff.engine.DKContext;
import org.diffkit.diff.engine.DKSource;
import org.diffkit.diff.engine.DKTableModel;
import org.diffkit.util.DKSqlUtil;

/**
 * @author jpanico
 */
@NotThreadSafe
public class DKDBSource implements DKSource {

   private final String _tableName;
   private final String _whereClause;
   private final DKTableModel _model;
   private final String[] _keyColumnNames;
   private final DKDBConnectionSource _connectionSource;
   private String[] _readColumnNames;
   private final DKDBTable _table;
   private transient Connection _connection;
   private transient ResultSet _resultSet;
   private transient long _lastIndex;
   private transient boolean _isOpen;
   private transient boolean _isValidated;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());
   private final boolean _isDebug = _log.isDebugEnabled();

   public DKDBSource(String tableName_, String whereClause_,
                     DKDBConnectionSource connectionSource_, DKTableModel model_,
                     String[] keyColumnNames_, int[] readColumnIdxs_) throws SQLException {
      _log.info("tableName_->{}", tableName_);
      _log.info("whereClause_->{}", whereClause_);
      _log.info("connectionSource_->{}", connectionSource_);
      _log.info("model_->{}", model_);
      _log.info("keyColumnNames_->{}", keyColumnNames_);
      _log.info("readColumnIdxs_->{}", readColumnIdxs_);

      if (readColumnIdxs_ != null)
         throw new NotImplementedException("readColumnIdxs_ not yet implemented");
      if ((model_ != null) && (keyColumnNames_ != null))
         throw new RuntimeException(String.format("does not allow both %s and %s params",
            "model_", "keyColumnNames_"));
      _tableName = tableName_;
      _whereClause = whereClause_;
      _connectionSource = connectionSource_;
      _table = this.getTable(_tableName, _connectionSource);
      _log.info("table->{}", _table);
      DKValidate.notNull(_tableName, _connectionSource);
      if (_table == null)
         throw new RuntimeException(String.format("couldn't find table named->%s",
            _tableName));
      _keyColumnNames = keyColumnNames_;
      _model = this.getModel(model_, _keyColumnNames, _table);
      _log.info("_model->{}", _model);
      DKValidate.notNull(_model);
      this.validateModel(_model, _table);
   }

   public String getTableName() {
      return _tableName;
   }

   @Override
   public void close(DKContext context_) throws IOException {
      this.ensureOpen();
      DKSqlUtil.close(_resultSet);
      DKSqlUtil.close(_connection);
      _resultSet = null;
      _connection = null;
      _isOpen = false;
   }

   @Override
   public void open(DKContext context_) throws IOException {
      this.ensureNotOpen();
      try {
         _readColumnNames = _model.getColumnNames();
         _connection = _connectionSource.getConnection();
         _resultSet = this.createResultSet();
         if (_isDebug)
            _log.debug("_resultSet->{}", _resultSet);
         _isOpen = true;
         _lastIndex = -1;
      }
      catch (Exception e_) {
         _log.error(null, e_);
         _connection = null;
         _resultSet = null;
         _isOpen = false;
         throw new IOException(e_);
      }
   }

   private DKDBTable getTable() throws SQLException {
      return _table;
   }

   public DKTableModel getModel() {
      return _model;
   }

   public DKDBConnectionSource getConnectionSource() {
      return _connectionSource;
   }

   public Object[] getNextRow() throws IOException {
      try {
         this.ensureOpen();
         if (!_resultSet.next())
            return null;
         _lastIndex++;
         return DKSqlUtil.readRow(_resultSet, _readColumnNames);
      }
      catch (Exception e_) {
         throw new IOException(e_);
      }
   }

   @Override
   public Kind getKind() {
      return Kind.DB;
   }

   @Override
   public long getLastIndex() {
      return _lastIndex;
   }

   @Override
   public URI getURI() throws IOException {
      try {
         return new URI(_connectionSource.getConnectionInfo().getJDBCUrl());
      }
      catch (URISyntaxException e_) {
         throw new IOException(e_);
      }
   }

   private ResultSet createResultSet() throws SQLException {
      return DKSqlUtil.executeQuery(this.generateSelectString(), _connection);
   }

   private DKDBTable getTable(String tableName_, DKDBConnectionSource connectionSource_)
      throws SQLException {
      _log.debug("tableName_->{}", tableName_);
      if ((tableName_ == null) || (connectionSource_ == null))
         return null;
      DKDBTableDataAccess tableDataAccess = new DKDBTableDataAccess(connectionSource_);
      DKDBTable dbTable = tableDataAccess.getTable(tableName_);
      return dbTable;
   }

   private String generateSelectString() throws SQLException {
      StringBuilder builder = new StringBuilder();
      DKDBTable table = this.getTable();
      builder.append(String.format("SELECT * FROM %s.%s", table.getSchema(),
         table.getTableName()));
      if (_whereClause != null)
         builder.append("\n" + _whereClause);
      String orderBy = this.generateOrderByClause();
      if (orderBy != null)
         builder.append("\n" + orderBy);
      return builder.toString();
   }

   private String generateOrderByClause() throws SQLException {
      DKDBTable table = this.getTable();
      DKDBPrimaryKey primaryKey = table.getPrimaryKey();
      if (primaryKey == null)
         return null;
      String[] primaryKeyColumnNames = primaryKey.getColumnNames();
      StringBuilder builder = new StringBuilder();
      builder.append("ORDER BY ");
      for (int i = 0; i < primaryKeyColumnNames.length; i++) {
         builder.append(primaryKeyColumnNames[i]);
         if (i < primaryKeyColumnNames.length - 1)
            builder.append(", ");
      }
      return builder.toString();
   }

   public DKTableModel getModel(DKTableModel model_, String[] keyColumnNames_,
                                DKDBTable dbTable_) {
      if (model_ != null)
         return model_;
      try {
         return DKTableModelUtil.createDefaultTableModel(dbTable_, keyColumnNames_);
      }
      catch (Exception e_) {
         throw new RuntimeException(e_);
      }
   }

   private void validateModel(DKTableModel model_, DKDBTable table_) {
      if (_isValidated)
         return;
      if (model_ == null)
         throw new RuntimeException("missing model_");
      if (table_ == null)
         throw new RuntimeException("missing table_");
      DKColumnModel[] columnModels = model_.getColumns();
      if ((columnModels == null) || (columnModels.length == 0))
         return;
      for (DKColumnModel columnModel : columnModels) {
         if (!table_.containsColumn(columnModel.getName()))
            throw new IllegalArgumentException(String.format(
               "modelled column->%s does not exist in table->%s", columnModel, table_));
      }
      _isValidated = true;
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
