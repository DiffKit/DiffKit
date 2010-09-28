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


import java.sql.DriverManager;
import java.util.Properties;
import org.h2.tools.Server 


import groovy.util.GroovyTestCase;


/**
 * @author jpanico
 */
public class TestH2Embedded extends GroovyTestCase {
   
   public void testH2(){
      
      // start the TCP Server
      //      Server server = Server.createTcpServer(null).start()
      //      Thread.currentThread().sleep(10000)
      //      def driverName = 'org.h2.Driver'
      //      def jdbcUrl = 'jdbc:h2:file:./test'
      //      def username = 'test'
      //      def password = 'test'
      //      
      //      Class.forName(driverName);
      //      
      //      Properties properties = new Properties();
      //      properties.put('user', username);
      //      properties.put('password', password);
      //      DriverManager.getConnection(jdbcUrl, properties);
   }
}
