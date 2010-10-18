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
package org.diffkit.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;
import org.diffkit.util.DKSqlUtil;

/**
 * @author jpanico
 */
public class DKDBInsertTableLoader implements DKDBTableLoader {
   private static final int LOAD_BATCH_SIZE = 1000;
   private final DKDBDatabase _database;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());
   private final boolean _debugEnabled = _log.isDebugEnabled();

   public DKDBInsertTableLoader(DKDBDatabase database_) {
      _database = database_;
      DKValidate.notNull(_database);
   }

   /**
    * @return true if the load succeeded
    * @throws IOException
    */
   public boolean load(DKDBTable table_, File csvFile_) throws IOException, SQLException {
      _log.debug("target_->{}", table_);
      _log.debug("csvFile_->{}", csvFile_);
      DKValidate.notNull(table_, csvFile_);
      if (!csvFile_.canRead())
         throw new IOException(String.format("can't read csvFile_->%s", csvFile_));
      if (!_database.tableExists(table_))
         throw new IOException(String.format("table_->%s does not exist in database->",
            table_, _database));
      Connection connection = _database.getConnection();
      _log.debug("connection->{}", connection);
      if (connection == null)
         throw new SQLException(String.format("can't get connection from database->",
            _database));

      connection.setAutoCommit(true);
      LineNumberReader reader = new LineNumberReader(new BufferedReader(new FileReader(
         csvFile_)));
      String[] columnNames = table_.getColumnNames();
      DKDBTypeInfo[] typeInfos = _database.getColumnTypeInfos(table_);
      String qualifiedTableName = table_.getSchemaQualifiedTableName();
      if (_debugEnabled) {
         _log.debug("columnNames->{}", Arrays.toString(columnNames));
         _log.debug("typeInfos->{}", Arrays.toString(typeInfos));
         _log.debug("qualifiedTableName->{}", qualifiedTableName);
      }
      String line = null;
      List<String> updateStatements = new ArrayList<String>(LOAD_BATCH_SIZE);
      // assume first line is header, throw away
      reader.readLine();
      for (long i = 1; (line = reader.readLine()) != null; i++) {
         String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
         if (_debugEnabled) {
            _log.debug("line: " + line);
            _log.debug("values: " + Arrays.toString(values));
         }
         if (!(values.length == columnNames.length))
            throw new RuntimeException(String.format(
               "number of values->%s does not match number of columns->%s",
               values.length, columnNames.length));
         String insertStatementString = _database.generateInsertDML(values, typeInfos,
            columnNames, qualifiedTableName);
         updateStatements.add(insertStatementString);
         _log.debug("insertStatementString: " + insertStatementString);
         if (i % LOAD_BATCH_SIZE == 0) {
            DKSqlUtil.executeBatchUpdate(updateStatements, connection);
            _log.debug("inserted " + i + " rows");
            updateStatements.clear();
         }
      }
      long updates = DKSqlUtil.executeBatchUpdate(updateStatements, connection);
      _log.debug("updates: " + updates);
      reader.close();
      return true;
   }
}
