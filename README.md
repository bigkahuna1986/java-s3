Java S3 Server/API
====

The S3 server API is compiled using Java 11

## The problem

There are a number of Java S3 server implementations but no API that is ready for a custom implementation.

## The solution

This project has the goal of creating a simple to use API for developers creating custom backends that is also relatively stateless. Creating 
an entire AWS S3 replacement is NOT a goal of this project, just to allow simple S3 backends. A default implementation is included, but it is MEMORY
only. There is no persistent storage.

## Project breakdown

s3-model: A basic model of data structures found in S3.
server-api: Most users will probably implement the interfaces found here. This project creates the Java APIs necessary for an S3 server to work.
s3-memory-impl: A testable MEMORY ONLY implementation of an S3 backend.
server-impl: A basic Jetty/Jersey server.
launch: Simple server for unit testing.

There is no http api for managing owners or users. This will have to be done programmatically! Managing owners and users is beyond the scope of this project.

To use this framework you need to implement 2 different interfaces:

```java
net.jolivier.s3api.S3DataStore
net.jolivier.s3api.S3AuthStore
```

provided by the s3-model project.
Example

```java
class MyDataStore implements S3DataStore {
   ... implemented methods ...
}
```

And plugin your implementation to 

```java
net.jolivier.s3api.http.ApiPoint

ApiPoint.configure(myDataStoreImpl, myAuthStoreImpl)
```

before starting the server. Examples of how to bind the s3 server are in the launch project.

## Gradle/Maven
This project is now published through Maven Central.
Pull instructions for gradle:
```gradle
implementaion "net.jolivier:s3-model:0.4"
```

or maven
```maven
    <dependency>
      <groupId>net.jolivier</groupId>
      <artifactId>s3-model</artifactId>
      <version>0.4</version>
    </dependency>
```


## Other Considerations

All requests are handled by Jersey, but you shouldn't need a familiarity with it to create the implementation.
