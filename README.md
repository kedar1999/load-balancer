## Introduction
This is the service which works as a load balancer

## Built with
- Java 17
- Spring Boot (3.2.5)

# Getting Started
## Building the JAR
- You can directly build the jar with `mvn clean install` as this project does not depend on any other project to build.

# App Server Details
- For the start, application server details are configured from the application.yml file. 
- App servers can be registered, removed using the rest-apis. (This apis are present in AppServerManagerController)
- All the apis apart from the registering/removing app servers are reverse proxied to suitable app server. (Using routing algorithm)
- Algorithm used for forwarding request to specific app server is configurable from application.yml

# High Level flow
- Client will make a call to load-balancer service. The load balancer will forward the request with the database system and routing algorithm.
- Caching also can be used for at the load balancer server level.
- The forwarded request will reach the app server. Load balancer will send back the app server's response back to client.
![HLD-FLOW](https://github.com/kedar1999/load-balancer/assets/56604563/fac7262d-72b3-48da-805f-ddf4fb6778cd)


# Low Level flow
- When a client makes a http call to load balancer, the request is handled by reverseProxyService.
- The routing algorithm comes into picture and provides the app server to which the request needs to be forwarded.
- At every api server connection start and connection termination we call change the app server to connection mapping.
- [WebClient](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html) makes an http call to app server provided by the `RequestForwardDecider`, and responds back to the client with the received response from the app server.
![LLD-FLOW](https://github.com/kedar1999/load-balancer/assets/56604563/5682ef48-f685-4c30-a223-aec3e022a60d)

# Guide to use load-balancer
- Get all api servers available

  curl --location 'localhost:8080/load-balancer/app-server'

- Register api server

  curl --location 'localhost:8080/load-balancer/app-server/register' \
  --header 'Content-Type: application/json' \
  --data '{
  "url": "url3",
  "healthCheckUrl": "url3/health"
  }'

- Remove api server

  curl --location --request DELETE 'localhost:8080/app-server/remove' \
  --header 'Content-Type: application/json' \
  --data '{
  "url": "url3"
  }'

- Api to the app server from client/Reverse proxy api

  curl --location 'localhost:8080/health'
