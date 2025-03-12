package cn.yafex.workflow.controller;

import cn.yafex.workflow.model.Workflow;
import cn.yafex.workflow.service.WorkflowChecker;
import cn.yafex.workflow.service.WorkflowChecker.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for workflow validation
 */
@RestController
@RequestMapping("/api/workflow/validation")
public class WorkflowValidationController {

    private final WorkflowChecker workflowChecker;

    @Autowired
    public WorkflowValidationController(WorkflowChecker workflowChecker) {
        this.workflowChecker = workflowChecker;
    }

    /**
     * Validate a workflow for errors
     * 
     * @param workflow The workflow to validate
     * @param startNodeId Optional start node ID to begin validation from
     * @return Validation result with any errors
     */
    @PostMapping("/validate")
    public ResponseEntity<ValidationResult> validateWorkflow(
            @RequestBody Workflow workflow,
            @RequestParam(required = false) String startNodeId) {
        
        ValidationResult result = workflowChecker.validateWorkflow(workflow, startNodeId);
        return ResponseEntity.ok(result);
    }
} 