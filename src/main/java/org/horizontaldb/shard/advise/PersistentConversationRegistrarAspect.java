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

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.horizontaldb.example.model.UserToken;
import org.horizontaldb.shard.ConversationRegistry;
import org.horizontaldb.shard.annotation.PersistentConversationSetup;
import org.horizontaldb.shard.annotation.PersistentConversationTeardown;
import org.springframework.core.Ordered;

@Aspect
public class PersistentConversationRegistrarAspect implements Ordered {
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

	@AfterReturning( value = "( within( org.horizontaldb..* ) ) && @annotation( persistentConversationSetup ) && args(userToken, ..)", argNames = "persistentConversationSetup, userToken" )
	public void startConversation( PersistentConversationSetup persistentConversationSetup, UserToken userToken ) throws Throwable {
		registry.startConversation( userToken );
	}

	@AfterReturning( value = "( within( org.horizontaldb..* ) ) && @annotation( persistentConversationTeardown ) && args(userToken, ..)", argNames = "persistentConversationTeardown, userToken" )
	public void tearDownConversation( PersistentConversationTeardown persistentConversationTeardown, UserToken userToken ) throws Throwable {
		registry.teardownConversation( userToken );
	}

}
