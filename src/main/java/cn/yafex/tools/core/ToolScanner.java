package cn.yafex.tools.core;

import cn.yafex.tools.annotations.Tool;
import cn.yafex.tools.annotations.InputVar;
import cn.yafex.tools.annotations.ReturnVal;
import cn.yafex.tools.schema.FieldDef;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 用于查找和注册工具 handler 的扫描器
 */
public class ToolScanner {
    
    /**
     * 扫描包以查找工具 handler 并注册它们
     * @param basePackage 要扫描的基包
     */
    public static void scanAndRegister(String basePackage) {
		System.out.println("扫描工具: " + basePackage);
        Reflections reflections = new Reflections(basePackage,
            Scanners.SubTypes.filterResultsBy(s -> true),
            Scanners.TypesAnnotated);
            
        // 查找所有带有 @Tool 注解的类
        Set<Class<?>> toolClasses = reflections.getTypesAnnotatedWith(Tool.class);
        
        for (Class<?> toolClass : toolClasses) {
            try {
                registerToolClass(toolClass);
            } catch (Exception e) {
                // 记录错误但继续处理其他工具
                System.err.println("注册工具失败: " + toolClass.getName() + " - " + e.getMessage());
            }
        }
    }
    
    private static void registerToolClass(Class<?> toolClass) throws Exception {
        // Tool toolAnnotation = toolClass.getAnnotation(Tool.class);
        
        // 创建工具实例
        ToolHandler handler = (ToolHandler) toolClass.getDeclaredConstructor().newInstance();
        
        // 查找 execute 方法
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
        
        // 从参数构建输入字段
        Map<String, FieldDef> inputFields = new HashMap<>();
        for (Parameter param : executeMethod.getParameters()) {
            InputVar field = param.getAnnotation(InputVar.class);
            if (field != null) {
                inputFields.put(field.name(), new FieldDef(
                    field.name(),
                    field.description(),
                    field.type(),
                    field.required(),
                    field.defaultValue(),
                    field.pattern()
                ));
            }
        }
        
        // 从方法构建输出字段
        Map<String, FieldDef> outputFields = new HashMap<>();
        ReturnVal returnField = executeMethod.getAnnotation(ReturnVal.class);
        if (returnField != null) {
            outputFields.put(returnField.name(), new FieldDef(
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