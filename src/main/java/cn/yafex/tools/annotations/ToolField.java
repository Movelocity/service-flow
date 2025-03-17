package cn.yafex.tools.annotations;

import cn.yafex.tools.schema.FieldType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明工具输入/输出字段的注解
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ToolFields.class)
public @interface ToolField {
    /**
     * 字段名称
     */
    String name();
    
    /**
     * 字段描述
     */
    String description();
    
    /**
     * 字段类型
     */
    FieldType type();
    
    /**
     * 是否必填
     */
    boolean required() default true;
    
    /**
     * 字段默认值
     */
    String defaultValue() default "";
    
    /**
     * 字段验证模式（如果存在）
     */
    String pattern() default "";
} 