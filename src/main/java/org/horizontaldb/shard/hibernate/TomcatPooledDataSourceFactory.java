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

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TomcatPooledDataSourceFactory implements DataSourceFactory {
	private static final Logger LOG = LoggerFactory.getLogger( TomcatPooledDataSourceFactory.class );

	@Value( "#{dbProperties['tomcatPooledDataSource.driverClassName']}" )
	private String driverClassName;
	@Value( "#{dbProperties['tomcatPooledDataSource.urlTemplate']}" )
	private String urlTemplate;
	@Value( "#{dbProperties['tomcatPooledDataSource.initialSize']}" )
	private int initialSize;
	@Value( "#{dbProperties['tomcatPooledDataSource.maxActive']}" )
	private int maxActive;
	@Value( "#{dbProperties['tomcatPooledDataSource.minIdle']}" )
	private int minIdle;
	@Value( "#{dbProperties['tomcatPooledDataSource.maxIdle']}" )
	private int maxIdle;
	@Value( "#{dbProperties['tomcatPooledDataSource.validationQuery']}" )
	private String validationQuery;
	@Value( "#{dbProperties['tomcatPooledDataSource.validationInterval']}" )
	private int validationInterval;
	@Value( "#{dbProperties['tomcatPooledDataSource.timeBetweenEvictionRunsMillis']}" )
	private int timeBetweenEvictionRunsMillis;
	@Value( "#{dbProperties['tomcatPooledDataSource.minEvictableIdleTimeMillis']}" )
	private int minEvictableIdleTimeMillis;
	@Value( "#{dbProperties['tomcatPooledDataSource.testOnBorrow']}" )
	private boolean testOnBorrow;
	@Value( "#{dbProperties['tomcatPooledDataSource.testOnReturn']}" )
	private boolean testOnReturn;
	@Value( "#{dbProperties['tomcatPooledDataSource.testWhileIdle']}" )
	private boolean testWhileIdle;
	@Value( "#{dbProperties['tomcatPooledDataSource.maxWait']}" )
	private int maxWait;
	@Value( "#{dbProperties['tomcatPooledDataSource.removeAbandoned']}" )
	private boolean removeAbandoned;
	@Value( "#{dbProperties['tomcatPooledDataSource.removeAbandonedTimeout']}" )
	private int removeAbandonedTimeout;
	@Value( "#{dbProperties['tomcatPooledDataSource.logAbandoned']}" )
	private boolean logAbandoned;
	@Value( "#{dbProperties['tomcatPooledDataSource.jdbcInterceptors']}" )
	private String jdbcInterceptors;

	@Override
	public DataSourceResource getDataSource( String tenantId ) {
		org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource( );

		dataSource.setDriverClassName( driverClassName );
		dataSource.setUrl( String.format( urlTemplate, tenantId ) );
		dataSource.setUsername( tenantId );
		dataSource.setPassword( getTenantPassword( tenantId ) );
		dataSource.setInitialSize( initialSize );
		dataSource.setMaxActive( maxActive );
		dataSource.setMinIdle( minIdle );
		dataSource.setMaxIdle( maxIdle );
		dataSource.setValidationQuery( validationQuery );
		dataSource.setValidationInterval( validationInterval );
		dataSource.setTimeBetweenEvictionRunsMillis( timeBetweenEvictionRunsMillis );
		dataSource.setMinEvictableIdleTimeMillis( minEvictableIdleTimeMillis );
		dataSource.setTestOnBorrow( testOnBorrow );
		dataSource.setTestOnReturn( testOnReturn );
		dataSource.setTestWhileIdle( testWhileIdle );
		dataSource.setMaxWait( maxWait );
		dataSource.setRemoveAbandoned( removeAbandoned );
		dataSource.setRemoveAbandonedTimeout( removeAbandonedTimeout );
		dataSource.setLogAbandoned( logAbandoned );
		dataSource.setJdbcInterceptors( jdbcInterceptors );

		LOG.debug( String.format( "getDataSource.%s=established [%s]", tenantId, dataSource ) );

		return new DataSourceResource( dataSource );
	}

	private String getTenantPassword( String tenantId ) {
		String retval = DigestUtils.md5Hex( tenantId );

		return retval;
	}

}
