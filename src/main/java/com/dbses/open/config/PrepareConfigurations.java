package com.dbses.open.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author yanglulu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(ConfigurationLoader.class)
public @interface PrepareConfigurations {

	String[] value() default {};
}
