package cn.yafex.workflow.controller;

import cn.yafex.workflow.model.Workflow;
import cn.yafex.workflow.service.WorkflowManager;
import cn.yafex.workflow.service.WorkflowDebugService;
import cn.yafex.workflow.util.WorkflowLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Date;


@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {
    private final WorkflowManager workflowManager;
    private final WorkflowLoader jsonFileHandler;
    private final WorkflowDebugService debugService;

    @Autowired
    public WorkflowController(WorkflowManager workflowManager, WorkflowLoader jsonFileHandler, WorkflowDebugService debugService) {
        this.workflowManager = workflowManager;
        this.jsonFileHandler = jsonFileHandler;
        this.debugService = debugService;
    }

    /**
     * 创建新工作流
     * @param workflow 要创建工作流包含的初始信息
     * @return 创建后的工作流，包含ID
     */
    @PostMapping
    public ResponseEntity<?> createWorkflow(@RequestBody Workflow workflow) {
        try {
            // 时间戳组成ID
            if (workflow.getId() == null || workflow.getId().isEmpty()) {
                workflow.setId("flow_" + System.currentTimeMillis());
            }

			// 当前时间标记可以作为默认名称
			if (workflow.getName() == null || workflow.getName().isEmpty()) {	
				Date date = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
				String dateStr = sdf.format(date);
				workflow.setName("工作流" + dateStr);
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
     * 更新现有工作流
     * @param workflowId 要更新的工作流ID
     * @param workflow 更新后的工作流数据
     * @return 更新后的工作流
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
     * 根据ID获取工作流
     * @param workflowId 要获取的工作流ID
     * @return 获取的工作流
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
     * 获取所有工作流
     * @return 工作流ID列表
     */
    @GetMapping
    public ResponseEntity<?> listWorkflows() {
        return ResponseEntity.ok(jsonFileHandler.listWorkflows());
    }

    /**
     * 删除工作流
     * @param workflowId 要删除的工作流ID
     * @return 成功状态
     */
    @DeleteMapping("/{workflowId}")
    public ResponseEntity<?> deleteWorkflow(@PathVariable String workflowId) {
        boolean deleted = jsonFileHandler.deleteWorkflow(workflowId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", deleted);
        return ResponseEntity.ok(response);
    }

    /**
     * 启动工作流执行
     * @param workflowId 要执行的工作流ID
     * @param input 执行输入变量
     * @return 执行ID
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
     * 获取工作流执行状态
     * @param workflowId 要获取的工作流ID
     * @param executionId 要获取的执行ID
     * @return 执行状态
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
     * 启动工作流执行（调试模式）
     * @param workflowId 要执行的工作流ID
     * @param input 执行输入变量
     * @return SSE流
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