package com.navan.billing.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.navan.billing.domain.BillingUtils;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.math.BigDecimal;

@Configuration
public class JacksonConfig {
    @Bean
    Jackson2ObjectMapperBuilderCustomizer moneySerializer() {
        return builder -> {
            builder.featuresToEnable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
            SimpleModule module = new SimpleModule();
            module.addSerializer(BigDecimal.class, new JsonSerializer<>() {
                @Override
                public void serialize(BigDecimal value, JsonGenerator generator, SerializerProvider serializers)
                        throws IOException {
                    generator.writeNumber(BillingUtils.toWire(value));
                }
            });
            builder.modulesToInstall(module);
        };
    }
}
