package cn.yafex.tools.core;

import cn.yafex.tools.annotations.Tool;
import cn.yafex.tools.annotations.ToolField;
import cn.yafex.tools.schema.FieldDefinition;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Scanner for finding and registering tool handlers using annotations
 */
public class ToolScanner {
    
    /**
     * Scan packages for tool handlers and register them
     * @param basePackage The base package to scan
     */
    public static void scanAndRegister(String basePackage) {
		System.out.println("扫描工具: " + basePackage);
        Reflections reflections = new Reflections(basePackage,
            Scanners.SubTypes.filterResultsBy(s -> true),
            Scanners.TypesAnnotated);
            
        // Find all classes annotated with @Tool
        Set<Class<?>> toolClasses = reflections.getTypesAnnotatedWith(Tool.class);
        
        for (Class<?> toolClass : toolClasses) {
            try {
                registerToolClass(toolClass);
            } catch (Exception e) {
                // Log error but continue with other tools
                System.err.println("注册工具失败: " + toolClass.getName() + " - " + e.getMessage());
            }
        }
    }
    
    private static void registerToolClass(Class<?> toolClass) throws Exception {
        // Tool toolAnnotation = toolClass.getAnnotation(Tool.class);
        
        // Create tool instance
        ToolHandler handler = (ToolHandler) toolClass.getDeclaredConstructor().newInstance();
        
        // Find execute method
        Method executeMethod = null;
        for (Method method : toolClass.getMethods()) {
            if (method.getName().equals("execute")) {
                executeMethod = method;
                break;
            }
        }
        
        if (executeMethod == null) {
            throw new IllegalStateException("工具类中没有找到 execute 方法: " + toolClass.getName());
        }
        
        // Build input fields from parameters
        Map<String, FieldDefinition> inputFields = new HashMap<>();
        for (Parameter param : executeMethod.getParameters()) {
            ToolField field = param.getAnnotation(ToolField.class);
            if (field != null) {
                inputFields.put(field.name(), new FieldDefinition(
                    field.name(),
                    field.description(),
                    field.type(),
                    field.required(),
                    field.defaultValue(),
                    field.pattern()
                ));
            }
        }
        
        // Build output fields from method
        Map<String, FieldDefinition> outputFields = new HashMap<>();
        ToolField returnField = executeMethod.getAnnotation(ToolField.class);
        if (returnField != null) {
            outputFields.put(returnField.name(), new FieldDefinition(
                returnField.name(),
                returnField.description(),
                returnField.type(),
                returnField.required(),
                returnField.defaultValue(),
                returnField.pattern()
            ));
        }
        
        // // Create tool definition
        // ToolDefinition definition = new ToolDefinition(
        //     toolAnnotation.name(),
        //     toolAnnotation.description(),
        //     inputFields,
        //     outputFields,
        //     null // Exception types can be added if needed
        // );
        
        // Register the tool
        ToolRegistry.register(handler);
		System.out.println("注册工具: " + toolClass.getName());
    }
} 