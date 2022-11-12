Java S3 Server/API
====

The S3 server API is compiled using Java 11

## The problem

There are a number of Java S3 server implementations but no API that is ready for a custom implementation.

## The solution

This project has the goal of creating a simple to use API for developers creating custom backends that is also relatively stateless.
No code is executing past the life of the web request.
S3 objects/xml are modeled in the s3-model project. The actual server API is in server-api. 


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
```

before starting the server. Examples of how to bind the s3 server are in the launch project.

## Gradle/Maven
This project is now published through Maven Central.
Pull instructions for gradle:
```gradle
implementaion "net.jolivier:s3-model:0.3"
```

or maven
```maven
    <dependency>
      <groupId>net.jolivier</groupId>
      <artifactId>s3-model</artifactId>
      <version>0.3</version>
    </dependency>
```


## Other Considerations

All requests are handled by Jersey, but you shouldn't need a familiarity with it to create the implementation.
