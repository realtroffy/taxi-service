package com.modsen.rideservice.integration.annotation;

import com.modsen.rideservice.integration.testenvironment.IntegrationTestEnvironment;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.springframework.test.context.TestConstructor.AutowireMode.ALL;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ActiveProfiles("test")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = IntegrationTestEnvironment.TestConfig.class)
@TestConstructor(autowireMode = ALL)
public @interface IntegrationTests {}
