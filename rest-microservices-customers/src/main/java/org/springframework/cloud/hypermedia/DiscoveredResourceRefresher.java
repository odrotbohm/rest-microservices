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

import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.config.ContextLifecycleScheduledTaskRegistrar;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * A {@link ScheduledTaskRegistrar} that verifies all {@link DiscoveredResource} instances in the system based on the
 * given timing configuration.
 * 
 * @author Oliver Gierke
 */
@RequiredArgsConstructor
public class DiscoveredResourceRefresher extends ContextLifecycleScheduledTaskRegistrar {

	private final int fixedDelay, initialDelay;

	private @Setter(onMethod = @__(@Autowired) ) List<DiscoveredResource> discoveredResources;

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.scheduling.config.ContextLifecycleScheduledTaskRegistrar#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {

		discoveredResources.forEach(resource -> {
			addFixedDelayTask(new IntervalTask(() -> resource.verify(), fixedDelay, initialDelay));
		});

		super.afterPropertiesSet();
	}
}
