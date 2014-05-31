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

package org.horizontaldb.util;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class BeanUtils implements ApplicationContextAware {
	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public <T> T getSpringBean( Class<T> beanType ) {
		return applicationContext.getBean( beanType );
	}

	@SuppressWarnings( "unchecked" )
	public <T> T getSpringBean( String beanName ) {
		return ( T ) applicationContext.getBean( beanName );
	}

	@SuppressWarnings( "unchecked" )
	public <T> T getUnpackedSpringBean( String beanName ) {
		Object object = applicationContext.getBean( beanName );

		Object target = unpackDynamicProxy( object );

		return ( T ) target;
	}

	public Object unpackDynamicProxy( Object bean ) {
		Object target = bean;

		if ( AopUtils.isJdkDynamicProxy( bean ) ) {
			try {
				target = ( ( Advised ) bean ).getTargetSource( ).getTarget( );
			} catch ( Exception e ) {
				throw new IllegalStateException( e );
			}
		}

		return target;
	}

}
