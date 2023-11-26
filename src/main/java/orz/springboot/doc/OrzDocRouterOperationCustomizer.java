package orz.springboot.doc;

import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.RouterOperationCustomizer;
import org.springdoc.core.fn.RouterOperation;
import org.springframework.web.method.HandlerMethod;

public class OrzDocRouterOperationCustomizer implements RouterOperationCustomizer {
    private final String scopeUrlPath;

    public OrzDocRouterOperationCustomizer(String scope) {
        this.scopeUrlPath = "/" + StringUtils.capitalize(scope) + "/";
    }

    @Override
    public RouterOperation customize(RouterOperation routerOperation, HandlerMethod handlerMethod) {
        if (routerOperation.getPath().startsWith(scopeUrlPath)) {
            routerOperation.setPath(routerOperation.getPath().substring(scopeUrlPath.length() - 1));
        }
        return routerOperation;
    }
}
