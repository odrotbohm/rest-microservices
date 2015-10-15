# REST based micro-services sample

[![Build Status](https://travis-ci.org/olivergierke/rest-microservices.svg?branch=master)](https://travis-ci.org/olivergierke/rest-microservices)

## tl;dr

- Two Spring Boot based Maven projects that are standalone applications:
  - Stores (MongoDB, exposing a few Starbucks shops across north america, geo-spatial functionality)
  - Customers (JPA)
- The customers application tries to discover a search-by-location-resource and periodically verifying it's still available (see `StoreIntegration`).
- If the remote system is found the customers app includes a link to let clients follow to the remote system and thus find stores near the customer.

## Fundamentals

The core domain and focus of the example can be explored by simply starting both the customer and the store service using `mvn spring-boot:run`. The store service exposes a resource to trigger geo-spatial queries for Starbucks shops given a reference location and distance.

## The customer service

The customer service has a static reference to the store service configured (http://localhost:8081, as bean in `CustomerApplication.staticServiceInstanceProvider()`) and traverses a set of hypermedia links to discover the resource solely by knowing the relation names. The service then adds a link to the discovered system, expanding the link with the current location of the customer (in `CustomerResourceProcessor.process(…)`). This result of that can be seen by follwing the `customers` relation in the root resource of the customer service. A `stores-nearby` link shows up.

As the store system might become unavailable, we verify the presence of it by issuing a HEAD requests to the discovered resource (time interval configured by the `cloud.hypermedia.refresh.fixed-delay` property in `application.properties`). This is implemented by the `DiscoveredResourceRefresher` auto-configured by `CloudHypermediaAutoConfiguration`, a tiny Spring Boot extension.

## The use of Hystrix

The customer service uses Hystrix to short-circuit the discovery calls trying to find the store system if the link discovery or validation fails repeatedly. To see this working run the `hystrix-dashboard` app (`mvn spring-boot:run`), browse to http://localhost:7979/hystrix and point the dashboard to the customer service's Hystrix stream (http://localhost:8080/hystrix.stream).

While the store service is running, you should see the requests being forwarded, the circuit closed. Stop the store service and see how the failing requests will trigger the circuit to be opened at some point (a couple of seconds usually). Restart the store service and see how after a couple of seconds the circuit gets closed again, the resource discovery is re-triggered and the `stores-nearby` link appears in the resources the customer service exposes.

## Using service discovery

As an alternative to the static service reference the customer service uses by default, service discovery via Eureka can be used. Make sure both the customer and store service are stopped. Start the `eureka-server` application using `mvn spring-boot:run`. Browse `http://localhost:8761` to see the Eureka web interface.

Now start both the customer and the store service with the `cloud` profile enabled (`mvn spring-boot:run -Dspring.profiles.active="cloud"`). Inspecting the Eureka web interface you should see both instances being registered with the registry. The customer service now uses a `DiscoveryClient` to obtain a `ServiceInstance` by name (see the `CustomerApplication.CloudConfig.dynamicServiceProvider(…)` bean definition).
