package org.diffkit.db.tst;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import org.diffkit.db.DKDBConnectionInfo;
import org.diffkit.db.DKDBDatabase;
import org.diffkit.db.DKDBFlavor;

public class TestDBConnection {

   public static void main(String[] args_) {
      try {
         // DKDBConnectionInfo connectionInfo = new DKDBConnectionInfo("test",
         // DKDBConnectionInfo.Kind.H2, "mem:test", null, null, "test", "test");
         DKDBConnectionInfo connectionInfo = new DKDBConnectionInfo("db204",
            DKDBFlavor.DB2, "LRMDB", "db204.95g.jec.us.ml.com",
            (Long) 50000L, "analytic", "merrill");
         // 'jdbc:db2://unit_host:unit_port/unit_server'
         testConnectionInfo(connectionInfo);
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }

   private static void testConnectionInfo(DKDBConnectionInfo connectionInfo_)
      throws Exception {
      System.out.println("connectionInfo->" + connectionInfo_);
      System.out.println("JDBCUrl->" + connectionInfo_.getJDBCUrl());
      System.out.println("driverName->" + connectionInfo_.getDriverName());
      System.out.println("connectionInfo->" + connectionInfo_);
      DKDBDatabase connectionSource = new DKDBDatabase(connectionInfo_);
      Connection connection = connectionSource.getConnection();
      System.out.println("connection->" + connection);
      DatabaseMetaData meta = connection.getMetaData();
      System.out.println("meta->" + meta);
   }
}
