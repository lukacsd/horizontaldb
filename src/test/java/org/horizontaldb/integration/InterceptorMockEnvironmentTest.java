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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Inject;

import org.easymock.EasyMock;
import org.horizontaldb.example.model.dao.DepartmentDao;
import org.horizontaldb.example.model.dao.DepartmentDaoImpl;
import org.horizontaldb.example.model.dao.PersonDao;
import org.horizontaldb.shard.ConversationRegistry;
import org.horizontaldb.shard.ShardBeanEnricher;
import org.horizontaldb.shard.ShardContext;
import org.horizontaldb.shard.hibernate.DataSourceResource;
import org.horizontaldb.shard.hibernate.TenantContext;
import org.horizontaldb.shard.resolver.ShardBeanResolver;
import org.horizontaldb.testhelpers.ConversationRegistryMockProxy;
import org.horizontaldb.testhelpers.DataSourceFactoryMockProxy;
import org.horizontaldb.testhelpers.ShardBeanEnricherMockProxy;
import org.horizontaldb.testhelpers.ShardBeanResolverMockProxy;
import org.horizontaldb.testhelpers.TenantContextMockProxy;
import org.horizontaldb.testhelpers.TestServiceOne;
import org.horizontaldb.testhelpers.TestUserHelper;
import org.horizontaldb.testhelpers.TestUserHelper.TestUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/*
 * The purpose of this test is to prove the correctness of the machinery in isolation. The correctness is
 * expressed in EasyMock DSL.
 * 
 * `OneLevelShardedCall' - the intention behind the wording is to clearly set the scenario, where the
 *  service call does not make further sharded calls which would result in embedded context's.
 *  
 * `MultiLevelShardedCall' - in this case the service do call other services with new context's.
 */
@ContextConfiguration( locations = { "classpath:/META-INF/spring/spring-context.xml", "/interceptor-testspring-context.xml" } )
@RunWith( SpringJUnit4ClassRunner.class )
@DirtiesContext( classMode = ClassMode.AFTER_EACH_TEST_METHOD )
public class InterceptorMockEnvironmentTest {
	@Inject
	private TestUserHelper testUserHelper;
	@Inject
	private ConversationRegistryMockProxy conversationRegistryMockProxy;
	@Inject
	private TenantContextMockProxy tenantContextMockProxy;
	@Inject
	private DataSourceFactoryMockProxy dataSourceFactoryMockProxy;
	@Inject
	private ShardBeanResolverMockProxy shardBeanResolverMockProxy;
	@Inject
	private ShardBeanEnricherMockProxy shardBeanEnricherMockProxy;
	@Inject
	private TestServiceOne testService;

