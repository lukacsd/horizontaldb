## Horizontal database scaling with Spring and Hibernate

I faced with the following problem some time ago:

_..let's have a separate database per user in our web application!_

After some thinking and extensive googling, which provided a lot of
clues, this is the prototype I have came up with. I tried to grasp
some of the aspects involved in the problem, hope it helps someone one
day solving a similar task.

I could not find any clear-cut solutions to the problem to start with,
apart from the [Hibernate Shards](http://hibernate.org/others/) project,
which if would not have been abandoned, probably I would not have done
this project in the first place.

Eventually, the most viable way turned out to be Hibernate multi-tenancy;
this is a nice, low level feature in the framework, by which I mean,
this project shows one way to make use of it in an application.

### Disclaimer

I called a lot of classes _ShardThis_ and _ShardThat_, because _shard_
seemed to be the prevalent nomenclature in this domain.

I am heavily relying on Spring for dependency injection and AOP proxies.
Caching is handled by Ehcache. For a DataSource implementation, I have
used Tomcat's pooled DataSource library.

Unfortunately, I could not manage to create a solution which is 
entirely un-intrusive. Since dao's are supposed to behave differently
depending on the context they are used in, this dynamic nature has to
be honoured somehow in the layers accessing the data. I will 
ellaborate this a bit further in the _Code Utilisation_ section.

### Basic Concepts

This project is supposed to be the _model_ stem of an application.

A _conversation_ is a sort of session that holds resources related to 
the client for a given period of time. A resource would typically be 
a database connection.

The unit of a shard is the _client_ - where the implementation could be
a uniqe database or schema. Slicing tables further adds complexity
beyond the scope of this project.

### Code Utilisation

My idea to use a model object in the service layer, was something like 
this:

_As a first step, a user has to signed in somewhere in the service.._

```java
@PersistentConversationSetup
public void authenticate( UserToken userToken ) {
	// do login stuff
}
```

_From that point on, the client can use the service, while the connection
is directed to the proper data source behind the scenes.._

```java
@Transactional( readOnly = true )
@ShardBeans( { @ShardBean( FooDao.class ), @ShardBean( BarDao.class ) } )
public Long getCountOfExamples( ShardContext shardContext ) {
	FooDao foo = shardContext.getBean( FooDao.class );
	BarDao bar = shardContext.getBean( BarDao.class );

	// do stuff with dao as usual

	return retval;
}
```

_After all the work's been done, the user is logged off.._

```java
@PersistentConversationTeardown
public void logoff( UserToken userToken ) {
	// do logout stuff
}
```

The _shardContext_ instance is created with the appropriate clientId, 
eg new ShardContext( "joe" ); given that _joe_ has already been 
authenticated succesfully by the _authenticate_ method. Once the 
execution is inside the method, all the needed beans are setup and 
accessible through the context. The ShardContext instances are 
throw-away objects.

### Building From Source

The project is delivered with a couple of test entities and dao's,
and the whole procedure can be observed by executing the unit tests.

`mvn clean cobertura:cobertura package`

### License

All code is released under [Apache License v2](http://www.apache.org/licenses/LICENSE-2.0.html).
