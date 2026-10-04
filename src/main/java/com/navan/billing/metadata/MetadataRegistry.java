package com.navan.billing.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.navan.billing.domain.TxnType;
import com.navan.billing.entity.FlightMetadataEntity;
import com.navan.billing.entity.HotelMetadataEntity;
import com.navan.billing.entity.MetadataEntity;
import com.navan.billing.entity.NavanFeeMetadataEntity;
import com.navan.billing.entity.RailMetadataEntity;
import com.navan.billing.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class MetadataRegistry {
    private final ObjectMapper objectMapper;
    private final Map<TxnType, Class<? extends MetadataEntity>> metadataClasses;

    public MetadataRegistry(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        EnumMap<TxnType, Class<? extends MetadataEntity>> classes = new EnumMap<>(TxnType.class);
        classes.put(TxnType.flight, FlightMetadataEntity.class);
        classes.put(TxnType.hotel, HotelMetadataEntity.class);
        classes.put(TxnType.rail, RailMetadataEntity.class);
        classes.put(TxnType.navan_fee, NavanFeeMetadataEntity.class);
        for (TxnType type : TxnType.values()) {
            if (!classes.containsKey(type)) {
                throw new IllegalStateException("No metadata entity registered for " + type.wire());
            }
        }
        this.metadataClasses = Map.copyOf(classes);
    }

    public MetadataEntity parse(TxnType type, JsonNode node) {
        if (node == null || !node.isObject()) {
            throw invalidMetadata("metadata: must be a JSON object");
        }
        try {
            return objectMapper.treeToValue(node, classOf(type));
        } catch (JsonMappingException e) {
            throw invalidMetadata(path(e));
        } catch (JsonProcessingException e) {
            throw invalidMetadata("metadata: invalid value");
        }
    }

    public JsonNode toJson(MetadataEntity metadata) {
        return objectMapper.valueToTree(metadata);
    }

    public Class<? extends MetadataEntity> classOf(TxnType type) {
        Class<? extends MetadataEntity> result = metadataClasses.get(type);
        if (result == null) {
            throw new IllegalArgumentException("No metadata entity registered for " + type);
        }
        return result;
    }

    private String path(JsonMappingException exception) {
        StringBuilder result = new StringBuilder("metadata");
        List<Reference> references = exception.getPath();
        for (Reference reference : references) {
            if (reference.getFieldName() != null) {
                result.append('.').append(reference.getFieldName());
            } else if (reference.getIndex() >= 0) {
                result.append('[').append(reference.getIndex()).append(']');
            }
        }
        return result.append(": invalid value").toString();
    }

    private ApiException invalidMetadata(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "invalid_metadata", message, Map.of());
    }
}
