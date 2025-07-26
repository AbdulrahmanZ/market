package com.market.serializer.town;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.market.model.Town;
import org.hibernate.Hibernate;

import java.io.IOException;

public class TownSerializer extends JsonSerializer<Town> {

    @Override
    public void serialize(Town town, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        
        if (town != null) {
            // Always write the ID
            gen.writeNumberField("id", town.getId());
            
            // Only write name if the proxy is initialized or force initialization
            if (Hibernate.isInitialized(town)) {
                gen.writeStringField("name", town.getName());
            } else {
                // Force initialization and write name
                try {
                    gen.writeStringField("name", town.getName());
                } catch (Exception e) {
                    // If initialization fails, just write the ID
                    gen.writeNullField("name");
                }
            }
        }
        
        gen.writeEndObject();
    }
}
