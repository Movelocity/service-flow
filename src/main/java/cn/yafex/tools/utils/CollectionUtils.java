package cn.yafex.tools.utils;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

/**
 * Utility class for collection operations
 */
public class CollectionUtils {
    
    /**
     * Create an immutable Map with a single entry
     */
    public static <K, V> Map<K, V> mapOf(K key, V value) {
        Map<K, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    /**
     * Create an immutable List with a single element
     */
    public static <T> List<T> listOf(T element) {
        List<T> list = new ArrayList<>();
        list.add(element);
        return list;
    }
} 