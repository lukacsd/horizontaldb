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

import org.horizontaldb.shard.ShardContext;
import org.horizontaldb.shard.resolver.ShardBeanResolver;
import org.horizontaldb.shard.resolver.SpringResolver;

public class ShardBeanResolverMockProxy extends SpringResolver {
	private ShardBeanResolver mockResolver;

	public void setMockResolver( ShardBeanResolver mockResolver ) {
		this.mockResolver = mockResolver;
	}

	@Override
	public <T> T getBean( Class<T> beanClass, ShardContext shardContext ) {
		mockResolver.getBean( beanClass, shardContext );

		return super.getBean( beanClass, shardContext );
	}

}
