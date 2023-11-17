package orz.springboot.doc.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Repeatable(OrzExternalWebErrors.class)
public @interface OrzExternalWebError {
    /**
     * 错误编号
     */
    String code();

    /**
     * 错误描述
     */
    String description();
}
