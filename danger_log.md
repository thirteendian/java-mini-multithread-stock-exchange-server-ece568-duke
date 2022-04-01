# Danger Log

## Overall Architecture
1. For simplicity, the application stack consists of 2 containers, the server and the database without a load balancer, which would be recommended to handle public traffic. 
1. The server is implemented in Java, which has significant overhead when deploying the application stack. 
1. The database container exposes the port to the docker host, leaving it accessible to all applications on the host which may pose security risk. A docker private network is advised. 

## Input Handling
1. For each request, the entire xml must be sent. The server does not support segmented inputs.
1. The length of the xml, which is presented before the xml body is ignore, thanks to Java IO streams. This, however, means that an invalid length indicator is also allowed. e.g. `asdf<create>....` would be valid.

## Server-Client Connections
1. With each client connecting to the server, the connection does not drop upon each request's fulfilment. The client can keep sending requests until the connection is actively terminate (e.g. client shuts down the terminal).

## Server-Database Connections
1. With each client connecting to the server, a dedicated connection JDBC connection is established between the server and the database until the client exits.
1. JDBC is thread-safe. Essentially, only one client could modify the database at a time, which guarentees database level thread safety.  

## Application
1. Each time a client requests to access the database, a transaction is used to ensure no interleaving. This is implemented using JDBC's `autocommit` and `rollback` mechanism, which guarentees application level thread safety. 
1. A custom ORM is implemented to interact between the application and the database. To avoid stale server-side data, each time an operation is performed, the ORM would sync with the database first before proceeding. 
1. The xml parser is tightly coupled with the ORM API, which may lead to maintenance overhead when new request types are added. 

## Database
1. The database container is using the default Postgresql database and login credentials, which may pose a security risk to the application.
1. The field `account_number` in table `account` is of type `int`. Thus, `0001` and `01` would be intepreted by the database as the same value. 