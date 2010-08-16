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
package org.diffkit.db.tst

import java.io.File;
import java.sql.Connection 

import org.diffkit.db.DKDBConnectionSource;
import org.diffkit.db.DKDBH2Loader 
import org.diffkit.db.DKDBTable;
import org.diffkit.db.DKDBTableLoader 
import org.diffkit.util.DKResourceUtil;
import org.diffkit.util.DKSqlUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext 
import org.springframework.context.support.ClassPathXmlApplicationContext 
import org.springframework.context.support.FileSystemXmlApplicationContext 


/**
 * @author jpanico
 */
public class DBTestSetup {
   
   private static final Logger _log = LoggerFactory.getLogger(DBTestSetup.class)
   
   public static void setupDB(File dbSetupFile_, String lhsSourcePath_, String rhsSourcePath_) {
      File lhsSourceFile = DKResourceUtil.findResourceAsFile(lhsSourcePath_)
      File rhsSourceFile = DKResourceUtil.findResourceAsFile(rhsSourcePath_)
      setupDB( dbSetupFile_, lhsSourceFile, rhsSourceFile)
   }
   
   /**
    * dbSetupFile_ can be a FS file path, or a classpath resource path
    */
   public static void setupDB(File dbSetupFile_, File lhsSourceFile_, File rhsSourceFile_) {
      if(!dbSetupFile_)
         return 
      ApplicationContext context = null
      _log.debug("dbSetupFile_->{}",dbSetupFile_.canonicalPath)
      if(dbSetupFile_.exists())
         context = new FileSystemXmlApplicationContext('file:'+dbSetupFile_.absolutePath)
      else 
         context = new ClassPathXmlApplicationContext(dbSetupFile_.path)
      assert context
      
      def connectionSource = context.getBean('connectionSource')
      _log.debug("connectionSource->{}",connectionSource)
      if(!connectionSource)
         throw new RuntimeException("no 'connectionSource' bean in dbsetup file->${dbSetupFile_}")
      def beanName = 'lhs.table'
      if(context.containsBean(beanName)) {
         def lhsTable = context.getBean(beanName)
         _log.debug("lhsTable->{}",lhsTable)
         setupDBTable( lhsTable, lhsSourceFile_, connectionSource)
      }
      beanName = 'rhs.table'
      if(context.containsBean(beanName)) {
         def rhsTable = context.getBean(beanName)
         _log.debug("rhsTable->{}",rhsTable)
         setupDBTable( rhsTable, rhsSourceFile_, connectionSource)
      }
   }
   
   private static void setupDBTable(DKDBTable table_, File dataFile_, DKDBConnectionSource connectionSource_){
      if(!table_)
         return
      
      Connection connection = connectionSource_.connection
      _log.debug("connection->{}",connection)
      DKDBTable.createTable( table_, connection)
      DKSqlUtil.close(connection)
      DKDBTableLoader loader = new DKDBH2Loader(connectionSource_)
      _log.debug("loader->{}",loader)
      loader.load(table_, dataFile_)
   }
}
