package cn.yafex.workflow.controller;

import cn.yafex.workflow.model.Workflow;
import cn.yafex.workflow.service.WorkflowManager;
import cn.yafex.workflow.service.WorkflowDebugService;
import cn.yafex.workflow.util.JsonFileHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {
    private final WorkflowManager workflowManager;
    private final JsonFileHandler jsonFileHandler;
    private final WorkflowDebugService debugService;

    @Autowired
    public WorkflowController(WorkflowManager workflowManager, JsonFileHandler jsonFileHandler, WorkflowDebugService debugService) {
        this.workflowManager = workflowManager;
        this.jsonFileHandler = jsonFileHandler;
        this.debugService = debugService;
    }

    /**
     * Create a new workflow
     * @param workflow The workflow to create
     * @return The created workflow with ID
     */
    @PostMapping
    public ResponseEntity<?> createWorkflow(@RequestBody Workflow workflow) {
        try {
            // Generate ID for new workflow
            if (workflow.getId() == null || workflow.getId().isEmpty()) {
                workflow.setId("flow_" + System.currentTimeMillis());
            }
            jsonFileHandler.saveWorkflow(workflow);
            return ResponseEntity.ok(workflow);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "保存工作流失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Update an existing workflow
     * @param workflowId The ID of the workflow to update
     * @param workflow The updated workflow data
     * @return The updated workflow
     */
    @PutMapping("/{workflowId}")
    public ResponseEntity<?> updateWorkflow(
            @PathVariable String workflowId,
            @RequestBody Workflow workflow) {
        try {
            // Ensure the ID in the path matches the workflow
            workflow.setId(workflowId);
            jsonFileHandler.saveWorkflow(workflow);
            return ResponseEntity.ok(workflow);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "更新工作流失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get a workflow by ID
     * @param workflowId The ID of the workflow to retrieve
     * @return The workflow if found
     */
    @GetMapping("/{workflowId}")
    public ResponseEntity<?> getWorkflow(@PathVariable String workflowId) {
        try {
            Workflow workflow = jsonFileHandler.loadWorkflow(workflowId);
            return ResponseEntity.ok(workflow);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * List all workflows
     * @return Array of workflow IDs
     */
    @GetMapping
    public ResponseEntity<?> listWorkflows() {
        return ResponseEntity.ok(jsonFileHandler.listWorkflows());
    }

    /**
     * Delete a workflow
     * @param workflowId The ID of the workflow to delete
     * @return Success status
     */
    @DeleteMapping("/{workflowId}")
    public ResponseEntity<?> deleteWorkflow(@PathVariable String workflowId) {
        boolean deleted = jsonFileHandler.deleteWorkflow(workflowId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", deleted);
        return ResponseEntity.ok(response);
    }

    /**
     * Start a workflow execution
     * @param workflowId The ID of the workflow to execute
     * @param input The input variables for execution
     * @return The execution ID
     */
    @PostMapping("/{workflowId}/execute")
    public ResponseEntity<?> executeWorkflow(
            @PathVariable String workflowId,
            @RequestBody(required = false) Map<String, Object> input) {
        try {
            String executionId = workflowManager.startWorkflow(workflowId, input);
            Map<String, String> response = new HashMap<>();
            response.put("executionId", executionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "工作流执行失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get workflow execution status
     * @param workflowId The ID of the workflow
     * @param executionId The ID of the execution
     * @return The execution status
     */
    @GetMapping("/{workflowId}/executions/{executionId}")
    public ResponseEntity<?> getExecutionStatus(
            @PathVariable String workflowId,
            @PathVariable String executionId) {
        try {
            Map<String, String> response = new HashMap<>();
            response.put("status", workflowManager.getWorkflowStatus(executionId).toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取工作流执行状态失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Start a workflow execution in debug mode
     * @param workflowId The ID of the workflow to execute
     * @param input The input variables for execution
     * @return SSE stream of execution events
     */
    @PostMapping("/{workflowId}/debug")
    public SseEmitter debugWorkflow(
            @PathVariable String workflowId,
            @RequestBody(required = false) Map<String, Object> input) {
        try {
            String executionId = workflowManager.startWorkflow(workflowId, input);
            return debugService.registerDebugSession(executionId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "工作流调试启动失败: " + e.getMessage());
        }
    }
} 