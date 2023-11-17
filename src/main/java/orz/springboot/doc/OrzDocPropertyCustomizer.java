package orz.springboot.doc;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.PropertyCustomizer;
import org.springframework.stereotype.Component;

@Component
public class OrzDocPropertyCustomizer implements PropertyCustomizer {
    private final OrzDocSchemaConverter schemaConverter;

    public OrzDocPropertyCustomizer(OrzDocSchemaConverter schemaConverter) {
        this.schemaConverter = schemaConverter;
    }

    @Override
    public Schema<?> customize(Schema property, AnnotatedType type) {
        return schemaConverter.convert(property);
    }
}
