package org.diffkit.db.tst

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

import org.diffkit.util.DKResourceUtil
import org.slf4j.LoggerFactory

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
   public static void setupDB(File dbSetupFile_, File[] connectionInfoFiles_, 
   File lhsSourceFile_, File rhsSourceFile_) {
      
      if(!dbSetupFile_)
         return 
      ApplicationContext context = null
      _log.debug("dbSetupFile_->{}",dbSetupFile_.canonicalPath)
      def configFiles = [dbSetupFile_]
      if(connectionInfoFiles_)
         configFiles.addAll(connectionInfoFiles_)
      if(dbSetupFile_.exists()) {
         String[] paths = configFiles.collect { 'file:'+ it.absolutePath }
         context = new FileSystemXmlApplicationContext(paths, false)
      }
      else {
         String[] paths = configFiles.collect { it.path }
         context = new ClassPathXmlApplicationContext(paths,false)
      }
      context.setClassLoader(DBTestSetup.class.getClassLoader())
      context.refresh()
      assert context
      
      def connectionSource = (!context.containsBean('connectionSource')?null:context.getBean('connectionSource'))
      def lhsConnectionSource = (!context.containsBean('lhsConnectionSource')?null:context.getBean('lhsConnectionSource'))
      def rhsConnectionSource = (!context.containsBean('rhsConnectionSource')?null:context.getBean('rhsConnectionSource'))
      _log.debug("connectionSource->{}",connectionSource)
      _log.debug("lhsConnectionSource->{}",lhsConnectionSource)
      _log.debug("rhsConnectionSource->{}",rhsConnectionSource)
      if(!connectionSource && ! (lhsConnectionSource && rhsConnectionSource))
         throw new RuntimeException("connectionSource bean(s) not in dbsetup file->${dbSetupFile_}")
      if(connectionSource && (lhsConnectionSource || rhsConnectionSource))
         throw new RuntimeException("cannot specify both 'connectionSource' and ('lhsConnectionSource' or 'rhsConnectionSource') bean in dbsetup file->${dbSetupFile_}; choose one or the other")
      
      if(!lhsConnectionSource)
         lhsConnectionSource = connectionSource
      if(!rhsConnectionSource)
         rhsConnectionSource = connectionSource
      _log.debug("lhsConnectionSource->{}",lhsConnectionSource)
      _log.debug("rhsConnectionSource->{}",rhsConnectionSource)
      
      def beanName = 'lhs.table'
      if(context.containsBean(beanName)) {
         def lhsTable = context.getBean(beanName)
         _log.debug("lhsTable->{}",lhsTable)
         setupDBTable( lhsTable, lhsSourceFile_, lhsConnectionSource)
      }
      beanName = 'rhs.table'
      if(context.containsBean(beanName)) {
         def rhsTable = context.getBean(beanName)
         _log.debug("rhsTable->{}",rhsTable)
         setupDBTable( rhsTable, rhsSourceFile_, rhsConnectionSource)
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
