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
import orz.springboot.web.OrzWebUtils;
import orz.springboot.web.annotation.OrzWebApi;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.springdoc.core.utils.Constants.SPRINGDOC_ENABLED;
import static orz.springboot.base.description.OrzDescriptionUtils.desc;
import static orz.springboot.doc.OrzDocConstants.GROUP_BEAN_PREFIX;
import static orz.springboot.doc.OrzDocConstants.PROPS_PREFIX;
import static orz.springboot.web.OrzWebConstants.API_PACKAGE;

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
                beanFactory.registerSingleton(GROUP_BEAN_PREFIX + scope, GroupedOpenApi.builder()
                        .group(scope)
                        .displayName(displayName)
                        .packagesToScan(pkg)
                        .addOpenApiCustomizer(new OrzDocOpenApiCustomizer(scope, props))
                        .addOperationCustomizer(new OrzDocOperationCustomizer(props))
                        .addRouterOperationCustomizer(new OrzDocRouterOperationCustomizer(scope))
                        .build());
            });
        } else {
            log.error(desc("OrzDocProps not bound", "prefix", PROPS_PREFIX));
        }
    }

    public Map<String, String> getScopePackageMap() {
        var basePath = ClassUtils.convertClassNameToResourcePath(applicationClass.getPackageName() + "." + API_PACKAGE);
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
                var className = reader.getClassMetadata().getClassName();
                var packageName = getPackageName(className);
                var scope = OrzWebUtils.getScope(className);
                if (scope != null && packageName != null) {
                    scopePackageMap.put(scope, packageName);
                }
            }
        });
        return scopePackageMap;
    }

    private static String getScope(Resource resource) {
        try {
            var scope = getScopeFromPath(resource.getURI().getPath());
            if (scope != null) {
                return scope;
            }
        } catch (Exception ignored) {
        }

        try {
            var scope = getScopeFromPath(resource.getURL().getPath());
            if (scope != null) {
                return scope;
            }
        } catch (Exception ignored) {
        }

        try {
            var scope = getScopeFromPath(resource.getFile().toURI().getPath());
            if (scope != null) {
                return scope;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private static String getPackageName(String cls) {
        return Optional.ofNullable(cls)
                .filter(s -> s.contains("."))
                .map(name -> name.substring(0, name.lastIndexOf(".")))
                .map(s -> StringUtils.defaultIfBlank(s, null))
                .orElse(null);
    }

    private static String getScopeFromPath(String path) {
        return Optional.ofNullable(path)
                .filter(StringUtils::isNotBlank)
                .filter(s -> s.contains(API_PACKAGE))
                .map(s -> removePathExtension(s).replace("/", "."))
                .map(OrzWebUtils::getScope)
                .orElse(null);
    }

    private static String removePathExtension(String path) {
        var index = path.lastIndexOf(".");
        if (index == -1) {
            return path;
        }
        return path.substring(0, index);
    }
}
