package orz.springboot.doc;

import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class OrzDocSchemaConverter {
    private final OrzDocProps props;

    public OrzDocSchemaConverter(OrzDocProps props) {
        this.props = props;
    }

    public Schema<?> convert(Schema<?> schema) {
        if (schema != null) {
            if (schema instanceof IntegerSchema) {
                if ("integer".equals(schema.getType()) && "int64".equals(schema.getFormat())) {
                    var newSchema = new StringSchema();
                    if (schema.getExample() != null) {
                        newSchema.setExample(schema.getExample());
                    } else {
                        newSchema.setExample("1");
                    }
                    if (StringUtils.isNotBlank(schema.getDescription())) {
                        newSchema.setDescription(schema.getDescription());
                    }
                    // TODO: other properties copy
                    if (props.isLongToStringWithFormat()) {
                        newSchema.setFormat("int64");
                    }
                    return newSchema;
                }
            }
        }
        return schema;
    }
}
