package com.dbses.open.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

/**
 * @author yanglulu
 * @date 2022/1/20
 */
public class MultiProfilesYamlPropertiesFactoryBean extends YamlPropertiesFactoryBean {

    private ConfigurableEnvironment environment;

    public MultiProfilesYamlPropertiesFactoryBean(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @Override
    protected Properties createProperties() {
        final Properties result = new Properties();
        process((properties, map) -> {
            if (result.isEmpty()) {
                result.putAll(properties);
            } else {
                for (String profile : environment.getActiveProfiles()) {
                    if (profile.equals(properties.get("spring.profiles"))) {
                        result.putAll(properties);
                    }
                }
            }
        });
        return result;
    }

}
