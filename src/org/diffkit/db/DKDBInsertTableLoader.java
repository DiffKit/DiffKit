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
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;

/**
 * @author jpanico
 */
public class DKDBInsertTableLoader implements DKDBTableLoader {

   private final DKDBDatabase _connectionSource;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   public DKDBInsertTableLoader(DKDBDatabase connectionSource_) {
      _connectionSource = connectionSource_;
      DKValidate.notNull(_connectionSource);
   }

   /**
    * @return true if the load succeeded
    * @throws IOException
    */
   public boolean load(DKDBTable target_, File csvFile_) throws IOException, SQLException {
      _log.debug("target_->{}", target_);
      _log.debug("csvFile_->{}", csvFile_);
      DKValidate.notNull(target_, csvFile_);
      if (!csvFile_.canRead())
         throw new IOException(String.format("can't read csvFile_->%s", csvFile_));

      LineNumberReader reader = new LineNumberReader(new BufferedReader(new FileReader(
         csvFile_)));
      Connection connection = _connectionSource.getConnection();
      _log.debug("connection->{}", connection);
      if (connection == null)

         connection.setAutoCommit(true);

      String line = null;
      throw new NotImplementedException();
      // List<String> updateStatements = new ArrayList<String>(LOAD_BATCH_SIZE);
      // for (long i = 1; (line = reader.readLine()) != null; i++) {
      // if (debugEnabled) {
      // _log.debug("line: " + line);
      // }
      // String[] values = line.split(fieldSeparator_, -1);
      // String insertStatementString =
      // MLSqlFormatter.formatInsertStatement(tableInfo_,
      // values, ignoreColumns_);
      // updateStatements.add(insertStatementString);
      // _log.debug("insertStatementString: " + insertStatementString);
      // if (i % LOAD_BATCH_SIZE == 0) {
      // MLSqlUtil.executeBatchUpdate(updateStatements, connection_);
      // _log.debug("inserted " + i + " rows");
      // updateStatements.clear();
      // }
      // }
      // long updates = MLSqlUtil.executeBatchUpdate(updateStatements,
      // connection_);
      // _log.debug("updates: " + updates);
      // reader.close();
      // return success;
   }
}
