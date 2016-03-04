package com.opencredo.concourse.spring.demo.spring;

import com.opencredo.concourse.domain.events.cataloguing.AggregateCatalogue;
import com.opencredo.concourse.domain.events.cataloguing.InMemoryAggregateCatalogue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class InMemoryConfiguration {

    private final InMemoryAggregateCatalogue inMemoryAggregateCatalogue = new InMemoryAggregateCatalogue();

    @Bean
    public AggregateCatalogue aggregateCatalogue() {
        return inMemoryAggregateCatalogue;
    }
}
