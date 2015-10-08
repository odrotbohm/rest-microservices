/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.cloud.hypermedia;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

/**
 * A REST resource that is defined by a service reference and a traversal operation within that service.
 *
 * @author Oliver Gierke
 */
@Slf4j
@RequiredArgsConstructor
public class DiscoveredResource {

	private final ServiceInstanceProvider provider;
	private final RestOperations restOperations = new RestTemplate();
	private final TraversalDefinition traversal;

	private @Getter Optional<Link> link = Optional.empty();

	/**
	 * Verifies the link to the current
	 */
	public void verify() {
		this.link = link.map(it -> verify(it)).orElseGet(() -> discoverLink());
	}

	/**
	 * Verifies the given {@link Link} by issuing an HTTP HEAD request to the resource.
	 * 
	 * @param link must not be {@literal null}.
	 * @return
	 */
	private Optional<Link> verify(Link link) {

		Assert.notNull(link, "Link must not be null!");

		try {

			String uri = link.expand().getHref();

			log.debug("Verifying link pointing to {}…", uri);
			restOperations.headForHeaders(uri);
			log.debug("Successfully verified link!");

			return Optional.of(link);

		} catch (RestClientException o_O) {

			log.debug("Verification failed, marking as outdated!");
			return Optional.empty();
		}
	}

	private Optional<Link> discoverLink() {

		try {

			Optional<ServiceInstance> service = provider.getServiceInstance();

			return service.map(ServiceInstance::getUri).map(storesUri -> {

				log.debug("Discovered {} system at {}. Discovering resource…", service.map(ServiceInstance::getServiceId),
						storesUri);

				Traverson traverson = new Traverson(storesUri, MediaTypes.HAL_JSON);
				Link link = traversal.buildTraversal(traverson).asTemplatedLink();

				log.debug("Found link pointing to {}.", link.getHref());

				return Optional.of(link);

			}).orElse(Optional.empty());

		} catch (RuntimeException o_O) {

			this.link = Optional.empty();
			log.debug("Target system unavailable. Got: ", o_O.getMessage());
		}

		return Optional.empty();
	}
}
