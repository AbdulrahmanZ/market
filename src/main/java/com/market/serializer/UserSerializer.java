package com.market.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.market.model.User;
import org.hibernate.Hibernate;

import java.io.IOException;

public class UserSerializer extends JsonSerializer<User> {

    @Override
    public void serialize(User user, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        
        if (user != null) {
            // Always write the ID
            gen.writeNumberField("id", user.getId());
            
            // Only write fields if the proxy is initialized or force initialization
            if (Hibernate.isInitialized(user)) {
                gen.writeStringField("username", user.getUsername());
                gen.writeStringField("phone", user.getPhone());
                gen.writeStringField("active", String.valueOf(user.getActive()));
                gen.writeStringField("admin", String.valueOf(user.getAdmin()));
                // Never serialize password for security
                if (user.getCreatedAt() != null) {
                    gen.writeStringField("createdAt", user.getCreatedAt().toString());
                }
                if (user.getUpdatedAt() != null) {
                    gen.writeStringField("updatedAt", user.getUpdatedAt().toString());
                }
            } else {
                // Force initialization and write fields
                try {
                    gen.writeStringField("username", user.getUsername());
                    gen.writeStringField("phone", user.getPhone());
                    gen.writeStringField("active", String.valueOf(user.getActive()));
                    gen.writeStringField("admin", String.valueOf(user.getAdmin()));
                    if (user.getCreatedAt() != null) {
                        gen.writeStringField("createdAt", user.getCreatedAt().toString());
                    }
                    if (user.getUpdatedAt() != null) {
                        gen.writeStringField("updatedAt", user.getUpdatedAt().toString());
                    }
                } catch (Exception e) {
                    // If initialization fails, just write the ID and username if possible
                    try {
                        gen.writeStringField("username", user.getUsername());
                    } catch (Exception ex) {
                        gen.writeNullField("username");
                    }
                }
            }
        }
        
        gen.writeEndObject();
    }
}
