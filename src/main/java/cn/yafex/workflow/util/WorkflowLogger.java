package cn.yafex.workflow.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

/**
 * 工具类，用于记录工作流执行的详细信息
 */
@Component
public class WorkflowLogger {
    private static final String LOG_DIR = "logs_workflow";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public WorkflowLogger() {
        // 如果日志目录不存在则先创建
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }

    /**
     * 获取日志文件名
     * @param workflowId 工作流ID
     * @param executionId 执行ID
     * @return 日志文件名
     */
    private String getLogFileName(String workflowId, String executionId) {
        return String.format("%s/%s_%s.txt", LOG_DIR, workflowId, executionId);
    }

    /**
     * 记录节点执行事件
     * @param executionId 工作流执行ID
     * @param workflowId 工作流ID
     * @param nodeName 节点名称
     * @param nodeType 节点类型
     * @param input 输入参数
     * @param output 输出参数
     * @param duration 执行时长
     */
    public void logNodeExecution(
        String executionId, String workflowId, String nodeName, String nodeType, 
        Object input, Object output, long duration
    ) {
        String logFileName = getLogFileName(workflowId, executionId);
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String logEntry = String.format(
            "[%s] Node: %s, Type: %s, Duration: %dms%n Input: %s%nOutput: %s%n", 
            timestamp, nodeName, nodeType, duration, input, output);
        try (OutputStreamWriter fileWriter = new OutputStreamWriter(
                new FileOutputStream(logFileName, true), StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(fileWriter)) {
            writer.println(logEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 记录工作流开始
     * @param executionId 工作流执行ID
     * @param workflowId 工作流ID
     */
    public void logWorkflowStart(String executionId, String workflowId) {
        String logFileName = getLogFileName(workflowId, executionId);
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String logEntry = String.format("[%s] Workflow %s started. Execution ID: %s%n",
                timestamp, workflowId, executionId);

        try (OutputStreamWriter fileWriter = new OutputStreamWriter(
                new FileOutputStream(logFileName, true), StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(fileWriter)) {
            writer.println(logEntry);
            System.out.println("工作流执行日志："+logEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 记录工作流完成
     * @param executionId 工作流执行ID
     * @param workflowId 工作流ID
     * @param status 最终状态
     * @param duration 总执行时长
     */
    public void logWorkflowComplete(String executionId, String workflowId, String status, long duration) {
        String logFileName = getLogFileName(workflowId, executionId);
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String logEntry = String.format("[%s] Workflow completed. Status: %s, Total Duration: %dms%n",
                timestamp, status, duration);

        try (OutputStreamWriter fileWriter = new OutputStreamWriter(
                new FileOutputStream(logFileName, true), StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(fileWriter)) {
            writer.println(logEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 