package cn.yafex.tools.annotations;

import cn.yafex.tools.schema.FieldType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to declare tool input/output fields
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ToolField {
    /**
     * Field name
     */
    String name();
    
    /**
     * Field description
     */
    String description();
    
    /**
     * Field type
     */
    FieldType type();
    
    /**
     * Whether the field is required
     */
    boolean required() default true;
    
    /**
     * Default value for the field
     */
    String defaultValue() default "";
    
    /**
     * Field validation pattern if any
     */
    String pattern() default "";
} 