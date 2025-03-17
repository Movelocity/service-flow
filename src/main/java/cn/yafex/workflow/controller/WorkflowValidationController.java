package cn.yafex.workflow.controller;

import cn.yafex.workflow.model.Workflow;
import cn.yafex.workflow.service.WorkflowChecker;
import cn.yafex.workflow.service.WorkflowChecker.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 工作流验证控制器
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
     * 验证工作流错误
     * 
     * @param workflow 要验证的工作流
     * @param startNodeId 可选的开始节点ID
     * @return 验证结果，包含任何错误
     */
    @PostMapping("/validate")
    public ResponseEntity<ValidationResult> validateWorkflow(
            @RequestBody Workflow workflow,
            @RequestParam(required = false) String startNodeId) {
        
        ValidationResult result = workflowChecker.validateWorkflow(workflow, startNodeId);
        return ResponseEntity.ok(result);
    }
} 