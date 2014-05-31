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

import javax.sql.DataSource;

import org.horizontaldb.shard.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourceResource implements Resource {
	private static final Logger LOG = LoggerFactory.getLogger( DataSourceResource.class );

	private org.apache.tomcat.jdbc.pool.DataSource dataSource;

	public DataSourceResource( DataSource dataSource ) {
		if ( !( dataSource instanceof org.apache.tomcat.jdbc.pool.DataSource ) ) {
			throw new IllegalArgumentException( );
		}

		this.dataSource = ( org.apache.tomcat.jdbc.pool.DataSource ) dataSource;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	@Override
	public void release() {
		if ( dataSource != null ) {
			LOG.debug( String.format( "release=releasing dataSource [%s]", dataSource ) );

			dataSource.close( true );

			dataSource = null;
		}
	}

}
