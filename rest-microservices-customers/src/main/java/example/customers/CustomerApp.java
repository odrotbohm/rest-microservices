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
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Oliver Gierke
 */
@SpringBootApplication
@EnableScheduling
@EnableDiscoveryClient
public class CustomerApp {

	public static void main(String[] args) {
		SpringApplication.run(CustomerApp.class, args);
	}

	@Autowired CustomerRepository customers;

	@PostConstruct
	public void init() {

		Customer customer = new Customer("Oliver", "Gierke");
		customer.address = new Address("625 Avenue of the Americas", "10011", "New York", new Location(40.740337,
				-73.995146));

		customers.save(customer);
	}
}
