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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKValidate;
import org.diffkit.util.DKSqlUtil;

/**
 * @author jpanico
 */
public class DKDBH2Loader implements DKDBTableLoader {

   private final DKDBDatabase _connectionSource;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   public DKDBH2Loader(DKDBDatabase connectionSource_) {
      _connectionSource = connectionSource_;
      DKValidate.notNull(_connectionSource);
   }

   /**
    * @return true if the load succeeded
    * @throws IOException
    */
   public boolean load(DKDBTable target_, File csvFile_) throws IOException, SQLException {
      DKValidate.notNull(target_, csvFile_);
      if (!csvFile_.canRead())
         throw new IOException(String.format("can't read csvFile_->%s", csvFile_));

      String sqlString = String.format("INSERT INTO %s (SELECT * FROM CSVREAD('%s') )",
         target_.getSchemaQualifiedTableName(), csvFile_.getAbsolutePath());
      Connection connection = _connectionSource.getConnection();
      _log.debug("sqlString->{}", sqlString);
      _log.debug("connection->{}", connection);
      boolean success = DKSqlUtil.executeUpdate(sqlString, connection);
      DKSqlUtil.close(connection);
      return success;
   }
}
