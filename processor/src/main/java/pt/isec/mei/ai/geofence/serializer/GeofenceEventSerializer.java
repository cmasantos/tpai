package pt.isec.mei.ai.geofence.serializer;

import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import pt.isec.mei.ai.geofence.model.GeofenceEvent;

public class GeofenceEventSerializer implements SerializationSchema<GeofenceEvent> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public byte[] serialize(GeofenceEvent element) {
        try {
            return MAPPER.writeValueAsBytes(element);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
