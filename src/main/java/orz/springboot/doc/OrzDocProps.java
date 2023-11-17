package orz.springboot.doc;

import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "orz.doc")
public class OrzDocProps {
    private boolean longToStringWithFormat = true;

    @Valid
    private Display display = new Display();

    @Data
    public static class Display {
        private String errors = "Errors";
        private String code = "Code";
        private String description = "Description";
    }
}
