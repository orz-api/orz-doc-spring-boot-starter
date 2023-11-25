package orz.springboot.doc;

import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springframework.core.MethodParameter;

public class OrzDocParameterCustomizer implements ParameterCustomizer {
    private final OrzDocSchemaConverter schemaConverter;

    public OrzDocParameterCustomizer(OrzDocSchemaConverter schemaConverter) {
        this.schemaConverter = schemaConverter;
    }

    @Override
    public Parameter customize(Parameter parameterModel, MethodParameter methodParameter) {
        if (parameterModel != null) {
            parameterModel.setSchema(schemaConverter.convert(parameterModel.getSchema()));
        }
        return parameterModel;
    }
}
