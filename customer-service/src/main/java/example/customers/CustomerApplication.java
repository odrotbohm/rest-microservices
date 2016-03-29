/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package example.customers;

import example.customers.Customer.Address;
import example.customers.Customer.Address.Location;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.hypermedia.DiscoveredResource;
import org.springframework.cloud.client.hypermedia.DynamicServiceInstanceProvider;
import org.springframework.cloud.client.hypermedia.ServiceInstanceProvider;
import org.springframework.cloud.client.hypermedia.StaticServiceInstanceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * Spring Boot application bootstrap class to run a customer service and configure integration with a remote system that
 * exposes a REST resource to lookup stores by location.
 * 
 * @author Oliver Gierke
 */
@SpringBootApplication
@EnableCircuitBreaker
public class CustomerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomerApplication.class, args);
	}

	/**
	 * A remote {@link DiscoveredResource} that provides functionality to lookup stores by location.
	 * 
	 * @param provider
	 * @return
	 */
	@Bean
	public DiscoveredResource storesByLocationResource(ServiceInstanceProvider provider) {
		return new DiscoveredResource(provider, traverson -> traverson.follow("stores", "search", "by-location"));
	}

	/**
	 * A simple default {@link ServiceInstanceProvider} to use a hard-coded remote service to detect the store locations.
	 * 
	 * @return
	 */
	@Bean
	@Profile("default")
	public StaticServiceInstanceProvider staticServiceInstanceProvider() {
		return new StaticServiceInstanceProvider(new DefaultServiceInstance("stores", "localhost", 8081, false));
	}

	// Cloud configuration

	/**
	 * Dedicated configuration to rather use a {@link ServiceInstanceProvider} to lookup the remote service via a service
	 * registry.
	 *
	 * @author Oliver Gierke
	 */
	@Profile("cloud")
	@EnableDiscoveryClient
	static class CloudConfiguration {

		@Bean
		public DynamicServiceInstanceProvider dynamicServiceProvider(DiscoveryClient client) {
			return new DynamicServiceInstanceProvider(client, "stores");
		}
	}

	// Data setup

	@Autowired CustomerRepository customers;

	@PostConstruct
	public void init() {

		Customer customer = new Customer("Oliver", "Gierke",
				new Address("625 Avenue of the Americas", "10011", "New York", new Location(40.740337, -73.995146)));

		customers.save(customer);
	}
}
