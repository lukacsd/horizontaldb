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

package org.horizontaldb.shard.hibernate;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.easymock.EasyMock;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.horizontaldb.example.model.dao.AbstractDao;
import org.horizontaldb.example.model.dao.DepartmentDaoImpl;
import org.horizontaldb.example.model.dao.PersonDaoImpl;
import org.horizontaldb.shard.ShardContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.orm.hibernate4.HibernateTransactionManager;

@RunWith( JUnit4.class )
public class AbstractDaoEnricherTest {
    private HibernateTransactionManager txManager;

    @Before
    public void setUp() {
        txManager = EasyMock.createMock( HibernateTransactionManager.class );
    }

    @After
    public void tearDown() {
        txManager = null;
    }

    @Test
    public void shouldSetAndUnsetSessionForDao() {
        AbstractDaoEnricher enricher = new AbstractDaoEnricher( txManager );

        ShardContext shardContext = new ShardContext( "testClient" );
        SessionFactory mockSessionFactory = EasyMock.createMock( SessionFactory.class );
        Session mockSession = EasyMock.createMock( "mockSession", Session.class );
        AbstractDao mockDao = EasyMock.createMock( "mockDao", AbstractDao.class );

        // called during statistics logging
        expect( mockSession.getSessionFactory( ) ).andReturn( mockSessionFactory );
        expect( mockSessionFactory.getStatistics( ) ).andReturn( null );
        // called during statistics logging

        expect( txManager.getSessionFactory( ) ).andReturn( mockSessionFactory );
        expect( mockSessionFactory.getCurrentSession( ) ).andReturn( mockSession );
        // setup
        mockDao.setSession( mockSession );
        // tearDown
        mockDao.setSession( null );

        replay( txManager, mockSessionFactory, mockSession, mockDao );

        enricher.setup( mockDao, shardContext );

        enricher.tearDown( mockDao, shardContext );

        verify( txManager, mockSessionFactory, mockSession, mockDao );
    }

    @Test
    public void shouldSetAndUnsetDistinctSessionsForDaoUsingDistinctContexts() {
        AbstractDaoEnricher enricher = new AbstractDaoEnricher( txManager );

        ShardContext shardContext = new ShardContext( "testClient" );
        ShardContext shardContext1 = new ShardContext( "testClient1" );
        ShardContext shardContext2 = new ShardContext( "testClient2" );

        SessionFactory mockSessionFactory = EasyMock.createMock( SessionFactory.class );
        Session mockSession = EasyMock.createMock( "mockSession", Session.class );
        Session mockSession1 = EasyMock.createMock( "mockSession1", Session.class );
        Session mockSession2 = EasyMock.createMock( "mockSession2", Session.class );
        AbstractDao mockDao = EasyMock.createMock( "mockDao", AbstractDao.class );

        // called during statistics logging
        expect( mockSession2.getSessionFactory( ) ).andReturn( mockSessionFactory );
        expect( mockSession1.getSessionFactory( ) ).andReturn( mockSessionFactory );
        expect( mockSession.getSessionFactory( ) ).andReturn( mockSessionFactory );
        expect( mockSessionFactory.getStatistics( ) ).andReturn( null ).times( 3 );
        // called during statistics logging

        expect( txManager.getSessionFactory( ) ).andReturn( mockSessionFactory ).times( 3 );
        expect( mockSessionFactory.getCurrentSession( ) ).andReturn( mockSession );
        expect( mockSessionFactory.getCurrentSession( ) ).andReturn( mockSession1 );
        expect( mockSessionFactory.getCurrentSession( ) ).andReturn( mockSession2 );
        // setups
        mockDao.setSession( mockSession );
        mockDao.setSession( mockSession1 );
        mockDao.setSession( mockSession2 );
        // tearDowns
        mockDao.setSession( mockSession1 );
        mockDao.setSession( mockSession );
        mockDao.setSession( null );

        replay( txManager, mockSessionFactory, mockSession, mockSession1, mockSession2, mockDao );

        enricher.setup( mockDao, shardContext );
        enricher.setup( mockDao, shardContext1 );
        enricher.setup( mockDao, shardContext2 );

        enricher.tearDown( mockDao, shardContext2 );
        enricher.tearDown( mockDao, shardContext1 );
        enricher.tearDown( mockDao, shardContext );

        verify( txManager, mockSessionFactory, mockSession, mockSession1, mockSession2, mockDao );
    }

    @Test
    public void shouldSetAndUnsetProperSessionsForDistinctDaosUsingDistinctContexts() {
        AbstractDaoEnricher enricher = new AbstractDaoEnricher( txManager );

        ShardContext shardContext = new ShardContext( "testClient" );
        ShardContext shardContext1 = new ShardContext( "testClient1" );
        ShardContext shardContext2 = new ShardContext( "testClient2" );

        SessionFactory mockSessionFactory = EasyMock.createMock( SessionFactory.class );
        Session mockSession = EasyMock.createMock( "mockSession", Session.class );
        Session mockSession1 = EasyMock.createMock( "mockSession1", Session.class );
        Session mockSession2 = EasyMock.createMock( "mockSession2", Session.class );
        AbstractDao mockDao = EasyMock.createMock( "mockDao", AbstractDao.class );
        DepartmentDaoImpl mockDao1 = EasyMock.createMock( "mockDao1", DepartmentDaoImpl.class );
        PersonDaoImpl mockDao2 = EasyMock.createMock( "mockDao2", PersonDaoImpl.class );

        // called during statistics logging
        expect( mockSession2.getSessionFactory( ) ).andReturn( mockSessionFactory );
        expect( mockSession1.getSessionFactory( ) ).andReturn( mockSessionFactory );
        expect( mockSession.getSessionFactory( ) ).andReturn( mockSessionFactory );
        expect( mockSessionFactory.getStatistics( ) ).andReturn( null ).times( 3 );
        // called during statistics logging

        expect( txManager.getSessionFactory( ) ).andReturn( mockSessionFactory ).times( 3 );
        expect( mockSessionFactory.getCurrentSession( ) ).andReturn( mockSession );
        expect( mockSessionFactory.getCurrentSession( ) ).andReturn( mockSession1 );
        expect( mockSessionFactory.getCurrentSession( ) ).andReturn( mockSession2 );
        // setups
        mockDao.setSession( mockSession );
        mockDao1.setSession( mockSession );
        mockDao2.setSession( mockSession1 );
        mockDao1.setSession( mockSession2 );
        // tearDowns
        mockDao1.setSession( mockSession );
        mockDao2.setSession( null );
        mockDao1.setSession( null );
        mockDao.setSession( null );

        replay( txManager, mockSessionFactory, mockSession, mockSession1, mockSession2, mockDao, mockDao1, mockDao2 );

        enricher.setup( mockDao, shardContext );
        enricher.setup( mockDao1, shardContext );
        enricher.setup( mockDao2, shardContext1 );
        enricher.setup( mockDao1, shardContext2 );

        enricher.tearDown( mockDao1, shardContext2 );
        enricher.tearDown( mockDao2, shardContext1 );
        enricher.tearDown( mockDao1, shardContext );
        enricher.tearDown( mockDao, shardContext );

        verify( txManager, mockSessionFactory, mockSession, mockSession1, mockSession2, mockDao, mockDao1, mockDao2 );
    }

}
