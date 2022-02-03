Java S3 Server/API

The goal of this project is to provide a more modular S3 api for java applications.

S3 objects/xml structures are stored in the s3-model sub project, the jersey API is in server-api. 
Any application can implement the S3DataStore and S3AuthStore interfaces and attach via ApiPoint. 
The rest can be started as a jetty/jersey server to handle any requests.

This is not intended to be a AWS/Ceph replacement, more just a testing tool.


