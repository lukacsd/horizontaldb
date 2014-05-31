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

import java.util.Collection;
import java.util.List;

import org.hibernate.Query;
import org.horizontaldb.example.model.Person;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PersonDaoImpl extends AbstractDao implements PersonDao {

	@Override
	@Transactional( propagation = Propagation.SUPPORTS, readOnly = true )
	public long getCountOfPersonsByDepartment( Long departmentId ) {
		Query query = getQueryBuilder( ).query( "select count( name ) from Person where department_id = :deparmentId" ).build( );

		query.setParameter( "deparmentId", departmentId );

		long retval = ( Long ) query.uniqueResult( );

		return retval;
	}

	@Override
	@SuppressWarnings( "unchecked" )
	@Transactional( propagation = Propagation.SUPPORTS, readOnly = true )
	public Collection<Person> getPersonNamesByDepartment( Long departmentId ) {
		Query query = getQueryBuilder( ).query( "from Person where department_id = :deparmentId" ).build( );

		query.setParameter( "deparmentId", departmentId );

		List<Person> retval = query.list( );

		return retval;
	}

	@Override
	@SuppressWarnings( "unchecked" )
	@Transactional( propagation = Propagation.SUPPORTS, readOnly = true )
	public Collection<Person> getAllPersons() {
		Query query = getQueryBuilder( ).query( "from Person" ).build( );

		List<Person> retval = query.list( );

		return retval;
	}

	@Override
	@Transactional( propagation = Propagation.SUPPORTS, readOnly = true )
	public Person getPersonById( Long id ) {
		return ( Person ) getSession( ).get( Person.class, id );
	}

	@Override
	@Transactional( propagation = Propagation.MANDATORY, readOnly = false )
	public Person save( Person object ) {
		Person retval = object;

		if ( object.getId( ) == null ) {
			getSession( ).save( object );
		} else {
			retval = ( Person ) getSession( ).merge( object );
		}

		return retval;
	}

	@Override
	@Transactional( propagation = Propagation.MANDATORY, readOnly = false )
	public void delete( Long objectId ) {
		if ( objectId != null ) {
			Person object = getPersonById( objectId );

			if ( object != null ) {
				getSession( ).delete( object );
			}
		}
	}

}
