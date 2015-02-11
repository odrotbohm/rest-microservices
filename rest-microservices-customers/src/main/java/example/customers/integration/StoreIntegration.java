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
package example.customers.integration;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * @author Oliver Gierke
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
class StoreIntegration implements ApplicationListener<InstanceRegisteredEvent> {

	private final @NonNull DiscoveryClient client;

	private @Getter Link storesByLocationLink;

	private AtomicBoolean discoveryAvailable = new AtomicBoolean(false);

	@Override
	public void onApplicationEvent(InstanceRegisteredEvent event) {
		discoveryAvailable.compareAndSet(false, true);
	}

	@Scheduled(fixedDelay = 5000)
	public void checkStoresAvailability() {
		if (!discoveryAvailable.get()) return;
		if (storesByLocationLink != null) {
			verify(storesByLocationLink);
		} else {
			discoverByLocationLink();
		}
	}

	private void verify(Link link) {

		try {
			log.info("Verifying stores-nearby link pointing to {}…", storesByLocationLink);
			new RestTemplate().headForHeaders(link.expand().getHref());
			log.info("Successfully verified link!");
		} catch (RestClientException o_O) {

			log.info("Verification failed, marking as outdated!");
			this.storesByLocationLink = null;
		}
	}

	private void discoverByLocationLink() {

		try {

			discoverStoreService().ifPresent(storesUri -> {

				log.info("Discovered stores system at {}. Discovering by-location resource…", storesUri);

				Traverson traverson = new Traverson(URI.create(storesUri), MediaTypes.HAL_JSON);
				this.storesByLocationLink = traverson.follow("stores", "search", "by-location").asLink();

				log.info("Found stores-by-location link pointing to {}.", storesByLocationLink.getHref());
			});

		} catch (RuntimeException o_O) {
			this.storesByLocationLink = null;
			log.info("Stores system unavailable. Got: "+ o_O.getMessage(), o_O);
		}
	}

	private Optional<String> discoverStoreService() {

		List<ServiceInstance> stores = client.getInstances("stores");
		return stores.stream().findFirst()
				.map(instance -> String.format("http://%s:%s", instance.getHost(), instance.getPort()));
	}

	public boolean isStoresAvailable() {
		return storesByLocationLink != null;
	}
}
