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
import org.horizontaldb.example.model.Department;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class DepartmentDaoImpl extends AbstractDao implements DepartmentDao {

	@Override
	@Transactional( propagation = Propagation.SUPPORTS, readOnly = true )
	public long getCountOfDepartments() {
		Query query = getQueryBuilder( ).query( "select count( name ) from Department" ).build( );

		long retval = ( Long ) query.uniqueResult( );

		return retval;
	}

	@Override
	@SuppressWarnings( "unchecked" )
	@Transactional( propagation = Propagation.SUPPORTS, readOnly = true )
	public Collection<Department> getAllDepartments() {
		Query query = getQueryBuilder( ).query( "from Department" ).build( );

		List<Department> retval = query.list( );

		return retval;
	}

	@Override
	@Transactional( propagation = Propagation.SUPPORTS, readOnly = true )
	public Department getDepartmentById( Long id ) {
		return ( Department ) getSession( ).get( Department.class, id );
	}

	@Override
	@Transactional( propagation = Propagation.SUPPORTS, readOnly = true )
	public Department getDepartmentByName( String name ) {
		Query query = getQueryBuilder( ).query( "from Department where name = :name" ).build( );

		query.setParameter( "name", name );

		Department retval = ( Department ) query.uniqueResult( );

		return retval;
	}

	@Override
	@Transactional( propagation = Propagation.MANDATORY, readOnly = false )
	public Department save( Department object ) {
		Department retval = object;

		if ( object.getId( ) == null ) {
			getSession( ).save( object );
		} else {
			retval = ( Department ) getSession( ).merge( object );
		}

		return retval;
	}

	@Override
	@Transactional( propagation = Propagation.MANDATORY, readOnly = false )
	public void delete( Long objectId ) {
		if ( objectId != null ) {
			Department object = getDepartmentById( objectId );

			if ( object != null ) {
				getSession( ).delete( object );
			}
		}
	}

}
