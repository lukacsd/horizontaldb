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

package org.horizontaldb.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Arrays;

import javax.inject.Inject;

import org.horizontaldb.shard.ShardContext;
import org.horizontaldb.testhelpers.EmbeddedH2Helper;
import org.horizontaldb.testhelpers.TestServiceOne;
import org.horizontaldb.testhelpers.TestUserHelper;
import org.horizontaldb.testhelpers.TestUserHelper.TestDepartment;
import org.horizontaldb.testhelpers.TestUserHelper.TestPerson;
import org.horizontaldb.testhelpers.TestUserHelper.TestUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/*
 * The purpose of this test is to prove that the mechanics of a sharded call is working in a full-blown Spring context
 * backed by a H2 database.
 * 
 * The database instance is an H2 tcp server which is started up before the testcases are about to be executed,
 * and shut down once all test finished. The database files are removed by the jvm on exit.
 */
@ContextConfiguration( locations = { "classpath:/META-INF/spring/spring-context.xml", "/integration-testspring-context.xml" } )
@RunWith( SpringJUnit4ClassRunner.class )
public class DatabaseIntegrationTest extends EmbeddedH2Helper {
	@Inject
	private TestUserHelper testUserHelper;
	@Inject
	private TestServiceOne testService;

	@Test( expected = IllegalStateException.class )
	public void shouldFailUnauthenticatedServiceCall() {
		ShardContext context = new ShardContext( TestUser.JOE.name( ) );

		testService.getCountOfPersonsByDepartment( context, TestDepartment.DEVELOPMENT.name( ) );
	}

	@Test( expected = IllegalStateException.class )
	public void shouldNotAllowChangingIdentityForAuthenticatedCall() {
		ShardContext context = new ShardContext( TestUser.JANE.name( ) );

		try {
			testService.authenticate( testUserHelper.getJoeToken( ) );

			Long actualCount = testService.getCountOfPersonsByDepartment( context, TestDepartment.DEVELOPMENT.name( ) );

			assertEquals( 0, actualCount.longValue( ) );
		} finally {
			testService.logoff( testUserHelper.getJoeToken( ) );
		}
	}

	@Test
	public void shouldSucceedAuthenticatedServiceCall() {
		ShardContext context = new ShardContext( TestUser.JOE.name( ) );

		try {
			testService.authenticate( testUserHelper.getJoeToken( ) );

			Long actualCount = testService.getCountOfDepartments( context );

			assertEquals( 0, actualCount.longValue( ) );
		} finally {
			testService.logoff( testUserHelper.getJoeToken( ) );
		}
	}

	@Test
	public void shouldSucceedPersistOperationForClientOne() {
		ShardContext context = new ShardContext( TestUser.JOE.name( ) );

		try {
			testService.authenticate( testUserHelper.getJoeToken( ) );

			testService.createDepartment( context, TestDepartment.DEVELOPMENT.name( ) );
			testService.addPersonToDepartment( context, TestPerson.DUDE.getName( ), TestPerson.DUDE.getDepartmentName( ) );
			testService.addPersonToDepartment( context, TestPerson.STEVE.getName( ), TestPerson.STEVE.getDepartmentName( ) );

			testService.createDepartment( context, TestDepartment.ACCOUNTING.name( ) );
			testService.addPersonToDepartment( context, TestPerson.BARBARA.getName( ), TestPerson.BARBARA.getDepartmentName( ) );

			assertEquals( Arrays.asList( TestPerson.DUDE.getName( ), TestPerson.STEVE.getName( ) ),
					testService.getPersonNamesByDepartment( context, TestDepartment.DEVELOPMENT.name( ) ) );
			assertEquals( Arrays.asList( TestPerson.BARBARA.getName( ) ),
					testService.getPersonNamesByDepartment( context, TestDepartment.ACCOUNTING.name( ) ) );
		} finally {
			testService.logoff( testUserHelper.getJoeToken( ) );
		}
	}

	@Test
	public void shouldSucceedPersistOperationForClientTwo() {
		ShardContext context = new ShardContext( TestUser.JANE.name( ) );

		try {
			testService.authenticate( testUserHelper.getJaneToken( ) );

			testService.createDepartment( context, TestDepartment.DEVELOPMENT.name( ) );
			testService.addPersonToDepartment( context, TestPerson.SAM.getName( ), TestPerson.SAM.getDepartmentName( ) );

			testService.createDepartment( context, TestDepartment.ACCOUNTING.name( ) );
			testService.addPersonToDepartment( context, TestPerson.NANCY.getName( ), TestPerson.NANCY.getDepartmentName( ) );
			testService.addPersonToDepartment( context, TestPerson.MARY.getName( ), TestPerson.MARY.getDepartmentName( ) );

			assertEquals( Arrays.asList( TestPerson.SAM.getName( ) ),
					testService.getPersonNamesByDepartment( context, TestDepartment.DEVELOPMENT.name( ) ) );
			assertEquals( Arrays.asList( TestPerson.NANCY.getName( ), TestPerson.MARY.getName( ) ),
					testService.getPersonNamesByDepartment( context, TestDepartment.ACCOUNTING.name( ) ) );
		} finally {
			testService.logoff( testUserHelper.getJaneToken( ) );
		}
	}

	@Test
	public void shouldReturnDifferentResultForDifferentClients() {
		ShardContext joeContext = new ShardContext( TestUser.JOE.name( ) );
		ShardContext janeContext = new ShardContext( TestUser.JANE.name( ) );

		try {
			testService.authenticate( testUserHelper.getJoeToken( ) );
			testService.authenticate( testUserHelper.getJaneToken( ) );

			Long joesDevelopers = testService.getCountOfPersonsByDepartment( joeContext, TestDepartment.DEVELOPMENT.name( ) );
			Long janesDevelopers = testService.getCountOfPersonsByDepartment( janeContext, TestDepartment.DEVELOPMENT.name( ) );
			Long joesAccountants = testService.getCountOfPersonsByDepartment( joeContext, TestDepartment.ACCOUNTING.name( ) );
			Long janesAccountants = testService.getCountOfPersonsByDepartment( janeContext, TestDepartment.ACCOUNTING.name( ) );

			assertNotEquals( joesDevelopers, janesDevelopers );
			assertNotEquals( joesAccountants, janesAccountants );
		} finally {
			testService.logoff( testUserHelper.getJoeToken( ) );
			testService.logoff( testUserHelper.getJaneToken( ) );
		}
	}

}