	@Test
	public void shouldValidateOneLevelShardedCall() throws SQLException {
		ConversationRegistry mockRegistry = EasyMock.createMock( ConversationRegistry.class );
		TenantContext mockTenantContext = EasyMock.createMock( TenantContext.class );
		org.apache.tomcat.jdbc.pool.DataSource mockDataSource = EasyMock.createMock( org.apache.tomcat.jdbc.pool.DataSource.class );
		DataSourceResource mockDataSourceResource = new DataSourceResource( mockDataSource );
		ShardBeanResolver mockShardBeanResolver = EasyMock.createMock( ShardBeanResolver.class );
		ShardBeanEnricher mockShardBeanEnricher = EasyMock.createMock( ShardBeanEnricher.class );
		Connection mockConnection = EasyMock.createMock( Connection.class );
		PreparedStatement mockStatement = EasyMock.createMock( PreparedStatement.class );
		ResultSet mockResultset = EasyMock.createMock( ResultSet.class );

		conversationRegistryMockProxy.setMockRegistry( mockRegistry );
		tenantContextMockProxy.setMockTenantContext( mockTenantContext );
		dataSourceFactoryMockProxy.setMockDataSourceResource( mockDataSourceResource );
		shardBeanResolverMockProxy.setMockResolver( mockShardBeanResolver );
		shardBeanEnricherMockProxy.setMockEnricher( mockShardBeanEnricher );

		// This is the protocol that the interceptors should follow during a sharded call
		mockRegistry.startConversation( testUserHelper.getJoeToken( ) );
		expect( mockRegistry.hasConversation( TestUser.JOE.name( ) ) ).andReturn( true );
		expect( mockTenantContext.resolveCurrentTenantIdentifier( ) ).andReturn( TestUser.JOE.name( ) );
		mockRegistry.addResource( TestUser.JOE.name( ), mockDataSourceResource );
		mockRegistry.addResource( same( TestUser.JOE.name( ) ), anyObject( DepartmentDaoImpl.class ) );
		expect( mockShardBeanResolver.getBean( same( DepartmentDao.class ), anyObject( ShardContext.class ) ) ).andReturn( null );
		mockShardBeanEnricher.setup( anyObject( DepartmentDaoImpl.class ), anyObject( ShardContext.class ) );
		mockShardBeanEnricher.tearDown( anyObject( DepartmentDaoImpl.class ), anyObject( ShardContext.class ) );
		mockDataSource.close( true );
		mockRegistry.teardownConversation( testUserHelper.getJoeToken( ) );
		// end protocol

		// This is the flow of a Hibernate transaction which is irrelevant, but had to be defined because of the
		// mocked dataSource.
		expect( mockDataSource.getConnection( ) ).andReturn( mockConnection );
		mockConnection.setReadOnly( true );
		expect( mockConnection.getAutoCommit( ) ).andReturn( false );
		expect( mockConnection.prepareStatement( anyObject( String.class ) ) ).andReturn( mockStatement );
		expect( mockStatement.executeQuery( ) ).andReturn( mockResultset );
		expect( mockStatement.getWarnings( ) ).andReturn( null );
		mockStatement.clearWarnings( );
		expect( mockStatement.getMaxRows( ) ).andReturn( 0 );
		expect( mockStatement.getQueryTimeout( ) ).andReturn( 0 );
		expect( mockResultset.next( ) ).andReturn( true );
		expect( mockResultset.next( ) ).andReturn( false );
		expect( mockResultset.getLong( anyObject( String.class ) ) ).andReturn( 0l );
		expect( mockResultset.wasNull( ) ).andReturn( false );
		mockResultset.close( );
		mockStatement.close( );
		mockConnection.commit( );
		// end Hibernate transaction

		replay( mockRegistry, mockTenantContext, mockShardBeanResolver, mockShardBeanEnricher, mockDataSource, mockConnection,
				mockStatement, mockResultset );

		try {
			ShardContext context = new ShardContext( TestUser.JOE.name( ) );

			testService.authenticate( testUserHelper.getJoeToken( ) );

			Long actualCount = testService.getCountOfDepartments( context );

			assertEquals( 0, actualCount.longValue( ) );
		} finally {
			testService.logoff( testUserHelper.getJoeToken( ) );
		}

		verify( mockRegistry, mockTenantContext, mockShardBeanResolver, mockShardBeanEnricher, mockDataSource, mockConnection,
				mockStatement, mockResultset );
	}

