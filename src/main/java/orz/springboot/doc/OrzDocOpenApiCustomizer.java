package orz.springboot.doc;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static orz.springboot.doc.OrzDocConstants.DEFAULT_SCOPE;

public class OrzDocOpenApiCustomizer implements OpenApiCustomizer {
    private final String scope;
    private final boolean defaultScope;
    private final String scopeUrlPath;
    private final OrzDocProps props;

    public OrzDocOpenApiCustomizer(String scope, OrzDocProps props) {
        this.scope = scope;
        this.defaultScope = DEFAULT_SCOPE.equals(scope);
        this.scopeUrlPath = defaultScope ? null : "/" + StringUtils.capitalize(scope);
        this.props = props;
    }

    @Override
    public void customise(OpenAPI openApi) {
        var serverConfigList = Optional.ofNullable(props.getScopes().get(scope))
                .map(OrzDocProps.ScopeConfig::getServers)
                .orElse(Collections.emptyList());

        var serverList = new ArrayList<>(openApi.getServers());
        if (!defaultScope && !serverList.isEmpty()) {
            serverList.forEach(s -> {
                if (Constants.DEFAULT_SERVER_DESCRIPTION.equals(s.getDescription())) {
                    if (!s.getUrl().endsWith(scopeUrlPath)) {
                        s.setUrl(s.getUrl() + scopeUrlPath);
                    }
                }
            });
        }
        if (!serverConfigList.isEmpty()) {
            serverList.forEach(s -> {
                var server = new Server();
                server.setUrl(s.getUrl());
                server.setDescription(s.getDescription());
                serverList.add(server);
            });
        }

        openApi.setServers(serverList);
    }
}
