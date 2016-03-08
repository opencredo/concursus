package com.opencredo.concourse.spring.demo.spring;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("kafka")
@ComponentScan("com.opencredo.concourse.spring.demo.kafka")
public class KafkaConfiguration {

}
