package cn.yafex.workflow.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import cn.yafex.workflow.model.Workflow;
import cn.yafex.tools.core.ToolDefinition;
import cn.yafex.tools.schema.FieldDef;
import cn.yafex.tools.schema.FieldType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Type;

/**
 * 用于处理JSON工作流定义
 */
@Component
public class WorkflowLoader {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowLoader.class);
    
    static {
        // 配置自定义日志格式
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        ConsoleAppender<ILoggingEvent> appender = (ConsoleAppender<ILoggingEvent>) rootLogger.getAppender("CONSOLE");
        
        if (appender != null) {
            PatternLayout layout = new PatternLayout();
            layout.setContext(loggerContext);
            // Simplified pattern: timestamp level class{without package} - message
            layout.setPattern("%d{HH:mm:ss.SSS} %-5level %logger{0} - %msg%n");
            layout.start();
            appender.setLayout(layout);
        }
    }
    
    @Value("${workflow.definitions.path:workflow-definitions}")
    private String workflowPath;
    
    private Path workflowDir;

    public WorkflowLoader() {
        // 配置Fastjson全局设置
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.PrettyFormat.getMask();
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteMapNullValue.getMask();
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteNullListAsEmpty.getMask();
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteNullStringAsEmpty.getMask();
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.DisableCircularReferenceDetect.getMask();
        
        // 配置自动类型支持嵌套对象
        ParserConfig globalConfig = ParserConfig.getGlobalInstance();
        globalConfig.setAutoTypeSupport(true);
        
        // 添加类型映射为我们自己的类
        globalConfig.putDeserializer(FieldDef.class, new FieldDefDeserializer());
    }

    @PostConstruct
    public void init() throws IOException {
        // 如果必要，将相对路径转换为绝对路径
        workflowDir = Paths.get(workflowPath).toAbsolutePath();
        
        // 如果目录不存在，则创建目录
        Files.createDirectories(workflowDir);
        logger.info("工作流保存目录: {}", workflowDir);
    }

    /**
     * 保存工作流定义到JSON文件
     * @param workflow 要保存的工作流
     * @throws IOException 如果文件操作失败
     */
    public void saveWorkflow(Workflow workflow) throws IOException {
        if (workflow == null || workflow.getId() == null) {
            throw new IllegalArgumentException("Workflow or workflow ID cannot be null");
        }

        Path filePath = workflowDir.resolve(workflow.getId() + ".json");
        try {
            // 创建一个没有Java类型信息的干净JSON字符串
            String jsonString = JSON.toJSONString(
                workflow, 
                SerializerFeature.PrettyFormat,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.NotWriteRootClassName,  // 不要写根类名
                SerializerFeature.DisableCircularReferenceDetect  // 防止@ref使用
            );

            // 清理任何剩余的类型信息
            JSONObject jsonObj = JSON.parseObject(jsonString);
            cleanupTypeInfo(jsonObj);
            jsonString = JSON.toJSONString(
                jsonObj, 
                SerializerFeature.PrettyFormat,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteNullStringAsEmpty
            );

            Files.write(filePath, jsonString.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            logger.info("Saved workflow {} to {}", workflow.getId(), filePath);
        } catch (IOException e) {
            logger.error("Failed to save workflow {}: {}", workflow.getId(), e.getMessage());
            throw e;
        }
    }

    /**
     * 递归地从JSON对象中删除类型信息
     */
    private void cleanupTypeInfo(JSONObject json) {
        if (json == null) return;

        // 删除类型信息
        json.remove("@type");

        // Process all nested objects
        for (Map.Entry<String, Object> entry : json.entrySet()) {
            if (entry.getValue() instanceof JSONObject) {
                cleanupTypeInfo((JSONObject) entry.getValue());
            } else if (entry.getValue() instanceof JSONArray) {
                JSONArray array = (JSONArray) entry.getValue();
                for (int i = 0; i < array.size(); i++) {
                    if (array.get(i) instanceof JSONObject) {
                        cleanupTypeInfo((JSONObject) array.get(i));
                    }
                }
            }
        }
    }

    /**
     * 从JSON文件加载工作流定义
     * @param workflowId 要加载的工作流ID
     * @return 加载的工作流
     * @throws IOException 如果文件操作失败
     */
    public Workflow loadWorkflow(String workflowId) throws IOException {
        if (workflowId == null) {
            throw new IllegalArgumentException("Workflow ID cannot be null");
        }

        Path filePath = workflowDir.resolve(workflowId + ".json");
        if (!Files.exists(filePath)) {
            throw new IOException("Workflow file does not exist: " + filePath);
        }

        try {
            byte[] bytes = Files.readAllBytes(filePath);
            String jsonString = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            Workflow workflow = JSON.parseObject(jsonString, Workflow.class);
            
            // 加载后验证工具定义
            if (workflow.getTools() != null) {
                for (Map.Entry<String, ToolDefinition> entry : workflow.getTools().entrySet()) {
                    String toolName = entry.getKey();
                    ToolDefinition tool = entry.getValue();
                    
                    if (tool == null) {
                        logger.warn("Tool definition is null for tool: {}", toolName);
                        continue;
                    }
                    
                    logger.debug("Loaded tool: {}", toolName);
                }
            }
            
            return workflow;
        } catch (Exception e) {
            logger.error("Failed to load workflow {}: {}", workflowId, e.getMessage());
            logger.error("Stack trace:", e);
            throw new IOException("Failed to load workflow: " + e.getMessage(), e);
        }
    }

    /**
     * 列出所有可用的工作流定义
     * @return 工作流ID数组
     */
    public String[] listWorkflows() {
        try {
            return Files.list(workflowDir)
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(path -> path.getFileName().toString())
                    .map(name -> name.substring(0, name.length() - 5)) // Remove .json extension
                    .toArray(String[]::new);
        } catch (IOException e) {
            logger.error("Failed to list workflows: {}", e.getMessage());
            return new String[0];
        }
    }

    /**
     * 删除一个工作流定义
     * @param workflowId 要删除的工作流ID
     * @return 如果删除成功则返回true
     */
    public boolean deleteWorkflow(String workflowId) {
        if (workflowId == null) {
            return false;
        }

        try {
            Path filePath = workflowDir.resolve(workflowId + ".json");
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            logger.error("Failed to delete workflow {}: {}", workflowId, e.getMessage());
            return false;
        }
    }

    /**
     * 自定义FieldDef反序列化器以确保正确初始化
     */
    private static class FieldDefDeserializer implements ObjectDeserializer {
        @Override
        @SuppressWarnings("unchecked")
        public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
            JSONObject jsonObject = parser.parseObject();
            FieldDef field = new FieldDef();
            
            // 设置基本字段
            field.setName(jsonObject.getString("name"));
            field.setDescription(jsonObject.getString("description"));
            field.setType(parseFieldType(jsonObject.getString("type")));
            field.setRequired(jsonObject.getBooleanValue("required"));
            field.setDefaultValue(jsonObject.getString("defaultValue"));
            
            // 处理集合，确保正确初始化
            JSONObject constraints = jsonObject.getJSONObject("constraints");
            if (constraints != null) {
                field.setConstraints(constraints.getInnerMap());
            }
            
            JSONObject properties = jsonObject.getJSONObject("properties");
            if (properties != null) {
                Map<String, FieldDef> propertyMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    if (entry.getValue() instanceof JSONObject) {
                        propertyMap.put(entry.getKey(), 
                            deserialze(new DefaultJSONParser(((JSONObject)entry.getValue()).toJSONString()),
                                     FieldDef.class, null));
                    }
                }
                field.setProperties(propertyMap);
            }
            
            JSONObject itemDef = jsonObject.getJSONObject("itemDefinition");
            if (itemDef != null) {
                field.setItemDefinition(deserialze(new DefaultJSONParser(itemDef.toJSONString()),
                                                 FieldDef.class, null));
            }
            
            return (T) field;
        }
        
        @Override
        public int getFastMatchToken() {
            return JSONToken.LBRACE;
        }
        
        private FieldType parseFieldType(String type) {
            if (type == null) return null;
            try {
                return FieldType.valueOf(type);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
} 