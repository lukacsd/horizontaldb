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

package org.horizontaldb.shard.resolver;

import javax.inject.Inject;

import org.horizontaldb.shard.ShardContext;
import org.horizontaldb.util.BeanUtils;
import org.springframework.stereotype.Component;

@Component( "defaultResolver" )
public class SpringResolver implements ShardBeanResolver {
	@Inject
	private BeanUtils beanUtils;

	@Override
	public <T> T getBean( Class<T> beanClass, ShardContext shardContext ) {
		return beanUtils.getSpringBean( beanClass );
	}

}
