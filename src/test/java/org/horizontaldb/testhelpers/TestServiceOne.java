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

import java.util.Collection;
import java.util.LinkedList;

import javax.inject.Inject;

import org.horizontaldb.example.model.Department;
import org.horizontaldb.example.model.Person;
import org.horizontaldb.example.model.UserToken;
import org.horizontaldb.example.model.dao.DepartmentDao;
import org.horizontaldb.example.model.dao.PersonDao;
import org.horizontaldb.shard.ShardContext;
import org.horizontaldb.shard.annotation.PersistentConversationSetup;
import org.horizontaldb.shard.annotation.PersistentConversationTeardown;
import org.horizontaldb.shard.annotation.ShardBean;
import org.horizontaldb.shard.annotation.ShardBeans;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TestServiceOne {
	@Inject
	private TestServiceTwo testServiceTwo;

	@PersistentConversationSetup
	public void authenticate( UserToken userToken ) {
	}

	@PersistentConversationTeardown
	public void logoff( UserToken userToken ) {
	}

	@Transactional( readOnly = true )
	@ShardBean( DepartmentDao.class )
	public Long getCountOfDepartments( ShardContext shardContext ) {
		DepartmentDao departmentDao = shardContext.getBean( DepartmentDao.class );

		Long retval = departmentDao.getCountOfDepartments( );

		return retval;
	}

	@Transactional( readOnly = true )
	@ShardBeans( { @ShardBean( PersonDao.class ), @ShardBean( DepartmentDao.class ) } )
	public Long getCountOfPersonsByDepartment( ShardContext shardContext, String departmentName ) {
		PersonDao personDao = shardContext.getBean( PersonDao.class );
		DepartmentDao departmentDao = shardContext.getBean( DepartmentDao.class );

		Department department = departmentDao.getDepartmentByName( departmentName );

		if ( department == null ) {
			throw new IllegalArgumentException( String.format( "Department [%s] does not exists", departmentName ) );
		}

		Long retval = personDao.getCountOfPersonsByDepartment( department.getId( ) );

		return retval;
	}

	@Transactional( readOnly = true )
	@ShardBeans( { @ShardBean( PersonDao.class ), @ShardBean( DepartmentDao.class ) } )
	public Collection<String> getPersonNamesByDepartment( ShardContext shardContext, String departmentName ) {
		PersonDao personDao = shardContext.getBean( PersonDao.class );
		DepartmentDao departmentDao = shardContext.getBean( DepartmentDao.class );

		Department department = departmentDao.getDepartmentByName( departmentName );

		if ( department == null ) {
			throw new IllegalArgumentException( String.format( "Department [%s] does not exists", departmentName ) );
		}

		Collection<String> retval = new LinkedList<String>( );

		Collection<Person> persons = personDao.getPersonNamesByDepartment( department.getId( ) );

		if ( persons != null ) {
			for ( Person person : persons ) {
				retval.add( person.getName( ) );
			}
		}

		return retval;
	}

	@Transactional
	@ShardBean( DepartmentDao.class )
	public void createDepartment( ShardContext shardContext, String departmentName ) {
		DepartmentDao departmentDao = shardContext.getBean( DepartmentDao.class );

		try {
			Department department = new Department( );
			department.setName( departmentName );

			departmentDao.save( department );
		} catch ( DataAccessException ex ) {
			throw new IllegalStateException( ex );
		}
	}

	@Transactional
	@ShardBeans( { @ShardBean( PersonDao.class ), @ShardBean( DepartmentDao.class ) } )
	public void addPersonToDepartment( ShardContext shardContext, String personName, String departmentName ) {
		PersonDao personDao = shardContext.getBean( PersonDao.class );
		DepartmentDao departmentDao = shardContext.getBean( DepartmentDao.class );

		Department department = departmentDao.getDepartmentByName( departmentName );

		if ( department == null ) {
			throw new IllegalArgumentException( String.format( "Department [%s] does not exists", departmentName ) );
		}

		try {
			Person person = new Person( );
			person.setName( personName );
			person.setDepartment( department );

			personDao.save( person );
		} catch ( DataAccessException ex ) {
			throw new IllegalStateException( ex );
		}
	}

	public void callNestedServiceChain( String clientId ) {
		ShardContext innerContext = new ShardContext( clientId );

		testServiceTwo.listPersonsAndDepartments( innerContext );
	}

}
