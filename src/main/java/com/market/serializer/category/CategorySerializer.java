package com.market.serializer.category;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.market.model.Category;
import org.hibernate.Hibernate;

import java.io.IOException;

public class CategorySerializer extends JsonSerializer<Category> {

    @Override
    public void serialize(Category category, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        if (category != null) {
            // Always write the ID
            gen.writeNumberField("id", category.getId());

            // Only write name if the proxy is initialized or force initialization
            if (Hibernate.isInitialized(category)) {
                gen.writeStringField("name", category.getName());
            } else {
                // Force initialization and write name
                try {
                    gen.writeStringField("name", category.getName());
                } catch (Exception e) {
                    // If initialization fails, just write the ID
                    gen.writeNullField("name");
                }
            }
        }

        gen.writeEndObject();
    }
}
