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

package org.horizontaldb.example.model.dao;

import org.hibernate.Query;
import org.hibernate.Session;

public abstract class AbstractDao {
	private ThreadLocal<Session> session = new ThreadLocal<>( );

	public Session getSession() {
		return this.session.get( );
	}

	public void setSession( Session session ) {
		this.session.set( session );
	}

	protected QueryBuilder getQueryBuilder() {
		return new QueryBuilder( );
	}

	protected class QueryBuilder {
		private String query;
		private boolean cacheable = true;

		public QueryBuilder query( String query ) {
			this.query = query;

			return this;
		}

		public QueryBuilder cacheable( boolean cacheable ) {
			this.cacheable = cacheable;

			return this;
		}

		public Query build() {
			Query retval = getSession( ).createQuery( query );

			retval.setCacheable( cacheable );

			return retval;
		}
	}

}
