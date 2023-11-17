package orz.springboot.doc.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface OrzExternalWebErrors {
    OrzExternalWebError[] value();
}
