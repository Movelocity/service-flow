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
     * 注册一个新的 SSE Emitter 用于工作流调试。
     * @param executionId 工作流执行ID
     * @return SseEmitter 用于客户端
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
     * 发送调试事件到客户端。如果客户端不存在，则不会发送。
     * @param event 节点执行事件
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
     * 完成调试会话。
     * @param executionId 工作流执行ID
     */
    public void completeDebugSession(String executionId) {
        SseEmitter emitter = debugEmitters.get(executionId);
        if (emitter != null) {
            emitter.complete();
            debugEmitters.remove(executionId);
        }
    }
} 