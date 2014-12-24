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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.horizontaldb.shard.annotation.ShardBean;
import org.horizontaldb.shard.annotation.ShardBeans;
import org.horizontaldb.shard.resolver.ShardBeanResolver;
import org.horizontaldb.util.BeanUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public abstract class ShardContextEnricher {
	@Inject
	private ConversationRegistry registry;

	@Inject
	private BeanUtils beanUtils;

	private Map<Class<?>, ShardBeanResolver> targetResolvers;
	private Map<Class<?>, ShardBeanEnricher> targetEnrichers;

	public void setTargetResolvers( Map<Class<?>, ShardBeanResolver> targetResolvers ) {
		this.targetResolvers = targetResolvers;
	}

	public void setTargetEnrichers( Map<Class<?>, ShardBeanEnricher> targetEnrichers ) {
		this.targetEnrichers = targetEnrichers;
	}

	protected Collection<Object> doSetup( ShardBeans shardBeans, ShardContext shardContext ) {
		Collection<Object> resolvedBeans = new LinkedList<>( );

		Set<ShardBean> resolvedTypes = new HashSet<>( );

		for ( ShardBean shardBean : shardBeans.value( ) ) {
			if ( !resolvedTypes.contains( shardBean ) ) {
				resolvedTypes.add( shardBean );

				resolvedBeans.add( populateShardContextWithBean( shardBean, shardContext ) );
			}
		}

		for ( Object bean : resolvedBeans ) {
			enricherDoSetup( bean, shardContext );
		}

		return resolvedBeans;
	}

	protected Object doSetup( ShardBean shardBean, ShardContext shardContext ) {
		Object resolvedBean = populateShardContextWithBean( shardBean, shardContext );

		enricherDoSetup( resolvedBean, shardContext );

		return resolvedBean;
	}

	protected void doTearDown( Collection<Object> beans, ShardContext shardContext ) {
		for ( Object bean : beans ) {
			doTearDown( bean, shardContext );
		}
	}

	protected void doTearDown( Object bean, ShardContext shardContext ) {
		enricherDoTearDown( bean, shardContext );
	}

	private Object populateShardContextWithBean( ShardBean shardBean, ShardContext shardContext ) {
		Object bean = getResolver( shardBean.value( ) ).getBean( shardBean.value( ), shardContext );

		if ( bean == null ) {
			throw new NoSuchBeanDefinitionException( shardBean.value( ).toString( ) );
		}

		registerResourceWithConversation( shardContext, bean );

		shardContext.setBean( shardBean.value( ), bean );

		return bean;
	}

	private void registerResourceWithConversation( ShardContext shardContext, Object bean ) {
		registry.addResource( shardContext.getClientId( ), bean );
	}

	/*
	 * returns the first matching resolver, based on equality, then assignability
	 */
	private ShardBeanResolver getResolver( Class<?> value ) {
		ShardBeanResolver retval = null;

		if ( targetResolvers != null ) {
			for ( Entry<Class<?>, ShardBeanResolver> entry : targetResolvers.entrySet( ) ) {
				if ( entry.getKey( ).equals( value ) || entry.getKey( ).isAssignableFrom( value ) ) {
					retval = entry.getValue( );

					break;
				}
			}
		}

		if ( retval == null ) {
			throw new IllegalStateException( "ShardContextEnricher is not configured for [" + value + "]" );
		}

		return retval;
	}

	private void enricherDoSetup( Object bean, ShardContext shardContext ) {
		Object target = beanUtils.unpackDynamicProxy( bean );

		ShardBeanEnricher enricher = getEnricher( target );

		if ( enricher != null ) {
			enricher.setup( target, shardContext );
		}
	}

	private void enricherDoTearDown( Object bean, ShardContext shardContext ) {
		Object target = beanUtils.unpackDynamicProxy( bean );

		ShardBeanEnricher enricher = getEnricher( target );

		if ( enricher != null ) {
			enricher.tearDown( target, shardContext );
		}
	}

	/*
	 * returns the first matching enricher for a bean, based on the key's assignment compatibility. the return value
	 * could be null if there is no match
	 */
	private ShardBeanEnricher getEnricher( Object bean ) {
		ShardBeanEnricher retval = null;

		if ( targetEnrichers != null ) {
			for ( Entry<Class<?>, ShardBeanEnricher> entry : targetEnrichers.entrySet( ) ) {
				if ( entry.getKey( ).isInstance( bean ) ) {
					retval = entry.getValue( );

					break;
				}
			}
		}

		return retval;
	}

}
