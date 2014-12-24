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

import java.util.HashMap;
import java.util.Map;

/*
 * Configuration for the sharded data source access machinery. The client id will be propagated to the Hibernate
 * framework in case the method is decorated with the Transactional annotation. DataSourceType will be propagated to dao
 * resolvers and can be used for further type refinements. The clientId is immutable, dataSourceType is mutable.
 */
public class ShardContext {
    private String clientId;
    private DataSourceType dataSourceType;
    private Map<Class<?>, Object> beans = new HashMap<>( );

    public ShardContext( String clientId ) {
        this( clientId, null );
    }

    public ShardContext( String clientId, DataSourceType dataSourceType ) {
        this.clientId = clientId;
        this.dataSourceType = dataSourceType;
    }

    public String getClientId() {
        return clientId;
    }

    public DataSourceType getDataSourceType() {
        return dataSourceType;
    }

    @SuppressWarnings( "unchecked" )
    public <T> T getBean( Class<T> beanClass ) {
        T retval = ( T ) beans.get( beanClass );

        return retval;
    }

    public void setBean( Class<?> beanClass, Object bean ) {
        beans.put( beanClass, bean );
    }

    @Override
    public String toString() {
        return "ShardContext [clientId=" + clientId + ", dataSourceType=" + dataSourceType + "]";
    }

}
