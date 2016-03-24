package com.opencredo.concourse.demos.game;

import com.opencredo.concourse.demos.game.states.GameState;
import com.opencredo.concourse.demos.game.states.PlayerState;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.state.StateRepository;
import com.opencredo.concourse.mapping.events.methods.state.DispatchingStateRepository;
import com.opencredo.concourse.spring.commands.CommandSystemBeans;
import com.opencredo.concourse.spring.events.EventSystemBeans;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@ComponentScan(basePackageClasses = {
        EventSystemBeans.class,
        CommandSystemBeans.class,
        Application.class })
@EnableWebMvc
@EnableAutoConfiguration
@Configuration
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public StateRepository<GameState> gameStateRepository(EventSource eventSource) {
        return DispatchingStateRepository.using(eventSource, GameState.class);
    }

    @Bean
    public StateRepository<PlayerState> playerStateRepository(EventSource eventSource) {
        return DispatchingStateRepository.using(eventSource, PlayerState.class);
    }
}
