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
import cn.yafex.tools.schema.FieldDefinition;
import cn.yafex.tools.schema.FieldType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Type;

/**
 * Utility class for handling JSON workflow definition files
 */
@Component
public class JsonFileHandler {
    private static final Logger logger = LoggerFactory.getLogger(JsonFileHandler.class);
    
    @Value("${workflow.definitions.path:workflow-definitions}")
    private String workflowPath;
    
    private Path workflowDir;

    public JsonFileHandler() {
        // Configure Fastjson global settings
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.PrettyFormat.getMask();
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteMapNullValue.getMask();
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteNullListAsEmpty.getMask();
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteNullStringAsEmpty.getMask();
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.DisableCircularReferenceDetect.getMask();
        
        // Configure auto-type support for nested objects
        ParserConfig globalConfig = ParserConfig.getGlobalInstance();
        globalConfig.setAutoTypeSupport(true);
        
        // Add type mappings for our classes
        globalConfig.putDeserializer(FieldDefinition.class, new FieldDefinitionDeserializer());
    }

    @PostConstruct
    public void init() throws IOException {
        // Convert relative path to absolute path if necessary
        workflowDir = Paths.get(workflowPath).toAbsolutePath();
        
        // Create workflow directory if it doesn't exist
        Files.createDirectories(workflowDir);
        logger.info("Using workflow directory: {}", workflowDir);
    }

    /**
     * Save workflow definition to JSON file
     * @param workflow Workflow to save
     * @throws IOException If file operations fail
     */
    public void saveWorkflow(Workflow workflow) throws IOException {
        if (workflow == null || workflow.getId() == null) {
            throw new IllegalArgumentException("Workflow or workflow ID cannot be null");
        }

        Path filePath = workflowDir.resolve(workflow.getId() + ".json");
        try {
            // Create a clean JSON string without Java type information
            String jsonString = JSON.toJSONString(
                workflow, 
                SerializerFeature.PrettyFormat,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.NotWriteRootClassName,  // Don't write root class name
                SerializerFeature.DisableCircularReferenceDetect  // Prevent @ref usage
            );

            // Clean up any remaining type information
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
     * Recursively remove type information from JSON objects
     */
    private void cleanupTypeInfo(JSONObject json) {
        if (json == null) return;

        // Remove type information
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
     * Load workflow definition from JSON file
     * @param workflowId ID of the workflow to load
     * @return Loaded workflow
     * @throws IOException If file operations fail
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
            // logger.debug("Loading workflow JSON: {}", jsonString);
            
            Workflow workflow = JSON.parseObject(jsonString, Workflow.class);
            
            // Validate tool definitions after loading
            if (workflow.getTools() != null) {
                for (Map.Entry<String, ToolDefinition> entry : workflow.getTools().entrySet()) {
                    String toolName = entry.getKey();
                    ToolDefinition tool = entry.getValue();
                    
                    if (tool == null) {
                        logger.warn("Tool definition is null for tool: {}", toolName);
                        continue;
                    }
                    
                    logger.debug("Loaded tool: {}", toolName);
                    // if (tool.getInputs() != null) {
                    //     for (Map.Entry<String, FieldDefinition> fieldEntry : tool.getInputs().entrySet()) {
                    //         logger.debug("Tool {} input field: {} = {}", toolName, fieldEntry.getKey(), fieldEntry.getValue());
                    //     }
                    // }
                    
                    // if (tool.getOutputs() != null) {
                    //     for (Map.Entry<String, FieldDefinition> fieldEntry : tool.getOutputs().entrySet()) {
                    //         logger.debug("Tool {} output field: {} = {}", toolName, fieldEntry.getKey(), fieldEntry.getValue());
                    //     }
                    // }
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
     * List all available workflow definitions
     * @return Array of workflow IDs
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
     * Delete a workflow definition
     * @param workflowId ID of the workflow to delete
     * @return true if deletion was successful
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
     * Custom deserializer for FieldDefinition to ensure proper initialization
     */
    private static class FieldDefinitionDeserializer implements ObjectDeserializer {
        @Override
        @SuppressWarnings("unchecked")
        public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
            JSONObject jsonObject = parser.parseObject();
            FieldDefinition field = new FieldDefinition();
            
            // Set basic fields
            field.setName(jsonObject.getString("name"));
            field.setDescription(jsonObject.getString("description"));
            field.setType(parseFieldType(jsonObject.getString("type")));
            field.setRequired(jsonObject.getBooleanValue("required"));
            field.setDefaultValue(jsonObject.getString("defaultValue"));
            field.setPattern(jsonObject.getString("pattern"));
            
            // Handle collections with proper initialization
            JSONObject constraints = jsonObject.getJSONObject("constraints");
            if (constraints != null) {
                field.setConstraints(constraints.getInnerMap());
            }
            
            JSONArray enumValues = jsonObject.getJSONArray("enumValues");
            if (enumValues != null) {
                field.setEnumValues(enumValues.toJavaList(String.class));
            }
            
            JSONObject properties = jsonObject.getJSONObject("properties");
            if (properties != null) {
                Map<String, FieldDefinition> propertyMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    if (entry.getValue() instanceof JSONObject) {
                        propertyMap.put(entry.getKey(), 
                            deserialze(new DefaultJSONParser(((JSONObject)entry.getValue()).toJSONString()),
                                     FieldDefinition.class, null));
                    }
                }
                field.setProperties(propertyMap);
            }
            
            JSONObject itemDef = jsonObject.getJSONObject("itemDefinition");
            if (itemDef != null) {
                field.setItemDefinition(deserialze(new DefaultJSONParser(itemDef.toJSONString()),
                                                 FieldDefinition.class, null));
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