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


