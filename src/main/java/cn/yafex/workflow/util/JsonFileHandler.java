package cn.yafex.workflow.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import cn.yafex.workflow.model.Workflow;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for handling JSON workflow definition files
 */
@Component
public class JsonFileHandler {
    private static final String WORKFLOW_DIR = "workflow-definitions";
    private final ObjectMapper objectMapper;

    public JsonFileHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Create workflow directory if it doesn't exist
        File workflowDir = new File(WORKFLOW_DIR);
        if (!workflowDir.exists()) {
            workflowDir.mkdirs();
        }
    }

    /**
     * Save workflow definition to JSON file
     * @param workflow Workflow to save
     * @throws IOException If file operations fail
     */
    public void saveWorkflow(Workflow workflow) throws IOException {
        String fileName = String.format("%s/%s.json", WORKFLOW_DIR, workflow.getId());
        objectMapper.writeValue(new File(fileName), workflow);
    }

    /**
     * Load workflow definition from JSON file
     * @param workflowId ID of the workflow to load
     * @return Loaded workflow
     * @throws IOException If file operations fail
     */
    public Workflow loadWorkflow(String workflowId) throws IOException {
        String fileName = String.format("%s/%s.json", WORKFLOW_DIR, workflowId);
        return objectMapper.readValue(new File(fileName), Workflow.class);
    }

    /**
     * List all available workflow definitions
     * @return Array of workflow IDs
     */
    public String[] listWorkflows() {
        File workflowDir = new File(WORKFLOW_DIR);
        return workflowDir.list((dir, name) -> name.endsWith(".json"));
    }

    /**
     * Delete a workflow definition
     * @param workflowId ID of the workflow to delete
     * @return true if deletion was successful
     */
    public boolean deleteWorkflow(String workflowId) {
        String fileName = String.format("%s/%s.json", WORKFLOW_DIR, workflowId);
        File file = new File(fileName);
        return file.delete();
    }
} 