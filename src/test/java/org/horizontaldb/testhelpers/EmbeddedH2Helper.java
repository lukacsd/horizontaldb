/*
 * Copyright 2014 David Lukacs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.horizontaldb.testhelpers;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.codec.digest.DigestUtils;
import org.h2.tools.Server;
import org.horizontaldb.testhelpers.TestUserHelper.TestUser;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class EmbeddedH2Helper {
	private static String BASE_DIR = "." + File.separator + "target" + File.separator + "test-classes" + File.separator;
	private static Server server;

	@BeforeClass
	public static void startEmbeddedH2Server() throws SQLException {
		server = Server.createTcpServer( "-tcp", "-tcpAllowOthers", "-tcpPort", "8099", "-baseDir", BASE_DIR ).start( );

		primeDatabaseAndTablesForTestUsers( );
	}

	@AfterClass
	public static void stopEmbeddedH2Server() {
		server.stop( );
	}

	private static void primeDatabaseAndTablesForTestUsers() throws SQLException {
		Connection conn = null;

		for ( TestUser user : TestUser.values( ) ) {
			try {
				conn = DriverManager.getConnection( String.format( "jdbc:h2:tcp://127.0.0.1:8099/%s", user ), user.name( ),
						getTenantPassword( user.name( ) ) );

				Statement stat = conn.createStatement( );

				stat.execute( "BEGIN;" );
				stat.execute( "CREATE SEQUENCE department_id_seq;" );
				stat.execute( "CREATE TABLE department( id BIGINT DEFAULT department_id_seq.nextval PRIMARY KEY, name VARCHAR );" );
				stat.execute( "CREATE SEQUENCE person_id_seq;" );
				stat.execute( "CREATE TABLE person( id BIGINT DEFAULT person_id_seq.nextval PRIMARY KEY, name VARCHAR, department_id BIGINT );" );
				stat.execute( "COMMIT;" );

				stat.close( );
			} finally {
				File db = new File( BASE_DIR + user + ".mv.db" );
				db.deleteOnExit( );

				if ( conn != null ) {
					conn.close( );
				}
			}
		}
	}

	private static String getTenantPassword( String tenantId ) {
		String retval = DigestUtils.md5Hex( tenantId );

		return retval;
	}

}
