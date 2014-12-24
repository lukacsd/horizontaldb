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

import java.net.MalformedURLException;
import java.net.URL;

/*
 * Workaround for https://hibernate.atlassian.net/browse/HHH-8213
 */
public class EhCacheRegionFactory extends org.hibernate.cache.ehcache.EhCacheRegionFactory {
    private static final long serialVersionUID = -1019987730124244207L;

    @Override
    protected URL loadResource( String configurationResourceName ) {
        URL url = null;

        if ( url == null ) {
            final ClassLoader standardClassloader = Thread.currentThread( ).getContextClassLoader( );

            if ( standardClassloader != null ) {
                url = standardClassloader.getResource( configurationResourceName );
            }

            if ( url == null ) {
                url = EhCacheRegionFactory.class.getResource( configurationResourceName );
            }

            if ( url == null ) {
                try {
                    url = new URL( configurationResourceName );
                } catch ( MalformedURLException e ) {
                    // ignore
                }
            }
        }

        return url;
    }

}
