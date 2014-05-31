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

import org.horizontaldb.shard.ShardBeanEnricher;
import org.horizontaldb.shard.ShardContext;
import org.horizontaldb.shard.hibernate.AbstractDaoEnricher;
import org.springframework.orm.hibernate4.HibernateTransactionManager;

public class ShardBeanEnricherMockProxy extends AbstractDaoEnricher {

	public ShardBeanEnricherMockProxy( HibernateTransactionManager txManager ) {
		super( txManager );
	}

	private ShardBeanEnricher mockEnricher;

	public void setMockEnricher( ShardBeanEnricher mockEnricher ) {
		this.mockEnricher = mockEnricher;
	}

	@Override
	public void setup( Object bean, ShardContext shardContext ) {
		mockEnricher.setup( bean, shardContext );

		super.setup( bean, shardContext );
	}

	@Override
	public void tearDown( Object bean, ShardContext shardContext ) {
		mockEnricher.tearDown( bean, shardContext );

		super.tearDown( bean, shardContext );
	}

}
