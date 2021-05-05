package com.flow.service.config;

import com.flow.service.security.CipherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bootstrap.BootstrapApplicationListener;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import javax.validation.ValidationException;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ApplicationContextConfig implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationContextConfig.class);

    public static final String DECRYPTED_PROPERTY_SOURCE_NAME = "decrypted";

    private final CipherService cipherService;

    public ApplicationContextConfig(CipherService cipherService) {
        this.cipherService = cipherService;
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        MapPropertySource decrypted = new MapPropertySource(
                DECRYPTED_PROPERTY_SOURCE_NAME, decrypt(environment.getPropertySources()));
        ApplicationContext parent = applicationContext.getParent();
        if (parent != null && (parent.getEnvironment() instanceof ConfigurableEnvironment)) {
            ConfigurableEnvironment mutable = (ConfigurableEnvironment) parent
                    .getEnvironment();
            // The parent is actually the bootstrap context, and it is fully
            // initialized, so we can fire an EnvironmentChangeEvent there to rebind
            // @ConfigurationProperties, in case they were encrypted.
            insert(mutable.getPropertySources(), decrypted);
            parent.publishEvent(new EnvironmentChangeEvent(decrypted.getSource().keySet()));
        }
    }

    private void insert(MutablePropertySources propertySources,
                        MapPropertySource propertySource) {
        if (propertySources.contains(BootstrapApplicationListener.BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
            propertySources.addAfter(BootstrapApplicationListener.BOOTSTRAP_PROPERTY_SOURCE_NAME, propertySource);
        } else {
            propertySources.addFirst(propertySource);
        }
    }

    public Map<String, Object> decrypt(MutablePropertySources propertySources) {
        Map<String, Object> overrides = new LinkedHashMap<>();
        for (PropertySource<?> source : propertySources) {
            decrypt(source, overrides);
        }
        return overrides;
    }

    private void decrypt(PropertySource<?> source, Map<String, Object> overrides) {
        if (source instanceof EnumerablePropertySource) {
            EnumerablePropertySource<?> enumerable = (EnumerablePropertySource<?>) source;
            for (String key : enumerable.getPropertyNames()) {
                String value = source.getProperty(key).toString();
                if (value.startsWith("{cipher}")) {
                    value = value.substring("{cipher}".length());
                    try {
                        value = cipherService.decrypt(value);
                        LOGGER.info("Decrypted: key={}", key);
                    } catch (Exception e) {
                        String message = "Cannot decrypt: key=" + key;
                        throw new IllegalStateException(message, e);
                    }
                    overrides.put(key, value);
                }
            }
        } else {
            throw new ValidationException("unknow source type");
        }
    }

    /**
     * Get the order value of this object.
     * <p>Higher values are interpreted as lower priority. As a consequence,
     * the object with the lowest value has the highest priority (somewhat
     * analogous to Servlet {@code load-on-startup} values).
     * <p>Same order values will result in arbitrary sort positions for the
     * affected objects.
     * @return the order value
     * @see #HIGHEST_PRECEDENCE
     * @see #LOWEST_PRECEDENCE
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 15;
    }
}
