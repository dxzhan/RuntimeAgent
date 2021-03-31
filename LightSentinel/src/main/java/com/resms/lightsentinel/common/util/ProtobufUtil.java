package com.resms.lightsentinel.common.util;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ProtobufUtil {
    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();
 
    private ProtobufUtil() {
    }
 
    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if (Objects.isNull(schema)) {
            schema = RuntimeSchema.getSchema(cls);
            if (Objects.nonNull(schema)) {
                cachedSchema.put(cls, schema);
            }
        }
        return schema;
    }
 
    @SuppressWarnings("unchecked")
    public static <T> String serializeToString(T obj) {
        try {
            return new String(serializeToByte(obj), "UTF-8");
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
 
    public static <T> T deserializeFromString(String data, Class<T> cls) {
        try {
            return deserializeFromByte(data.getBytes("UTF-8"),cls);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
     
 
    @SuppressWarnings("unchecked")
    public static <T> byte[] serializeToByte(T obj) {
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        final byte[] data;
        try {
            Schema<T> schema = getSchema(cls);
            data = ProtobufIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
        return data;
    }
 
    public static <T> T deserializeFromByte(byte[] data, Class<T> cls) {
            Schema<T> schema = getSchema(cls);
            T message = (T) schema.newMessage();
            ProtobufIOUtil.mergeFrom(data, message, schema);
            return message;
    }
}