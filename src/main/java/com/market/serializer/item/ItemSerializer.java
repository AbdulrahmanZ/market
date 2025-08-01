package com.market.serializer.item;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.market.model.Item;
import org.hibernate.Hibernate;

import java.io.IOException;

public class ItemSerializer extends JsonSerializer<Item> {

    @Override
    public void serialize(Item item, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        
        if (item != null) {
            // Always write basic fields
            gen.writeNumberField("id", item.getId());
            
            if (item.getDescription() != null) {
                gen.writeStringField("description", item.getDescription());
            }
            if (item.getPrice() != null) {
                gen.writeNumberField("price", item.getPrice());
            }
            if (item.getMediaUrl() != null) {
                gen.writeStringField("mediaUrl", item.getMediaUrl());
            }
            if (item.getMediaType() != null) {
                gen.writeStringField("mediaType", item.getMediaType().toString());
            }
            
            // Handle lazy-loaded Shop relationship
            if (item.getShop() != null) {
                gen.writeFieldName("shop");
                if (Hibernate.isInitialized(item.getShop())) {
                    gen.writeStartObject();
                    gen.writeNumberField("id", item.getShop().getId());
                    gen.writeStringField("name", item.getShop().getName());
                    if (item.getShop().getDescription() != null) {
                        gen.writeStringField("description", item.getShop().getDescription());
                    }
                    if (item.getShop().getAddress() != null) {
                        gen.writeStringField("address", item.getShop().getAddress());
                    }
                    if (item.getShop().getPhone() != null) {
                        gen.writeStringField("phone", item.getShop().getPhone());
                    }
                    if (item.getShop().getProfileImageUrl() != null) {
                        gen.writeStringField("profileImageUrl", item.getShop().getProfileImageUrl());
                    }
                    
                    // Include shop's category and town if available
                    if (item.getShop().getCategory() != null) {
                        gen.writeFieldName("category");
                        gen.writeStartObject();
                        gen.writeNumberField("id", item.getShop().getCategory().getId());
                        try {
                            gen.writeStringField("name", item.getShop().getCategory().getName());
                        } catch (Exception e) {
                            gen.writeNullField("name");
                        }
                        gen.writeEndObject();
                    }
                    
                    if (item.getShop().getTown() != null) {
                        gen.writeFieldName("town");
                        gen.writeStartObject();
                        gen.writeNumberField("id", item.getShop().getTown().getId());
                        try {
                            gen.writeStringField("name", item.getShop().getTown().getName());
                        } catch (Exception e) {
                            gen.writeNullField("name");
                        }
                        gen.writeEndObject();
                    }
                    
                    gen.writeEndObject();
                } else {
                    try {
                        gen.writeStartObject();
                        gen.writeNumberField("id", item.getShop().getId());
                        gen.writeStringField("name", item.getShop().getName());
                        if (item.getShop().getDescription() != null) {
                            gen.writeStringField("description", item.getShop().getDescription());
                        }
                        gen.writeEndObject();
                    } catch (Exception e) {
                        gen.writeStartObject();
                        gen.writeNumberField("id", item.getShop().getId());
                        gen.writeNullField("name");
                        gen.writeEndObject();
                    }
                }
            }
            
            // Timestamps
            if (item.getCreatedAt() != null) {
                gen.writeStringField("createdAt", item.getCreatedAt().toString());
            }
            if (item.getUpdatedAt() != null) {
                gen.writeStringField("updatedAt", item.getUpdatedAt().toString());
            }
        }
        
        gen.writeEndObject();
    }
}