	@Test
	public void shouldValidateMultiLevelShardedCalls() throws SQLException {
		ConversationRegistry mockRegistry = EasyMock.createMock( ConversationRegistry.class );
		TenantContext mockTenantContext = EasyMock.createMock( TenantContext.class );
		org.apache.tomcat.jdbc.pool.DataSource mockDataSource = EasyMock.createMock( org.apache.tomcat.jdbc.pool.DataSource.class );
		DataSourceResource mockDataSourceResource = new DataSourceResource( mockDataSource );
		ShardBeanResolver mockShardBeanResolver = EasyMock.createMock( ShardBeanResolver.class );
		ShardBeanEnricher mockShardBeanEnricher = EasyMock.createMock( ShardBeanEnricher.class );
		Connection mockConnection = EasyMock.createMock( Connection.class );
		PreparedStatement mockStatement = EasyMock.createMock( PreparedStatement.class );
		ResultSet mockResultset = EasyMock.createMock( ResultSet.class );

		conversationRegistryMockProxy.setMockRegistry( mockRegistry );
		tenantContextMockProxy.setMockTenantContext( mockTenantContext );
		dataSourceFactoryMockProxy.setMockDataSourceResource( mockDataSourceResource );
		shardBeanResolverMockProxy.setMockResolver( mockShardBeanResolver );
		shardBeanEnricherMockProxy.setMockEnricher( mockShardBeanEnricher );

		// This is the protocol that the interceptors should follow during a sharded call
		mockRegistry.startConversation( testUserHelper.getJoeToken( ) );
		expect( mockRegistry.hasConversation( TestUser.JOE.name( ) ) ).andReturn( true );
		expect( mockTenantContext.resolveCurrentTenantIdentifier( ) ).andReturn( TestUser.JOE.name( ) );
		mockRegistry.addResource( TestUser.JOE.name( ), mockDataSourceResource );

		// resolve Dao for TestServiceTwo
		expect( mockShardBeanResolver.getBean( same( PersonDao.class ), anyObject( ShardContext.class ) ) ).andReturn( null );
		mockRegistry.addResource( same( TestUser.JOE.name( ) ), anyObject( PersonDao.class ) );
		mockShardBeanEnricher.setup( anyObject( PersonDao.class ), anyObject( ShardContext.class ) );
		mockShardBeanEnricher.tearDown( anyObject( PersonDao.class ), anyObject( ShardContext.class ) );

		// Hibernate transaction flow
		expect( mockDataSource.getConnection( ) ).andReturn( mockConnection );
		mockConnection.setReadOnly( true );
		expect( mockConnection.getAutoCommit( ) ).andReturn( false );
		expect( mockConnection.prepareStatement( anyObject( String.class ) ) ).andReturn( mockStatement );
		expect( mockStatement.executeQuery( ) ).andReturn( mockResultset );
		expect( mockStatement.getWarnings( ) ).andReturn( null );
		mockStatement.clearWarnings( );
		expect( mockStatement.getMaxRows( ) ).andReturn( 0 );
		expect( mockStatement.getQueryTimeout( ) ).andReturn( 0 );
		expect( mockResultset.next( ) ).andReturn( true );
		expect( mockResultset.next( ) ).andReturn( false );
		expect( mockResultset.getLong( anyObject( String.class ) ) ).andReturn( 0l );
		expect( mockResultset.wasNull( ) ).andReturn( false );
		expect( mockResultset.getLong( anyObject( String.class ) ) ).andReturn( 0l );
		expect( mockResultset.wasNull( ) ).andReturn( true );
		expect( mockResultset.getString( anyObject( String.class ) ) ).andReturn( "mockPerson" );
		expect( mockResultset.wasNull( ) ).andReturn( false );
		mockResultset.close( );
		mockStatement.close( );
		mockConnection.commit( );
		// end Hibernate transaction

		// resolve Dao for TestServiceThree
		expect( mockRegistry.hasConversation( TestUser.JOE.name( ) ) ).andReturn( true );
		expect( mockShardBeanResolver.getBean( same( DepartmentDao.class ), anyObject( ShardContext.class ) ) ).andReturn( null );
		mockRegistry.addResource( same( TestUser.JOE.name( ) ), anyObject( DepartmentDaoImpl.class ) );
		mockShardBeanEnricher.setup( anyObject( DepartmentDaoImpl.class ), anyObject( ShardContext.class ) );
		mockShardBeanEnricher.tearDown( anyObject( DepartmentDaoImpl.class ), anyObject( ShardContext.class ) );

		// Hibernate transaction flow
		expect( mockConnection.prepareStatement( anyObject( String.class ) ) ).andReturn( mockStatement );
		expect( mockStatement.executeQuery( ) ).andReturn( mockResultset );
		expect( mockStatement.getWarnings( ) ).andReturn( null );
		mockStatement.clearWarnings( );
		expect( mockStatement.getMaxRows( ) ).andReturn( 0 );
		expect( mockStatement.getQueryTimeout( ) ).andReturn( 0 );
		expect( mockResultset.next( ) ).andReturn( true );
		expect( mockResultset.next( ) ).andReturn( false );
		expect( mockResultset.getLong( anyObject( String.class ) ) ).andReturn( 0l );
		expect( mockResultset.wasNull( ) ).andReturn( false );
		expect( mockResultset.getString( anyObject( String.class ) ) ).andReturn( "mockDepartment" );
		expect( mockResultset.wasNull( ) ).andReturn( false );
		mockResultset.close( );
		mockStatement.close( );
		// end Hibernate transaction

		// cleanup after service calls
		mockDataSource.close( true );
		mockRegistry.teardownConversation( testUserHelper.getJoeToken( ) );

		replay( mockRegistry, mockTenantContext, mockShardBeanResolver, mockShardBeanEnricher, mockDataSource, mockConnection,
				mockStatement, mockResultset );

		try {
			testService.authenticate( testUserHelper.getJoeToken( ) );

			testService.callNestedServiceChain( TestUser.JOE.name( ) );
		} finally {
			testService.logoff( testUserHelper.getJoeToken( ) );
		}

		verify( mockRegistry, mockTenantContext, mockShardBeanResolver, mockShardBeanEnricher, mockDataSource, mockConnection,
				mockStatement, mockResultset );
	}

}
