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

package org.horizontaldb.shard.hibernate;

import java.util.ArrayDeque;
import java.util.Deque;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.Statistics;
import org.horizontaldb.example.model.dao.AbstractDao;
import org.horizontaldb.shard.ShardBeanEnricher;
import org.horizontaldb.shard.ShardContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.stereotype.Component;

/*
 *  This class sets the relevant session on each dao implementation. The `setup' and `tearDown'
 *  methods are called by the `ShardContextEnricher' aspect respectively, before and after executing
 *  the advised method.
 *  
 *  The details of each enrichment are maintained in a ThreadLocal for the sake of recursive method
 *  calls - in case the annotated method makes further calls to other annotated methods using the
 *  same dao type, it is ensured that upon returning from calls the dao instance will hold the same
 *  session as it was holding before, and cleared for the next time upon returning.
 *  
 *  Additionally, the Hibernate Second Level Cache Statistics are logged upon returning from an
 *  advised method.
 */

@Component( "abstractDaoEnricher" )
public class AbstractDaoEnricher implements ShardBeanEnricher {
	private static final Logger LOG = LoggerFactory.getLogger( AbstractDaoEnricher.class );

	private HibernateTransactionManager txManager;
	private ThreadLocal<Deque<ContextFrame>> contextStack = new ThreadLocal<>( );

	@Inject
	public AbstractDaoEnricher( HibernateTransactionManager txManager ) {
		this.txManager = txManager;
	}

	@Override
	public void setup( Object bean, ShardContext shardContext ) {
		pushContextFrame( shardContext );

		AbstractDao abstractDao = ( AbstractDao ) bean;
		abstractDao.setSession( peekContextSession( ) );
	}

	@Override
	public void tearDown( Object bean, ShardContext shardContext ) {
		AbstractDao abstractDao = ( AbstractDao ) bean;
		abstractDao.setSession( pollContextSession( ) );
	}

	private void pushContextFrame( ShardContext shardContext ) {
		ContextFrame currentFrame = contextStack( ).peek( );

		if ( currentFrame == null || !currentFrame.isSame( shardContext ) ) {
			contextStack( ).push( getNewFrame( shardContext ) );
		} else {
			currentFrame.increaseUsageCount( );
		}
	}

	private ContextFrame getNewFrame( ShardContext shardContext ) {
		SessionFactory sessionFactory = txManager.getSessionFactory( );

		Session session = sessionFactory.getCurrentSession( );

		return new ContextFrame( shardContext, session );
	}

	private Session peekContextSession() {
		return contextStack( ).peek( ).getSession( );
	}

	/*
	 * Method responsibilities:
	 *   - return session from current frame unless it is spent
	 *   - in case current frame is spent, return session from next frame if there is any
	 *   - otherwise return nothing
	 */
	private Session pollContextSession() {
		Session retval = null;

		ContextFrame currentFrame = contextStack( ).peek( );
		currentFrame.decreaseUsageCount( );

		if ( currentFrame.isSpent( ) ) {
			logSlcStats( currentFrame.getSession( ), currentFrame.getShardContext( ).getClientId( ) );

			contextStack( ).poll( );

			currentFrame = contextStack( ).peek( );
		}

		if ( currentFrame != null ) {
			retval = currentFrame.getSession( );
		}

		return retval;
	}

	private void logSlcStats( Session session, String tenantId ) {
		if ( LOG.isTraceEnabled( ) && session != null ) {
			Statistics statistics = session.getSessionFactory( ).getStatistics( );

			if ( statistics != null && statistics.isStatisticsEnabled( ) ) {
				String[ ] regions = statistics.getSecondLevelCacheRegionNames( );

				for ( String region : regions ) {
					SecondLevelCacheStatistics stat = statistics.getSecondLevelCacheStatistics( region );

					LOG.trace( String.format(
							"secondLevelCacheStatistics.%s.%s=hits[%s], misses[%s], puts[%s], memCount[%s], memSize[%s], diskCount[%s]",
							tenantId, region, stat.getHitCount( ), stat.getMissCount( ), stat.getPutCount( ),
							stat.getElementCountInMemory( ), stat.getSizeInMemory( ), stat.getElementCountOnDisk( ) ) );
				}
			}
		}
	}

	private Deque<ContextFrame> contextStack() {
		Deque<ContextFrame> retval = contextStack.get( );

		if ( retval == null ) {
			retval = new ArrayDeque<>( );

			contextStack.set( retval );
		}

		return retval;
	}

	/*
	 * Helper class to track Session usage per context
	 */
	public class ContextFrame {
		private int usageCount;
		private Session session;
		private ShardContext shardContext;

		public ContextFrame( ShardContext shardContext, Session session ) {
			this.shardContext = shardContext;
			this.session = session;
		}

		public void increaseUsageCount() {
			usageCount++;
		}

		public void decreaseUsageCount() {
			usageCount--;
		}

		public Session getSession() {
			return session;
		}

		public ShardContext getShardContext() {
			return shardContext;
		}

		public boolean isSame( ShardContext shardContext ) {
			return this.shardContext == shardContext;
		}

		public boolean isSpent() {
			return usageCount < 0;
		}

		@Override
		public String toString() {
			return "ContextFrame [usageCount=" + usageCount + ", session=" + session + ", shardContext=" + shardContext + "]";
		}
	}

}
