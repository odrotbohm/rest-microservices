/*
 * Copyright 2014 the original author or authors.
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

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.hypermedia.DiscoveredResource;
import org.springframework.cloud.hypermedia.DynamicServiceInstanceProvider;
import org.springframework.cloud.hypermedia.ServiceInstanceProvider;
import org.springframework.cloud.hypermedia.StaticServiceInstanceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Oliver Gierke
 */
@SpringBootApplication
@EnableScheduling
public class CustomerApp {

	public static void main(String[] args) {
		SpringApplication.run(CustomerApp.class, args);
	}

	@Bean
	@Profile("default")
	public StaticServiceInstanceProvider staticServiceInstanceProvider() {
		return new StaticServiceInstanceProvider(new DefaultServiceInstance("stores", "localhost", 8081, false));
	}

	@Bean
	public DiscoveredResource storesByLocationResource(ServiceInstanceProvider provider) {
		return new DiscoveredResource(provider, traverson -> traverson.follow("stores", "search", "by-location"));
	}

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

		Customer customer = new Customer("Oliver", "Gierke");
		customer.setAddress(
				new Address("625 Avenue of the Americas", "10011", "New York", new Location(40.740337, -73.995146)));

		customers.save(customer);
	}
}
