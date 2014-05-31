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

package org.horizontaldb.shard.advise;

import java.util.Collection;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.horizontaldb.shard.ShardContext;
import org.horizontaldb.shard.ShardContextEnricher;
import org.horizontaldb.shard.annotation.ShardBean;
import org.horizontaldb.shard.annotation.ShardBeans;
import org.springframework.core.Ordered;

@Aspect
public class ShardContextEnricherAspect extends ShardContextEnricher implements Ordered {
	private int order;

	@Override
	public int getOrder() {
		return order;
	}

	public void setOrder( int order ) {
		this.order = order;
	}

	@Around( value = "( within( org.horizontaldb..* ) ) && @annotation( shardBeans ) && args(shardContext, ..)", argNames = "shardBeans, shardContext" )
	public Object populateShardContext( ProceedingJoinPoint pjp, ShardBeans shardBeans, ShardContext shardContext ) throws Throwable {
		Collection<Object> beans = doSetup( shardBeans, shardContext );

		Object proceed = null;

		try {
			proceed = pjp.proceed( );
		} finally {
			doTearDown( beans, shardContext );
		}

		return proceed;
	}

	@Around( value = "( within( org.horizontaldb..* ) ) && @annotation( shardBean ) && args(shardContext, ..)", argNames = "shardBean, shardContext" )
	public Object populateShardContext( ProceedingJoinPoint pjp, ShardBean shardBean, ShardContext shardContext ) throws Throwable {
		Object bean = doSetup( shardBean, shardContext );

		Object proceed = null;

		try {
			proceed = pjp.proceed( );
		} finally {
			doTearDown( bean, shardContext );
		}

		return proceed;
	}

}
