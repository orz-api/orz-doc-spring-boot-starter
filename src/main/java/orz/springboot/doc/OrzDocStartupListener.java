package orz.springboot.doc;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static orz.springboot.base.description.OrzDescriptionUtils.desc;

@Slf4j
public class OrzDocStartupListener implements OrzBaseStartupListener {
    private final PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private final SimpleMetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();
    private Class<?> applicationClass;

    @Override
    public void onInitialized(SpringApplication application) {
        applicationClass = application.getMainApplicationClass();
    }

    @SneakyThrows
    @Override
    public Properties getDefaultProperties() {
        var basePath = ClassUtils.convertClassNameToResourcePath(applicationClass.getPackageName() + ".api");
        var searchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + basePath + "/**/*.class";
        var resources = resourcePatternResolver.getResources(searchPath);
        var scopePackageMap = new LinkedHashMap<String, String>();

        Stream.of(resources).forEach(resource -> {
            var filename = resource.getFilename();
            if (filename == null || filename.contains("$")) {
                return;
            }

            if (scopePackageMap.containsKey(getScopeFromResource(resource))) {
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
                var scopeName = getScopeFormClassName(className);
                if (scopeName != null && packageName != null) {
                    scopePackageMap.put(scopeName, packageName);
                }
            }
        });

        var properties = new Properties();
        var i = 0;
        for (var entry : scopePackageMap.entrySet()) {
            var scopeName = entry.getKey();
            var packageName = entry.getValue();
            if (!"api".equals(scopeName)) {
                properties.setProperty("springdoc.group-configs[" + i + "].group", scopeName);
                properties.setProperty("springdoc.group-configs[" + i + "].packages-to-scan", packageName);
            } else {
                var excludePackages = scopePackageMap.entrySet().stream()
                        .filter(e -> !"api".equals(e.getKey()))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.joining(","));
                properties.setProperty("springdoc.group-configs[" + i + "].group", scopeName);
                properties.setProperty("springdoc.group-configs[" + i + "].packages-to-exclude", excludePackages);
            }
            i++;
        }
        return properties;
    }

    private static String getScopeFromResource(Resource resource) {
        try {
            var scope = Optional.of(resource.getURI())
                    .map(URI::getPath)
                    .map(path -> path.split("/"))
                    .filter(paths -> paths.length >= 2)
                    .map(paths -> paths[paths.length - 2])
                    .map(s -> StringUtils.defaultIfBlank(s, null))
                    .orElse(null);
            if (scope != null) {
                return scope;
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
                return scope;
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
                return scope;
            }
        } catch (IOException ignored) {
        }

        return null;
    }

    private static String getScopeFormClassName(String className) {
        return Optional.ofNullable(className)
                .map(name -> name.split("\\."))
                .filter(names -> names.length >= 2)
                .map(names -> names[names.length - 2])
                .map(s -> StringUtils.defaultIfBlank(s, null))
                .orElse(null);
    }

    private static String getPackageName(String className) {
        return Optional.ofNullable(className)
                .map(name -> name.substring(0, name.lastIndexOf(".")))
                .map(s -> StringUtils.defaultIfBlank(s, null))
                .orElse(null);
    }
}
