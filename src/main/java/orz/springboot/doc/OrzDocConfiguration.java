package orz.springboot.doc;

import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(SpringDocConfiguration.class)
public class OrzDocConfiguration {
    @Bean
    public OrzDocParameterCustomizer parameterCustomizer(OrzDocSchemaConverter schemaConverter) {
        return new OrzDocParameterCustomizer(schemaConverter);
    }

    @Bean
    public OrzDocPropertyCustomizer propertyCustomizer(OrzDocSchemaConverter schemaConverter) {
        return new OrzDocPropertyCustomizer(schemaConverter);
    }
}
