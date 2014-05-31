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

import org.horizontaldb.example.model.Department;
import org.horizontaldb.example.model.dao.DepartmentDao;
import org.horizontaldb.shard.ShardContext;
import org.horizontaldb.shard.annotation.ShardBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TestServiceThree {
	private static final Logger LOG = LoggerFactory.getLogger( TestServiceThree.class );

	@Transactional( readOnly = true )
	@ShardBean( DepartmentDao.class )
	public void listDepartments( ShardContext shardContext ) {
		DepartmentDao departmentDao = shardContext.getBean( DepartmentDao.class );

		for ( Department department : departmentDao.getAllDepartments( ) ) {
			LOG.info( department.toString( ) );
		}
	}

}
