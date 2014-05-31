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

package org.horizontaldb.shard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.horizontaldb.example.model.UserToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConversationRegistry {
	private static final Logger LOG = LoggerFactory.getLogger( ConversationRegistry.class );

	private final Object lock = new Object( );
	private Map<String, Boolean> conversationMap = new HashMap<>( );
	private Map<String, Set<Resource>> conversationResourceMap = new HashMap<>( );

	public void startConversation( UserToken userToken ) {
		synchronized ( lock ) {
			if ( !conversationMap.containsKey( userToken.getUserId( ) ) ) {
				conversationMap.put( userToken.getUserId( ), true );

				LOG.debug( String.format( "startConversation.%s=conversation started", userToken.getUserId( ) ) );
			} else {
				LOG.debug( String.format( "startConversation.%s=conversation already started", userToken.getUserId( ) ) );
			}
		}
	}

	public boolean hasConversation( String clientId ) {
		boolean retval = false;

		synchronized ( lock ) {
			retval = conversationMap.containsKey( clientId );
		}

		return retval;
	}

	public void teardownConversation( UserToken userToken ) {
		synchronized ( lock ) {
			if ( conversationMap.containsKey( userToken.getUserId( ) ) ) {
				releaseResources( userToken.getUserId( ) );

				conversationMap.remove( userToken.getUserId( ) );

				LOG.debug( String.format( "teardownConversation.%s=conversation torn down", userToken.getUserId( ) ) );
			} else {
				LOG.error( String.format( "teardownConversation.%s=client is not involved in a valid conversation", userToken.getUserId( ) ) );
			}
		}
	}

	public void addResource( String clientId, Object resource ) {
		if ( resource instanceof Resource ) {
			synchronized ( lock ) {
				if ( conversationMap.containsKey( clientId ) ) {
					Set<Resource> resourceSet = conversationResourceMap.get( clientId );

					if ( resourceSet == null ) {
						resourceSet = new HashSet<>( );

						conversationResourceMap.put( clientId, resourceSet );
					}

					if ( resourceSet.add( ( Resource ) resource ) ) {
						LOG.debug( String.format( "addResource.%s=resource registered", clientId ) );
					} else {
						LOG.debug( String.format( "addResource.%s=resource already registered", clientId ) );
					}
				} else {
					LOG.error( String.format( "addResource.%s=client is not involved in a valid conversation", clientId ) );
				}
			}
		}
	}

	private void releaseResources( String clientId ) {
		if ( conversationResourceMap.containsKey( clientId ) ) {
			Iterator<Resource> iterator = conversationResourceMap.get( clientId ).iterator( );

			while ( iterator.hasNext( ) ) {
				Resource resource = iterator.next( );

				try {
					resource.release( );
				} catch ( Exception ex ) {
					LOG.error( String.format( "releaseResource.%s=could not release dataSource [%s]", clientId, resource ), ex );
				}
			}

			conversationResourceMap.remove( clientId );
		}
	}

}
