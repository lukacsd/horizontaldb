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

package org.horizontaldb.shard.hibernate.advise;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.aspectj.lang.ProceedingJoinPoint;
import org.easymock.EasyMock;
import org.horizontaldb.shard.ShardContext;
import org.horizontaldb.shard.hibernate.TenantContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

public class TenantContextEnricherAspectTest {
	private TenantContext tenantContext;

	@Before
	public void setUp() {
		tenantContext = EasyMock.createMock( TenantContext.class );
	}

	@After
	public void tearDown() {
		tenantContext = null;
	}

	@Test
	public void shouldSetAndUnsetClientId() throws Throwable {
		TenantContextEnricherAspect enricher = new TenantContextEnricherAspect( tenantContext );

		ShardContext shardContext = new ShardContext( "testClient" );
		Transactional transactional = null;
		ProceedingJoinPoint pjp = EasyMock.createMock( ProceedingJoinPoint.class );

		expect( pjp.proceed( ) ).andReturn( null );
		tenantContext.setCurrentTenantId( "testClient" );
		tenantContext.setCurrentTenantId( null );

		replay( tenantContext, pjp );

		enricher.populateTenantContext( pjp, transactional, shardContext );

		verify( tenantContext, pjp );
	}

	@Test
	public void shouldConsecutivelySetAndUnsetClientId() throws Throwable {
		TenantContextEnricherAspect enricher = new TenantContextEnricherAspect( tenantContext );

		ShardContext shardContext = new ShardContext( "testClient" );
		Transactional transactional = null;
		ProceedingJoinPoint pjp = EasyMock.createMock( ProceedingJoinPoint.class );

		expect( pjp.proceed( ) ).andReturn( null ).times( 3 );
		tenantContext.setCurrentTenantId( "testClient" );
		tenantContext.setCurrentTenantId( null );
		tenantContext.setCurrentTenantId( "testClient" );
		tenantContext.setCurrentTenantId( null );
		tenantContext.setCurrentTenantId( "testClient" );
		tenantContext.setCurrentTenantId( null );

		replay( tenantContext, pjp );

		enricher.populateTenantContext( pjp, transactional, shardContext );
		enricher.populateTenantContext( pjp, transactional, shardContext );
		enricher.populateTenantContext( pjp, transactional, shardContext );

		verify( tenantContext, pjp );
	}

	@Test( expected = IllegalStateException.class )
	public void shouldSetAndUnsetClientIdWhenAdvisedMethodFails() throws Throwable {
		TenantContextEnricherAspect enricher = new TenantContextEnricherAspect( tenantContext );

		ShardContext shardContext = new ShardContext( "testClient" );
		Transactional transactional = null;
		ProceedingJoinPoint pjp = EasyMock.createMock( ProceedingJoinPoint.class );

		expect( pjp.proceed( ) ).andThrow( new IllegalStateException( ) );
		tenantContext.setCurrentTenantId( "testClient" );
		tenantContext.setCurrentTenantId( null );

		replay( tenantContext, pjp );

		enricher.populateTenantContext( pjp, transactional, shardContext );

		verify( tenantContext, pjp );
	}

}
