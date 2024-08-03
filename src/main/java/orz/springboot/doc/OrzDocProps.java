package orz.springboot.doc;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@Validated
@ConfigurationProperties(prefix = OrzDocConstants.PROPS_PREFIX)
public class OrzDocProps {
    private boolean longToStringWithFormat = true;

    @Valid
    private Map<String, ScopeConfig> scopes = Collections.emptyMap();

    @Valid
    private DisplayConfig display = new DisplayConfig();

    @Data
    public static class ScopeConfig {
        private boolean enabled = true;

        private String displayName;

        @Valid
        private List<ServerConfig> servers = Collections.emptyList();
    }

    @Data
    public static class ServerConfig {
        @NotBlank
        private String url;

        @NotBlank
        private String description;
    }

    @Data
    public static class DisplayConfig {
        private String errors = "Errors";
        private String code = "Code";
        private String description = "Description";
    }
}
