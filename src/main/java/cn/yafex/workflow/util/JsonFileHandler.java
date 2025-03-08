package cn.yafex.workflow.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import cn.yafex.workflow.model.Workflow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for handling JSON workflow definition files
 */
@Component
public class JsonFileHandler {
    private static final Logger logger = LoggerFactory.getLogger(JsonFileHandler.class);
    
    @Value("${workflow.definitions.path:workflow-definitions}")
    private String workflowPath;
    
    private final ObjectMapper objectMapper;
    private Path workflowDir;

    public JsonFileHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        this.objectMapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
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
            objectMapper.writeValue(filePath.toFile(), workflow);
            logger.info("Saved workflow {} to {}", workflow.getId(), filePath);
        } catch (IOException e) {
            logger.error("Failed to save workflow {}: {}", workflow.getId(), e.getMessage());
            throw e;
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
            return objectMapper.readValue(filePath.toFile(), Workflow.class);
        } catch (IOException e) {
            logger.error("Failed to load workflow {}: {}", workflowId, e.getMessage());
            throw e;
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
} 