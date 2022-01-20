package com.dbses.open.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author yanglulu
 */
@Configuration
public class ConfigurationLoader implements ImportSelector, EnvironmentAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationLoader.class);

    private ConfigurableEnvironment environment;

    @Override
    public void setEnvironment(Environment environment) {
        Assert.isInstanceOf(ConfigurableEnvironment.class, environment);
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        try {
            prepareConfigFromNacos(importingClassMetadata);
        } catch (NacosException e) {
            e.printStackTrace();
        }
        return new String[]{};
    }

    private void prepareConfigFromNacos(AnnotationMetadata importingClassMetadata) throws NacosException {
        // 初始化 nacos 连接
        String serverAddr = environment.getProperty("nacos.config.server-addr");
        String namespace = environment.getProperty("nacos.config.namespace");
        String username = environment.getProperty("nacos.config.username");
        String password = environment.getProperty("nacos.config.password");
        if (serverAddr == null || namespace == null) {
            LOGGER.warn("未识别到Nacos配置, nacos.config.server-addr={}, nacos.config.namespace={}",
                    serverAddr, namespace);
            return;
        }
        Properties nacosConnectProperties = new Properties();
        if (StringUtils.isNotEmpty(username)) {
            nacosConnectProperties.put("username", username);
        }
        if (StringUtils.isNotEmpty(password)) {
            nacosConnectProperties.put("password", password);
        }
        nacosConnectProperties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
        nacosConnectProperties.put(PropertyKeyConst.NAMESPACE, namespace);
        ConfigService configService = NacosFactory.createConfigService(nacosConnectProperties);

        // 加载公共配置
        Map<String, Object> attrs = importingClassMetadata.getAnnotationAttributes(PrepareConfigurations.class.getName());
        String[] names = (String[]) attrs.get("value");
        for (String configName : names) {
            String content = configService.getConfig(configName, "commons", 3000);
            if (content == null || content.isEmpty()) {
                LOGGER.error("{} 读取失败", configName);
                continue;
            }

            YamlPropertiesFactoryBean yamlFactory = new MultiProfilesYamlPropertiesFactoryBean(environment);
            yamlFactory.setResources(new ByteArrayResource(content.getBytes()));
            Properties commonsProperties = yamlFactory.getObject();
            PropertySource<?> propertySource = new MapPropertySource(configName, propertiesToMap(commonsProperties));
            environment.getPropertySources().addLast(propertySource);
            LOGGER.info("{} 附加完成[{}]", configName, "Nacos");
        }
    }

    private Map<String, Object> propertiesToMap(Properties properties) {
        Map<String, Object> result = new HashMap<>(16);
        Enumeration<String> keys = (Enumeration<String>) properties.propertyNames();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            String value = properties.getProperty(key);
            if (value != null) {
                result.put(key, value.trim());
            } else {
                result.put(key, null);
            }
        }
        return result;
    }

}
