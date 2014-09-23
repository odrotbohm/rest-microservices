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
package example.customers.integration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.hateoas.*;
import org.springframework.hateoas.client.Traverson;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * @author Oliver Gierke
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class StoreIntegration {

	private final Environment env;

	private @Getter Link storesByLocationLink;

	@Scheduled(fixedDelay = 5000)
	public void checkStoresAvailability() {

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
			URI storesUri = URI.create(env.getProperty("integration.stores.uri"));
			log.info("Trying to access the stores system at {}…", storesUri);

			Traverson traverson = new Traverson(storesUri, MediaTypes.HAL_JSON);
			final Link linkWithoutTemplateVars = traverson.follow("stores", "search", "by-location").asLink();
            
            // TODO find a way to retrieve the variable names from the JSON response!
            final TemplateVariable locationVar = new TemplateVariable("location", TemplateVariable.VariableType.REQUEST_PARAM);
            final TemplateVariable distanceVar = new TemplateVariable("distance", TemplateVariable.VariableType.REQUEST_PARAM);
            final TemplateVariables variables = new TemplateVariables(locationVar, distanceVar);
            this.storesByLocationLink = new Link(new UriTemplate(linkWithoutTemplateVars.getHref(), variables), linkWithoutTemplateVars.getRel());


            log.info("Found stores-by-location link pointing to {}.", linkWithoutTemplateVars.getHref());

		} catch (RuntimeException o_O) {
			this.storesByLocationLink = null;
			log.info("Stores system unavailable. Got: ", o_O.getMessage());
		}
	}

	public boolean isStoresAvailable() {
		return storesByLocationLink != null;
	}
}
