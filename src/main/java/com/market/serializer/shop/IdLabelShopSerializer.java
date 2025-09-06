package com.market.serializer.shop;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.market.model.Shop;

import java.io.IOException;

public class IdLabelShopSerializer extends JsonSerializer<Shop> {

    @Override
    public void serialize(Shop shop, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        if (shop != null) {
            // Always write basic fields
            gen.writeNumberField("id", shop.getId());
            gen.writeStringField("name", shop.getName());
        }

        gen.writeEndObject();
    }
}

