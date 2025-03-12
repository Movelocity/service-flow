package cn.yafex.workflow;

import cn.yafex.workflow.model.WorkflowNode;
import cn.yafex.workflow.model.VariableDefinition;
import cn.yafex.workflow.model.NodeType;
import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Test case for WorkflowNode's inputMap functionality
 */
public class InputMapTest {

    @Test
    public void testInputMapBasicFunctions() {
        // Create a test node
        WorkflowNode node = new WorkflowNode();
        node.setId("testNode");
        node.setName("Test Node");
        node.setType(NodeType.FUNCTION);
        node.setToolName("text_process");
        
        // Create a variable definition for a constant text input
        VariableDefinition textInput = new VariableDefinition();
        textInput.setName("CONSTANT");
        textInput.setType("STRING");
        textInput.setDescription("Input text to process");
        textInput.setValue("你好\n我好\n大家好");
        
        // Add to inputMap
        Map<String, VariableDefinition> inputMap = new HashMap<>();
        inputMap.put("text", textInput);
        node.setInputMap(inputMap);
        
        // Verify inputMap is set correctly
        assertNotNull(node.getInputMap());
        assertEquals(1, node.getInputMap().size());
        assertTrue(node.getInputMap().containsKey("text"));
        assertEquals("CONSTANT", node.getInputMap().get("text").getName());
        assertEquals("STRING", node.getInputMap().get("text").getType());
        assertEquals("你好\n我好\n大家好", node.getInputMap().get("text").getValue());
        
        // Test add/get input mapping methods
        VariableDefinition modeInput = new VariableDefinition();
        modeInput.setName("CONSTANT");
        modeInput.setType("STRING");
        modeInput.setValue("LOWERCASE");
        
        node.addInputMapping("mode", modeInput);
        
        assertEquals(2, node.getInputMap().size());
        assertEquals("LOWERCASE", node.getInputMapping("mode").getValue());
        
        // Test JSON serialization/deserialization
        String json = JSON.toJSONString(node);
        WorkflowNode deserializedNode = JSON.parseObject(json, WorkflowNode.class);
        
        assertNotNull(deserializedNode.getInputMap());
        assertEquals(2, deserializedNode.getInputMap().size());
        assertEquals("你好\n我好\n大家好", deserializedNode.getInputMap().get("text").getValue());
        assertEquals("LOWERCASE", deserializedNode.getInputMap().get("mode").getValue());
    }
    
    @Test
    public void testInputMapWithVariableReference() {
        // Create a test node
        WorkflowNode node = new WorkflowNode();
        node.setId("testNode");
        node.setName("Test Node");
        node.setType(NodeType.FUNCTION);
        node.setToolName("text_process");
        
        // Create a variable definition for a variable reference
        VariableDefinition textInput = new VariableDefinition();
        textInput.setName("previousOutput");
        textInput.setType("STRING");
        textInput.setDescription("Input text from previous node");
        textInput.setParent("previousNode");
        
        // Add to inputMap
        node.addInputMapping("text", textInput);
        
        // Verify parent reference
        assertEquals("previousNode", node.getInputMapping("text").getParent());
        assertEquals("previousOutput", node.getInputMapping("text").getName());
    }
} 