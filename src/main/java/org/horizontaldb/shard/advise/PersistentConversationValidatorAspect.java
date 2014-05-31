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

import javax.inject.Inject;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.horizontaldb.shard.ConversationRegistry;
import org.horizontaldb.shard.ShardContext;
import org.horizontaldb.shard.annotation.ShardBean;
import org.horizontaldb.shard.annotation.ShardBeans;
import org.springframework.core.Ordered;

@Aspect
public class PersistentConversationValidatorAspect implements Ordered {
	@Inject
	private ConversationRegistry registry;

	private int order;

	@Override
	public int getOrder() {
		return order;
	}

	public void setOrder( int order ) {
		this.order = order;
	}

	@Before( value = "( within( org.horizontaldb..* ) ) && @annotation( shardBeans ) && args(shardContext, ..)", argNames = "shardBeans, shardContext" )
	public void validateConversation( ShardBeans shardBeans, ShardContext shardContext ) throws Throwable {
		validate( shardContext );
	}

	@Before( value = "( within( org.horizontaldb..* ) ) && @annotation( shardBean ) && args(shardContext, ..)", argNames = "shardBean, shardContext" )
	public void validateConversation( ShardBean shardBean, ShardContext shardContext ) throws Throwable {
		validate( shardContext );
	}

	private void validate( ShardContext shardContext ) {
		if ( !registry.hasConversation( shardContext.getClientId( ) ) ) {
			throw new IllegalStateException( "client is not involved in a conversation" );
		}
	}

}
