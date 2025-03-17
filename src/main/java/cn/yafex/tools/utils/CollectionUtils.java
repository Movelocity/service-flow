package cn.yafex.tools.utils;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

/**
 * 集合操作的实用类
 */
public class CollectionUtils {
    
    /**
     * 创建一个包含单个条目的不可变 Map
     */
    public static <K, V> Map<K, V> mapOf(K key, V value) {
        Map<K, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    /**
     * 创建一个包含单个元素的不可变 List
     */
    public static <T> List<T> listOf(T element) {
        List<T> list = new ArrayList<>();
        list.add(element);
        return list;
    }
} 