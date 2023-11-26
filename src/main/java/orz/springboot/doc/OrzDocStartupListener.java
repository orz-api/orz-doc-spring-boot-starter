package orz.springboot.doc;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.ClassUtils;
import orz.springboot.base.OrzBaseStartupListener;
import orz.springboot.web.annotation.OrzWebApi;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.springdoc.core.utils.Constants.SPRINGDOC_ENABLED;
import static orz.springboot.base.description.OrzDescriptionUtils.desc;
import static orz.springboot.doc.OrzDocConstants.*;

@Slf4j
public class OrzDocStartupListener implements OrzBaseStartupListener {
    private final PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private final SimpleMetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();
    private Class<?> applicationClass;

    @Override
    public void onInitialized(SpringApplication application) {
        applicationClass = application.getMainApplicationClass();
    }

    @Override
    public void onContextLoaded(ConfigurableApplicationContext context) {
        var environment = context.getEnvironment();
        if (!environment.getProperty(SPRINGDOC_ENABLED, Boolean.class, true)) {
            return;
        }
        var propsBindResult = Binder.get(environment).bind(PROPS_PREFIX, OrzDocProps.class);
        if (propsBindResult.isBound()) {
            var props = propsBindResult.get();
            var beanFactory = context.getBeanFactory();
            var scopePackageMap = getScopePackageMap();
            scopePackageMap.forEach((scope, pkg) -> {
                var displayName = Optional.ofNullable(props.getScopes().get(scope))
                        .map(OrzDocProps.ScopeConfig::getDisplayName)
                        .map(s -> StringUtils.defaultIfBlank(s, null))
                        .orElse(StringUtils.capitalize(scope));
                if (DEFAULT_SCOPE.equals(scope)) {
                    var excludePackages = scopePackageMap.entrySet().stream()
                            .filter(e -> !DEFAULT_SCOPE.equals(e.getKey()))
                            .map(Map.Entry::getValue)
                            .toArray(String[]::new);
                    beanFactory.registerSingleton(GROUP_BEAN_PREFIX + scope, GroupedOpenApi.builder()
                            .group(scope)
                            .displayName(displayName)
                            .packagesToExclude(excludePackages)
                            .addOpenApiCustomizer(new OrzDocOpenApiCustomizer(scope, props))
                            .addOperationCustomizer(new OrzDocOperationCustomizer(props))
                            .build());
                } else {
                    beanFactory.registerSingleton(GROUP_BEAN_PREFIX + scope, GroupedOpenApi.builder()
                            .group(scope)
                            .displayName(displayName)
                            .packagesToScan(pkg)
                            .addOpenApiCustomizer(new OrzDocOpenApiCustomizer(scope, props))
                            .addOperationCustomizer(new OrzDocOperationCustomizer(props))
                            .addRouterOperationCustomizer(new OrzDocRouterOperationCustomizer(scope))
                            .build());
                }
            });
        } else {
            log.error(desc("OrzDocProps not bound", "prefix", PROPS_PREFIX));
        }
    }

    public Map<String, String> getScopePackageMap() {
        var basePath = ClassUtils.convertClassNameToResourcePath(applicationClass.getPackageName() + "." + ROOT_PACKAGE);
        var searchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + basePath + "/**/*.class";
        Resource[] resources;
        try {
            resources = resourcePatternResolver.getResources(searchPath);
        } catch (IOException e) {
            log.error(desc("getResources error", "searchPath", searchPath), e);
            return Collections.emptyMap();
        }
        var scopePackageMap = new LinkedHashMap<String, String>();
        Stream.of(resources).forEach(resource -> {
            var filename = resource.getFilename();
            if (filename == null || filename.contains("$")) {
                return;
            }

            if (scopePackageMap.containsKey(getScope(resource))) {
                return;
            }

            var reader = (MetadataReader) null;
            try {
                reader = metadataReaderFactory.getMetadataReader(resource);
            } catch (Exception e) {
                log.error(desc("getMetadataReader error", "resource", resource), e);
                return;
            }
            if (reader.getAnnotationMetadata().hasAnnotation(OrzWebApi.class.getName())) {
                var cls = reader.getClassMetadata().getClassName();
                var pkg = getPackage(cls);
                var scope = getScope(cls);
                if (scope != null && pkg != null) {
                    scopePackageMap.put(scope, pkg);
                }
            }
        });
        return scopePackageMap;
    }

    private static String getScope(Resource resource) {
        try {
            var scope = Optional.of(resource.getURI())
                    .map(URI::getPath)
                    .map(path -> path.split("/"))
                    .filter(paths -> paths.length >= 2)
                    .map(paths -> paths[paths.length - 2])
                    .map(s -> StringUtils.defaultIfBlank(s, null))
                    .orElse(null);
            if (scope != null) {
                return ROOT_PACKAGE.equals(scope) ? DEFAULT_SCOPE : scope;
            }
        } catch (IOException ignored) {
        }

        try {
            var scope = Optional.of(resource.getURL())
                    .map(URL::getPath)
                    .map(path -> path.split("/"))
                    .filter(paths -> paths.length >= 2)
                    .map(paths -> paths[paths.length - 2])
                    .map(s -> StringUtils.defaultIfBlank(s, null))
                    .orElse(null);
            if (scope != null) {
                return ROOT_PACKAGE.equals(scope) ? DEFAULT_SCOPE : scope;
            }
        } catch (IOException ignored) {
        }

        try {
            var scope = Optional.of(resource.getFile())
                    .map(File::getParentFile)
                    .map(File::getName)
                    .map(s -> StringUtils.defaultIfBlank(s, null))
                    .orElse(null);
            if (scope != null) {
                return ROOT_PACKAGE.equals(scope) ? DEFAULT_SCOPE : scope;
            }
        } catch (IOException ignored) {
        }

        return null;
    }

    private static String getScope(String cls) {
        return Optional.ofNullable(cls)
                .map(name -> name.split("\\."))
                .filter(names -> names.length >= 2)
                .map(names -> names[names.length - 2])
                .map(s -> StringUtils.defaultIfBlank(s, null))
                .map(s -> ROOT_PACKAGE.equals(s) ? DEFAULT_SCOPE : s)
                .orElse(null);
    }

    private static String getPackage(String cls) {
        return Optional.ofNullable(cls)
                .map(name -> name.substring(0, name.lastIndexOf(".")))
                .map(s -> StringUtils.defaultIfBlank(s, null))
                .orElse(null);
    }
}
