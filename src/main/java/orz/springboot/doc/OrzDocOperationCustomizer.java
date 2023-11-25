package orz.springboot.doc;

import io.swagger.v3.oas.models.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.web.method.HandlerMethod;
import orz.springboot.doc.annotation.OrzExternalWebError;
import orz.springboot.doc.annotation.OrzExternalWebErrors;
import orz.springboot.web.annotation.OrzWebApi;
import orz.springboot.web.annotation.OrzWebError;
import orz.springboot.web.annotation.OrzWebErrors;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class OrzDocOperationCustomizer implements GlobalOperationCustomizer {
    private final OrzDocProps props;

    public OrzDocOperationCustomizer(OrzDocProps props) {
        this.props = props;
    }

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        var webApi = handlerMethod.getBeanType().getAnnotation(OrzWebApi.class);
        if (webApi != null) {
            operation.setTags(getTags(webApi));
            operation.setOperationId(getOperationId(webApi));

            var errors = getErrors(handlerMethod);
            var externalErrors = getExternalErrors(handlerMethod);
            if (errors != null || externalErrors != null) {
                var builder = new StringBuilder();
                if (StringUtils.isNotEmpty(operation.getDescription())) {
                    builder.append(operation.getDescription()).append("\n<br/>\n");
                }
                var display = props.getDisplay();
                builder.append("## ").append(display.getErrors()).append("\n");
                builder.append("| ").append(display.getCode())
                        .append(" | ").append(display.getDescription())
                        .append(" |\n");
                builder.append("| --- | --- |\n");
                if (errors != null) {
                    for (var error : errors) {
                        builder.append("| `")
                                .append(error.code())
                                .append("` | ")
                                .append(StringUtils.defaultIfBlank(error.description(), error.reason()))
                                .append(" |\n");
                    }
                }
                if (externalErrors != null) {
                    for (var error : externalErrors) {
                        builder.append("| `")
                                .append(error.code())
                                .append("` | ")
                                .append(error.description())
                                .append(" |\n");
                    }
                }
                operation.setDescription(builder.toString());
            }
        }

        return operation;
    }

    private OrzWebError[] getErrors(HandlerMethod handlerMethod) {
        var errors = handlerMethod.getMethodAnnotation(OrzWebErrors.class);
        if (errors != null) {
            return errors.value();
        }
        var error = handlerMethod.getMethodAnnotation(OrzWebError.class);
        if (error != null) {
            return new OrzWebError[]{error};
        }
        return null;
    }

    private OrzExternalWebError[] getExternalErrors(HandlerMethod handlerMethod) {
        var errors = handlerMethod.getMethodAnnotation(OrzExternalWebErrors.class);
        if (errors != null) {
            return errors.value();
        }
        var error = handlerMethod.getMethodAnnotation(OrzExternalWebError.class);
        if (error != null) {
            return new OrzExternalWebError[]{error};
        }
        return null;
    }

    private List<String> getTags(OrzWebApi webApi) {
        return new ArrayList<>(List.of(StringUtils.capitalize(webApi.domain())));
    }

    private String getOperationId(OrzWebApi webApi) {
        var builder = new StringBuilder();
        builder.append(StringUtils.uncapitalize(webApi.domain()));
        if (StringUtils.isNotBlank(webApi.resource())) {
            builder.append(StringUtils.capitalize(webApi.resource()));
        }
        builder.append(StringUtils.capitalize(webApi.action()));
        if (webApi.variant() > 0) {
            builder.append("V").append(webApi.variant());
        }
        return builder.toString();
    }
}
