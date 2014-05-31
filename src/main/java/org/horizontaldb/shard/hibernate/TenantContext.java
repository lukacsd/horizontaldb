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

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component( "defaultTenantContext" )
public class TenantContext implements CurrentTenantIdentifierResolver {
	private ThreadLocal<String> currentTenantId = new ThreadLocal<>( );

	public String getCurrentTenantId() {
		return currentTenantId.get( );
	}

	public void setCurrentTenantId( String tenantId ) {
		currentTenantId.set( tenantId );
	}

	@Override
	public String resolveCurrentTenantIdentifier() {
		return getCurrentTenantId( );
	}

	@Override
	public boolean validateExistingCurrentSessions() {
		return true;
	}

}
