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

import org.horizontaldb.example.model.UserToken;
import org.horizontaldb.shard.ConversationRegistry;

public class ConversationRegistryMockProxy extends ConversationRegistry {
	private ConversationRegistry mockRegistry;

	public void setMockRegistry( ConversationRegistry mockRegistry ) {
		this.mockRegistry = mockRegistry;
	}

	@Override
	public void startConversation( UserToken userToken ) {
		mockRegistry.startConversation( userToken );

		super.startConversation( userToken );
	}

	@Override
	public boolean hasConversation( String clientId ) {
		boolean retval = mockRegistry.hasConversation( clientId );

		super.hasConversation( clientId );

		return retval;
	}

	@Override
	public void teardownConversation( UserToken userToken ) {
		mockRegistry.teardownConversation( userToken );

		super.teardownConversation( userToken );
	}

	@Override
	public void addResource( String clientId, Object resource ) {
		mockRegistry.addResource( clientId, resource );

		super.addResource( clientId, resource );
	}

}
