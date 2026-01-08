package com.iemr.common.utils.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Service
public class CallTypeMapper {
    private static GsonBuilder builder;
    private static Gson gsonInstance;
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public CallTypeMapper() {
        if (builder == null) {
            builder = new GsonBuilder();
            // Only serialize/deserialize fields with @Expose annotation
            builder.excludeFieldsWithoutExposeAnnotation();

            logger.info("CallTypeMapper initialized - Only @Expose fields will be processed");
        }
    }

    public static Gson gson() {
        if (gsonInstance == null) {
            gsonInstance = builder.create();
        }
        return gsonInstance;
    }

    public <T> T fromJson(String json, Class<T> classOfT) {
        try {
            T result = gson().fromJson(json, classOfT);
            logger.info("Successfully deserialized to class: {}", classOfT.getSimpleName());
            return result;
        } catch (Exception e) {
            logger.error("Error deserializing JSON to {}: {}", classOfT.getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }
}
