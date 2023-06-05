package pt.isec.mei.ai.geofence.deserializer;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import pt.isec.mei.ai.geofence.model.SensorLocation;

import java.io.IOException;

public class SensorLocationDeserializer implements KafkaRecordDeserializationSchema<SensorLocation> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void deserialize(ConsumerRecord<byte[], byte[]> consumerRecord, Collector<SensorLocation> collector) throws IOException {
        SensorLocation sensorLocation = MAPPER.readValue(consumerRecord.value(), SensorLocation.class);
        collector.collect(sensorLocation);
    }

    @Override
    public TypeInformation<SensorLocation> getProducedType() {
        return TypeInformation.of(SensorLocation.class);
    }
}
