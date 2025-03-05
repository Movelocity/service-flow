package cn.yafex.workflow.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

/**
 * Utility class for logging workflow execution details
 */
@Component
public class WorkflowLogger {
    private static final String LOG_DIR = "workflow-logs";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public WorkflowLogger() {
        // Create log directory if it doesn't exist
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }

    /**
     * Log a workflow execution event
     * @param executionId Workflow execution ID
     * @param nodeName Node name
     * @param nodeType Node type
     * @param input Input parameters
     * @param output Output parameters
     * @param duration Duration in milliseconds
     */
    public void logNodeExecution(String executionId, String nodeName, String nodeType, 
            Object input, Object output, long duration) {
        String logFileName = String.format("%s/%s.txt", LOG_DIR, executionId);
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String logEntry = String.format("[%s] Node: %s, Type: %s, Duration: %dms%n" +
                "Input: %s%nOutput: %s%n", 
                timestamp, nodeName, nodeType, duration, input, output);

        try (PrintWriter writer = new PrintWriter(new FileWriter(logFileName, true))) {
            writer.println(logEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Log workflow start
     * @param executionId Workflow execution ID
     * @param workflowId Workflow ID
     */
    public void logWorkflowStart(String executionId, String workflowId) {
        String logFileName = String.format("%s/%s.txt", LOG_DIR, executionId);
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String logEntry = String.format("[%s] Workflow %s started. Execution ID: %s%n",
                timestamp, workflowId, executionId);

        try (PrintWriter writer = new PrintWriter(new FileWriter(logFileName, true))) {
            writer.println(logEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Log workflow completion
     * @param executionId Workflow execution ID
     * @param status Final status
     * @param duration Total duration in milliseconds
     */
    public void logWorkflowComplete(String executionId, String status, long duration) {
        String logFileName = String.format("%s/%s.txt", LOG_DIR, executionId);
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String logEntry = String.format("[%s] Workflow completed. Status: %s, Total Duration: %dms%n",
                timestamp, status, duration);

        try (PrintWriter writer = new PrintWriter(new FileWriter(logFileName, true))) {
            writer.println(logEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 