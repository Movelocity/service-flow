package cn.yafex.tools.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as a tool handler
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Tool {
    /**
     * The name of the tool
     */
    String name();
    
    /**
     * Description of what the tool does
     */
    String description();
} 