# SPRING-CLOUD-AUTOSCALING_MICROSERVICES
## How Does It Work?
Every Spring Boot application, which contains the Spring Boot Actuator library can expose metrics under the endpoint /actuator/metrics.
There are many valuable metrics that gives you the detailed information about an application status.
Some of them may be especially important when talking about autoscaling: JVM, CPU metrics, a number of running threads and a number of incoming HTTP requests.
There is dedicated Jenkins pipeline responsible for monitoring application's metrics by polling endpoint /actuator/metrics periodically.
If any monitored metrics is below or above target range it runs new instance or shut down a running instance of the application using another Actuator endpoint, /actuator/shutdown. 
Before that, it needs to fetch the current list of running instances of a single application in order to get an address of existing application selected for shutting down or the address of the server with the smallest number of running instances for a new instance of application.

## ARCHITECTURE
![image](https://user-images.githubusercontent.com/35865592/164477691-d790b5c6-bd61-47ae-b353-cf207061a854.png)

Our application needs to meet some requirements: it has to expose metrics and endpoint for graceful shutdown, it needs to register in Eureka after startup and deregister on shutdown, and finally, it also should dynamically allocate running port randomly from the pool of free ports. 
## DYNAMIC PORT ALLOCATION
Since it is possible to run many instances of an application on a single machine, we have to guarantee that there won't be conflicts in port numbers. Fortunately, Spring Boot provides such mechanisms for an application. We just need to set the port number to 0 inside application.yml file using the server.port property. Because our application registers itself in eureka it also needs to send unique instanceId, which is by default generated as a concatenation of fields spring.cloud.client.hostname, spring.application.name and server.port.
Here's current configuration of our sample application. I have changed the template of the instanceId field by replacing the number of the port to a randomly generated number.
``` config
spring:
  application:
    name: example-service
server:
  port: ${PORT:0}
eureka:
  instance:
    instanceId: ${spring.cloud.client.hostname}:${spring.application.name}:${random.int[1,999999]}
 
 ```
 
## Integrating Jenkins Pipeline With Eureka
The first stage of our pipeline is responsible for fetching a list of services registered on the service discovery server. Eureka exposes HTTP API with several endpoints. One of them is GET /eureka/apps/{serviceName}, which returns a list of all instances of application with given name. We are saving the number of running instances and the URL of metrics endpoint of every single instance. These values would be accessed during the next stages of the pipeline.

## Integrating Jenkins Pipeline With Spring Boot Actuator Metrics
Spring Boot Actuator exposes endpoint with metrics, which allows us to find the metric by name and optionally by tag. In the fragment of pipeline visible below, I'm trying to find the instance with metric below or above a defined threshold. If there is such an instance we stop the loop in order to proceed to the next stage, which performs scaling down or up. The IP addresses of running applications are taken from pipeline environment variable with prefix INSTANCE_, which has been saved in the previous stage..



