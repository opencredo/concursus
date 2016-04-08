package com.opencredo.concursus.spring.demo.spring;

import com.opencredo.concursus.domain.events.sourcing.EventSource;
import com.opencredo.concursus.domain.events.state.StateRepository;
import com.opencredo.concursus.mapping.events.methods.state.DispatchingStateRepository;
import com.opencredo.concursus.spring.demo.repositories.GroupState;
import com.opencredo.concursus.spring.demo.repositories.UserState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfiguration {

    @Bean
    public StateRepository<GroupState> groupStateRepository(EventSource eventSource) {
        return DispatchingStateRepository.using(eventSource, GroupState.class);
    }

    @Bean
    public StateRepository<UserState> userStateRepository(EventSource eventSource) {
        return DispatchingStateRepository.using(eventSource, UserState.class);
    }
}
