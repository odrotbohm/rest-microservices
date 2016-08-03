/*
 * Copyright 2014-2016 the original author or authors.
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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import example.customers.Customer.Address;
import example.customers.Customer.Address.Location;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration tests for {@link CustomerRepository}.
 * 
 * @author Oliver Gierke
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class CustomerRepositoryIntegrationTest {

	@Autowired CustomerRepository repository;

	@Test
	public void savesAndFindsCustomer() {

		Customer customer = repository.save(new Customer("Dave", "Matthews",
				new Address("street", "zipCode", "city", new Location(55.349451, -131.673817))));

		assertThat(repository.findOne(customer.getId()), is(customer));
	}
}
