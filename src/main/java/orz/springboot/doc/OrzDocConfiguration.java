package orz.springboot.doc;

import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.springdoc.core.utils.Constants.SPRINGDOC_ENABLED;

@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(SpringDocConfiguration.class)
@ConditionalOnProperty(name = SPRINGDOC_ENABLED, matchIfMissing = false)
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
