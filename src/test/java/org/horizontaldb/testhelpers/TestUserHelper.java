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

import org.horizontaldb.example.model.UserToken;
import org.springframework.stereotype.Component;

@Component
public class TestUserHelper {
	private UserToken joeToken;
	private UserToken janeToken;

	public static enum TestUser {
		JOE, JANE;
	}

	public static enum TestDepartment {
		DEVELOPMENT, ACCOUNTING
	};

	public static enum TestPerson {
		DUDE( "Developer Dude", TestDepartment.DEVELOPMENT ),
		STEVE( "Tester Steve", TestDepartment.DEVELOPMENT ),
		SAM( "Developer Sam", TestDepartment.DEVELOPMENT ),
		BARBARA( "Accountant Barbara", TestDepartment.ACCOUNTING ),
		NANCY( "Accountant Nancy", TestDepartment.ACCOUNTING ),
		MARY( "Accountant Mary", TestDepartment.ACCOUNTING );

		private String name;
		private TestDepartment department;

		private TestPerson( String name, TestDepartment department ) {
			this.name = name;
			this.department = department;
		}

		public String getName() {
			return name;
		}

		public String getDepartmentName() {
			return department.name( );
		}
	};

	public UserToken getJoeToken() {
		if ( joeToken == null ) {
			joeToken = new UserToken( ) {

				@Override
				public String getUserId() {
					return TestUser.JOE.name( );
				}

				@Override
				public String getOrigin() {
					return null;
				}
			};
		}

		return joeToken;
	}

	public UserToken getJaneToken() {
		if ( janeToken == null ) {
			janeToken = new UserToken( ) {

				@Override
				public String getUserId() {
					return TestUser.JANE.name( );
				}

				@Override
				public String getOrigin() {
					return null;
				}
			};
		}

		return janeToken;
	}

}
