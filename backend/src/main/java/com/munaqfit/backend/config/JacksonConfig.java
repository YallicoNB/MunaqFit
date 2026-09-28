package com.munaqfit.backend.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Evita que Jackson intente serializar los atributos internos de los proxies de
 * Hibernate (hibernateLazyInitializer / handler), que en Hibernate 6 provocan el error:
 * "Type definition error: [simple type, class org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterceptor]"
 *
 * Se aplica de forma global, por lo que cubre tambien los controllers que devuelven
 * entidades JPA directamente.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer hibernateProxyMixIn() {
        return builder -> builder.mixIn(HibernateProxy.class, HibernateProxyMixIn.class);
    }

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private abstract static class HibernateProxyMixIn {
    }
}
