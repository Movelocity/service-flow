package cn.yafex.workflow.service;

import cn.yafex.workflow.model.NodeExecutionEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WorkflowDebugService {
    private final Map<String, SseEmitter> debugEmitters = new ConcurrentHashMap<>();

    /**
     * Register a new SSE emitter for workflow debugging
     * @param executionId The workflow execution ID
     * @return SseEmitter for the client
     */
    public SseEmitter registerDebugSession(String executionId) {
        SseEmitter emitter = new SseEmitter(0L); // No timeout
        
        emitter.onCompletion(() -> debugEmitters.remove(executionId));
        emitter.onTimeout(() -> debugEmitters.remove(executionId));
        emitter.onError(e -> debugEmitters.remove(executionId));
        
        debugEmitters.put(executionId, emitter);
        return emitter;
    }

    /**
     * Send a debug event to the client
     * @param event The node execution event
     */
    public void sendDebugEvent(NodeExecutionEvent event) {
        SseEmitter emitter = debugEmitters.get(event.getExecutionId());
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                    .name("node-execution")
                    .data(event));
            } catch (IOException e) {
                emitter.completeWithError(e);
                debugEmitters.remove(event.getExecutionId());
            }
        }
    }

    /**
     * Complete a debug session
     * @param executionId The workflow execution ID
     */
    public void completeDebugSession(String executionId) {
        SseEmitter emitter = debugEmitters.get(executionId);
        if (emitter != null) {
            emitter.complete();
            debugEmitters.remove(executionId);
        }
    }
} 