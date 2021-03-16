package com.resms.rta.common.util;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Json工具类
 *
 * @author sam
 */
public class JsonMapper {
    private static final Logger logger = LoggerFactory.getLogger(JsonMapper.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,false);
        OBJECT_MAPPER.setDateFormat(new SimpleDateFormat("yyyyMMddHHmmss"));
        OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        OBJECT_MAPPER.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);//解决BigDecimal装箱成float导致截断问题
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
//      objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE);
//      objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
//      objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//      objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        AnnotationIntrospector jaxbAI = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
        // if ONLY using JAXB annotations:
//		objectMapper.registerModule(new JaxbAnnotationModule());
//		objectMapper.setAnnotationIntrospector(_jaxbAI);
        // if using BOTH JAXB annotations AND Jackson annotations:
        AnnotationIntrospector jacksonAI = new JacksonAnnotationIntrospector();
        OBJECT_MAPPER.setAnnotationIntrospector(new AnnotationIntrospectorPair(jacksonAI,jaxbAI));
    }

    public static ObjectNode newObject()
    {
        return OBJECT_MAPPER.createObjectNode();
    }

    /**
     * pare a object to JsonNode
     * @param obj
     * @return
     */
    public static JsonNode parse(Object obj) {
        try {
            return OBJECT_MAPPER.convertValue(obj, JsonNode.class);
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return null;
        }
    }
    /**
     * from json string to generic object
     *
     * @param jsonString json string
     * @param clazz      generic object type
     * @return generic object
     */
    public static <T> T parse(String jsonString, Class<T> clazz) {
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(jsonString, clazz);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public static <T> T parse(byte[] bytes,Class<T> clazz) {
        return parse(new String(bytes,StandardCharsets.UTF_8),clazz);
    }
    /**
     * from JsonNode to generic Object
     *
     * @param jsonNode JsonNode object
     * @param clazz    generic object type
     * @return generic type object
     */
    public static <T> T parse(JsonNode jsonNode, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.treeToValue(jsonNode, clazz);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * from json String to Map
     *
     * @param jsonStr input json string
     * @return key and value map
     * @throws IOException          IOException for input
     * @throws Exception            other exception
     */
    public static Map<String, Object> parseMap(String jsonStr) {

        try {
            return OBJECT_MAPPER.readValue(jsonStr, Map.class);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * parse a object to Map<String, Object>
     * @param obj
     * @return
     */
    public static Map<String, Object> parseMap(Object obj) {

        try {
            return OBJECT_MAPPER.convertValue(obj, Map.class);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * json string convert to map with javaBean
     *
     * @param jsonStr json string
     * @param clazz   generic type
     * @return Map<String,T>
     * @throws Exception other exception
     */
    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> parseMap(String jsonStr, Class<T> clazz) throws Exception {

        final Map<String, T> map = OBJECT_MAPPER.readValue(jsonStr,
                new TypeReference<Map<String, T>>() {
                });
        final Map<String, T> result = new HashMap<String, T>();
        for (final Map.Entry<String, T> entry : map.entrySet()) {
            result.put(entry.getKey(), OBJECT_MAPPER.convertValue(entry.getValue(), clazz));
        }
        return result;
    }

    public static <T> T parse(String jsonText, Class<?> collectionClz, Class<?> elementClz) throws IOException {
        JavaType type = OBJECT_MAPPER.getTypeFactory().constructParametricType(collectionClz,elementClz);
        return OBJECT_MAPPER.readValue(jsonText,type);
    }

    /**
     * from json array string to list of generic type
     *
     * @param jsonArrayStr json array string
     * @param clazz        generic type
     * @return list of generic type
     * @throws Exception other exception
     */
    public static <T> List<T> parseList(String jsonArrayStr, Class<T> clazz) throws Exception {

        final List<T> list = OBJECT_MAPPER
                .readValue(jsonArrayStr, new TypeReference<List<T>>() {});
        final List<T> result = new ArrayList<T>();
        for (final T map : list) {
            result.add(OBJECT_MAPPER.convertValue(map, clazz));
        }
        return result;
    }

    /**
     * from InputStream to JsonNode
     *
     * @param inputStream input stream
     * @return JsonNode
     */
    public static JsonNode parse(InputStream inputStream) {
        try {
            return OBJECT_MAPPER.readValue(inputStream, JsonNode.class);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * from object to json string
     *
     * @param object object
     * @return json string
     */
    public static String toJson(Object object) {
        if (object == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public static byte[] toJsonBytes(Object object) {
        return toJson(object).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * to jsonP string
     *
     * @param functionName callback function name
     * @param object       object
     * @return jsonP string
     */
    public static String toJsonP(String functionName, Object object) {
        if (object == null) {
            return null;
        }

        return toJson(new JSONPObject(functionName, object));
    }

    public <T> T parse(String jsonText) throws IOException {
        return (T) OBJECT_MAPPER.readValue(jsonText, this.getClass());
    }

    public static boolean toJsonFile(JsonNode node, String pathName)
    {
        if(node != null) {
            File jsonFile = new File(pathName);

            ObjectMapper mapper = OBJECT_MAPPER;
            // save file
            JsonFactory jsonFactory = new JsonFactory();
            JsonGenerator jsonGenerator = null;
            try {
                jsonGenerator = jsonFactory.createGenerator(jsonFile, JsonEncoding.UTF8);
                mapper.writeTree(jsonGenerator, node);
                return true;
            } catch (IOException e) {
                logger.error(e.getMessage());
                return false;
            }
        }
        return false;
    }

    /**
     * use json string update object
     * @param jsonString
     * @param object
     * @return
     */
    public static <T> T update(String jsonString, T object) {
        if (object == null) {
            return null;
        }

        try {
            return (T) OBJECT_MAPPER.readerForUpdating(object).readValue(jsonString);
        } catch (IOException e) {
            logger.error(e.getMessage());
            //logger.warn("update json string:" + jsonString + " to object:" + this.getClass().getSimpleName() + " error.",e);
            return object;
        }
    }

    public static <T> T update(JsonNode jsonNode, T object) {
        if (object == null) {
            return null;
        }

        try {
            return (T) OBJECT_MAPPER.readerForUpdating(object).readValue(jsonNode);
        } catch (IOException e) {
            logger.error(e.getMessage());
            //logger.warn("update json string:" + jsonString + " to object:" + this.getClass().getSimpleName() + " error.",e);
            return object;
        }
    }
}