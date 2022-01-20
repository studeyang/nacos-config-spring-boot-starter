# Why do this?

## Description

Yaml does not support multiple profiles.

When Nacos configuration is:

```yaml
test1:
  config: 2

---
spring:
  profiles: alpha
test1:
  config: alpha

---
spring:
  profiles: beta
test1:
  config: beta
```

And I set `active profiles=alpha` and get property named `test1.config`, `nacos-spring-boot` gave me the result `beta`.

## what I expected

I expect  the result is `alpha`.

See: https://github.com/nacos-group/nacos-spring-boot-project/issues/226

# Usage

````java
@PrepareConfigurations({"common_database.yml", "common_eureka.yml"})
public class WebApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(WebApplication.class, args);
        context.getEnvironment();
    }

}
````