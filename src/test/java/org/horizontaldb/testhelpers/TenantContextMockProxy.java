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

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.horizontaldb.shard.hibernate.TenantContext;
import org.springframework.stereotype.Component;

@Component
public class TenantContextMockProxy implements CurrentTenantIdentifierResolver {
	private TenantContext mockContext;

	public void setMockTenantContext( TenantContext mockContext ) {
		this.mockContext = mockContext;
	}

	@Override
	public String resolveCurrentTenantIdentifier() {
		return mockContext.resolveCurrentTenantIdentifier( );
	}

	@Override
	public boolean validateExistingCurrentSessions() {
		return mockContext.validateExistingCurrentSessions( );
	}

}
