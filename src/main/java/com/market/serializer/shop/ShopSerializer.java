package com.market.serializer.shop;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.market.model.Shop;
import org.hibernate.Hibernate;

import java.io.IOException;

public class ShopSerializer extends JsonSerializer<Shop> {

    @Override
    public void serialize(Shop shop, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        if (shop != null) {
            // Always write basic fields
            gen.writeNumberField("id", shop.getId());
            gen.writeStringField("name", shop.getName());
            gen.writeStringField("itemCount", String.valueOf(shop.getItemCount()));

            if (shop.getDescription() != null) {
                gen.writeStringField("description", shop.getDescription());
            }
            if (shop.getAddress() != null) {
                gen.writeStringField("address", shop.getAddress());
            }
            if (shop.getPhone() != null) {
                gen.writeStringField("phone", shop.getPhone());
            }
            if (shop.getItemLimit() != null) {
                gen.writeNumberField("itemLimit", shop.getItemLimit());
            }
            if (shop.getImageKey() != null) {
                gen.writeStringField("imageKey", shop.getImageKey());
            }

            // Handle lazy-loaded relationships
            // Category
            if (shop.getCategory() != null) {
                gen.writeFieldName("category");
                if (Hibernate.isInitialized(shop.getCategory())) {
                    gen.writeStartObject();
                    gen.writeNumberField("id", shop.getCategory().getId());
                    gen.writeStringField("name", shop.getCategory().getName());
                    gen.writeEndObject();
                } else {
                    try {
                        gen.writeStartObject();
                        gen.writeNumberField("id", shop.getCategory().getId());
                        gen.writeStringField("name", shop.getCategory().getName());
                        gen.writeEndObject();
                    } catch (Exception e) {
                        gen.writeStartObject();
                        gen.writeNumberField("id", shop.getCategory().getId());
                        gen.writeNullField("name");
                        gen.writeEndObject();
                    }
                }
            }

            // Town
            if (shop.getTown() != null) {
                gen.writeFieldName("town");
                if (Hibernate.isInitialized(shop.getTown())) {
                    gen.writeStartObject();
                    gen.writeNumberField("id", shop.getTown().getId());
                    gen.writeStringField("name", shop.getTown().getName());
                    gen.writeEndObject();
                } else {
                    try {
                        gen.writeStartObject();
                        gen.writeNumberField("id", shop.getTown().getId());
                        gen.writeStringField("name", shop.getTown().getName());
                        gen.writeEndObject();
                    } catch (Exception e) {
                        gen.writeStartObject();
                        gen.writeNumberField("id", shop.getTown().getId());
                        gen.writeNullField("name");
                        gen.writeEndObject();
                    }
                }
            }

            // Owner (User)
            if (shop.getOwner() != null) {
                gen.writeFieldName("owner");
                if (Hibernate.isInitialized(shop.getOwner())) {
                    gen.writeStartObject();
                    gen.writeNumberField("id", shop.getOwner().getId());
                    gen.writeStringField("username", shop.getOwner().getUsername());
                    gen.writeStringField("phone", shop.getOwner().getPhone());
                    gen.writeEndObject();
                } else {
                    try {
                        gen.writeStartObject();
                        gen.writeNumberField("id", shop.getOwner().getId());
                        gen.writeStringField("username", shop.getOwner().getUsername());
                        gen.writeStringField("phone", shop.getOwner().getPhone());
                        gen.writeEndObject();
                    } catch (Exception e) {
                        gen.writeStartObject();
                        gen.writeNumberField("id", shop.getOwner().getId());
                        gen.writeNullField("username");
                        gen.writeEndObject();
                    }
                }
            }

            // Timestamps
            if (shop.getCreatedAt() != null) {
                gen.writeStringField("createdAt", shop.getCreatedAt().toString());
            }
            if (shop.getUpdatedAt() != null) {
                gen.writeStringField("updatedAt", shop.getUpdatedAt().toString());
            }
        }

        gen.writeEndObject();
    }
}
