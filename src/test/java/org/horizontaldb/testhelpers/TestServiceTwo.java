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

import javax.inject.Inject;

import org.horizontaldb.example.model.Person;
import org.horizontaldb.example.model.dao.PersonDao;
import org.horizontaldb.shard.ShardContext;
import org.horizontaldb.shard.annotation.ShardBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TestServiceTwo {
	private static final Logger LOG = LoggerFactory.getLogger( TestServiceTwo.class );

	@Inject
	private TestServiceThree testServiceThree;

	@Transactional( readOnly = true )
	@ShardBean( PersonDao.class )
	public void listPersonsAndDepartments( ShardContext shardContext ) {
		PersonDao personDao = shardContext.getBean( PersonDao.class );

		for ( Person person : personDao.getAllPersons( ) ) {
			LOG.info( person.toString( ) );
		}

		ShardContext innerContext = new ShardContext( shardContext.getClientId( ) );

		testServiceThree.listDepartments( innerContext );
	}

}
