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

import javax.inject.Inject;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.horizontaldb.shard.ShardContext;
import org.horizontaldb.shard.hibernate.TenantContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.Transactional;

/*
 *  This class tells the current tenantId to Hibernate in order to open an appropriate Session.
 *  
 *  The ThreadLocal with the counter is here for the sake of recursive method calls - in case
 *  the annotated method makes a call to another annotated method (..and so on), it is ensured that
 *  upon returning from calls the Hibernate context is still holds a valid tenantId, up until
 *  the last call, when it is cleared up for the next thread.
 */

@Aspect
public class TenantContextEnricherAspect implements Ordered {
	@Value( "#{dbProperties['multiTenantConnectionProvider.anyDataSourceTenantId']}" )
	private String anyDataSourceTenantId;

	private TenantContext tenantContext;
	private int order;
	private ThreadLocal<Integer> frameCounter = new ThreadLocal<>( );

	@Inject
	public TenantContextEnricherAspect( TenantContext tenantContext ) {
		this.tenantContext = tenantContext;
	}

	@Override
	public int getOrder() {
		return order;
	}

	public void setOrder( int order ) {
		this.order = order;
	}

	@Around( value = "( within( org.horizontaldb..* ) ) && @annotation( transactional ) && args(shardContext, ..)", argNames = "transactional, shardContext" )
	public Object populateTenantContext( ProceedingJoinPoint pjp, Transactional transactional, ShardContext shardContext ) throws Throwable {
		increaseFrameCount( );

		tenantContext.setCurrentTenantId( shardContext.getClientId( ) );

		Object proceed = null;

		try {
			proceed = pjp.proceed( );
		} finally {
			decreaseFrameCount( );

			if ( isLastFrame( ) ) {
				tenantContext.setCurrentTenantId( null );

				frameCounter.set( null );
			}
		}

		return proceed;
	}

	private void increaseFrameCount() {
		Integer count = frameCounter.get( );

		if ( count == null ) {
			count = new Integer( 0 );
		} else {
			count = count.intValue( ) + 1;
		}

		frameCounter.set( count );
	}

	private void decreaseFrameCount() {
		Integer count = frameCounter.get( );

		if ( count != null ) {
			frameCounter.set( count.intValue( ) - 1 );
		}
	}

	private boolean isLastFrame() {
		Integer count = frameCounter.get( );

		return count < 0;
	}

}
