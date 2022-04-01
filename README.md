# A Stock Exchange Machine
A multithreaded stock exchange server implemented in Java and PostgreSQL.

## Files
**Warning:** to run the unit tests, create a PostgreSQL server at localhost on port `5432`, a user `postgres` with password `passw0rd`, and a database named `ece568_hw4`.

- [**application code**](app/server/src/main/java/edu/duke/ece568/server/)
- [**unit tests**](app/server/src/test/java/edu/duke/ece568/server/)

## Deployment
1. install docker
```
sudo apt install -y docker docker-compose
```
2. build and deploy the application stack
```
sudo docker-compose build
sudo docker-compose up -d
```

## Debugging with Telnet
1. create a Telnet connection to the server, check if the connection is successful. A successful connection would display a blank screen.
```
telnet <hostname> 12345 
```
2. type in the request XML as a single string and send to the server, e.g.
```
123<?xml version="1.0" encoding="ISO-8859-1"?><create><account id="12714" balance="98349213"/></create>
```
3. check if the server responds with an expected response, e.g.
```
<results><created balance="9.8349213E7" id="12714"/></results>
```

