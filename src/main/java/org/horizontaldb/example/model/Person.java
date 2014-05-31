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

package org.horizontaldb.example.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table( name = "person" )
@Cache( usage = CacheConcurrencyStrategy.READ_WRITE )
public class Person {
	@Id
	@SequenceGenerator( name = "person_id_seq", sequenceName = "person_id_seq", allocationSize = 1 )
	@GeneratedValue( generator = "person_id_seq", strategy = GenerationType.IDENTITY )
	private Long id;

	@Column( name = "name" )
	@NotNull
	private String name;

	@ManyToOne( fetch = FetchType.EAGER )
	@JoinColumn( name = "department_id" )
	@Fetch( FetchMode.JOIN )
	private Department department;

	public Long getId() {
		return id;
	}

	public void setId( Long id ) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment( Department department ) {
		this.department = department;
	}

	@Override
	public String toString() {
		return "Person [id=" + id + ", name=" + name + ", department=" + department + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( department == null ) ? 0 : department.hashCode( ) );
		result = prime * result + ( ( id == null ) ? 0 : id.hashCode( ) );
		result = prime * result + ( ( name == null ) ? 0 : name.hashCode( ) );
		return result;
	}

	@Override
	public boolean equals( Object obj ) {
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass( ) != obj.getClass( ) )
			return false;
		Person other = ( Person ) obj;
		if ( department == null ) {
			if ( other.department != null )
				return false;
		} else if ( !department.equals( other.department ) )
			return false;
		if ( id == null ) {
			if ( other.id != null )
				return false;
		} else if ( !id.equals( other.id ) )
			return false;
		if ( name == null ) {
			if ( other.name != null )
				return false;
		} else if ( !name.equals( other.name ) )
			return false;
		return true;
	}

}
