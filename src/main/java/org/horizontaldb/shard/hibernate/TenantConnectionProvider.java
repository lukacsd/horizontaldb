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

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.hibernate.service.spi.Stoppable;
import org.horizontaldb.shard.ConversationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component( "defaultTenantConnectionProvider" )
public class TenantConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl implements Stoppable {
	private static final long serialVersionUID = 8458388188894077307L;
	private static final Logger LOG = LoggerFactory.getLogger( TenantConnectionProvider.class );

	@Inject
	private ConversationRegistry registry;

	@Inject
	private DataSourceFactory dataSourceFactory;

	@Value( "#{dbProperties['multiTenantConnectionProvider.anyDataSourceTenantId']}" )
	private String anyDataSourceTenantId;

	private final Object lock = new Object( );
	private Map<String, DataSourceResource> dataSourceMap = new HashMap<>( );

	@Override
	public void stop() {
		synchronized ( lock ) {
			if ( dataSourceMap != null ) {
				for ( String tenantId : dataSourceMap.keySet( ) ) {
					try {
						releaseResource( tenantId );
					} catch ( Exception ex ) {
						LOG.error( String.format( "releaseResource.%s=could not release dataSource [%s]", tenantId ), ex );
					}
				}

				dataSourceMap = null;
			}
		}
	}

	@Override
	protected DataSource selectAnyDataSource() {
		DataSource retval = null;

		synchronized ( lock ) {
			retval = selectDataSource( anyDataSourceTenantId );
		}

		return retval;
	}

	@Override
	protected DataSource selectDataSource( String tenantIdentifier ) {
		DataSource retval = null;

		synchronized ( lock ) {
			DataSourceResource resource = dataSourceMap.get( tenantIdentifier );

			if ( resource == null || resource.getDataSource( ) == null ) {
				resource = dataSourceFactory.getDataSource( tenantIdentifier );

				dataSourceMap.put( tenantIdentifier, resource );
			}

			retval = resource.getDataSource( );

			registry.addResource( tenantIdentifier, resource );
		}

		return retval;
	}

	private void releaseResource( String tenantIdentifier ) {
		DataSourceResource resource = dataSourceMap.get( tenantIdentifier );

		if ( resource != null ) {
			resource.release( );

			LOG.debug( String.format( "releaseResource.%s=released datasource", tenantIdentifier ) );
		} else {
			LOG.debug( String.format( "releaseResource.%s=no datasource was found", tenantIdentifier ) );
		}
	}

}
