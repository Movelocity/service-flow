package cn.yafex.workflow.controller;

import cn.yafex.workflow.model.Workflow;
import cn.yafex.workflow.service.WorkflowManager;
import cn.yafex.workflow.util.JsonFileHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {
    private final WorkflowManager workflowManager;
    private final JsonFileHandler jsonFileHandler;

    @Autowired
    public WorkflowController(WorkflowManager workflowManager, JsonFileHandler jsonFileHandler) {
        this.workflowManager = workflowManager;
        this.jsonFileHandler = jsonFileHandler;
    }

    @PostMapping("/{workflowId}/start")
    public ResponseEntity<String> startWorkflow(
            @PathVariable String workflowId,
            @RequestBody(required = false) Map<String, Object> variables) {
        String executionId = workflowManager.startWorkflow(workflowId, variables);
        return ResponseEntity.ok(executionId);
    }

    @GetMapping("/{workflowId}/status/{executionId}")
    public ResponseEntity<String> getWorkflowStatus(
            @PathVariable String workflowId,
            @PathVariable String executionId) {
        return ResponseEntity.ok(workflowManager.getWorkflowStatus(executionId).toString());
    }

    @PostMapping("/{workflowId}/stop/{executionId}")
    public ResponseEntity<Boolean> stopWorkflow(
            @PathVariable String workflowId,
            @PathVariable String executionId) {
        boolean stopped = workflowManager.stopWorkflow(executionId);
        return ResponseEntity.ok(stopped);
    }

    @PostMapping
    public ResponseEntity<String> saveWorkflow(@RequestBody Workflow workflow) {
        try {
            jsonFileHandler.saveWorkflow(workflow);
            return ResponseEntity.ok(workflow.getId());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to save workflow: " + e.getMessage());
        }
    }

    @GetMapping("/{workflowId}")
    public ResponseEntity<Workflow> getWorkflow(@PathVariable String workflowId) {
        try {
            Workflow workflow = jsonFileHandler.loadWorkflow(workflowId);
            return ResponseEntity.ok(workflow);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<String[]> listWorkflows() {
        return ResponseEntity.ok(jsonFileHandler.listWorkflows());
    }

    @DeleteMapping("/{workflowId}")
    public ResponseEntity<Boolean> deleteWorkflow(@PathVariable String workflowId) {
        boolean deleted = jsonFileHandler.deleteWorkflow(workflowId);
        return ResponseEntity.ok(deleted);
    }
} 