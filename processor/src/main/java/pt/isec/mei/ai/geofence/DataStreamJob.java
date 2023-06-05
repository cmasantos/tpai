package pt.isec.mei.ai.geofence;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import pt.isec.mei.ai.geofence.deserializer.SensorLocationDeserializer;
import pt.isec.mei.ai.geofence.model.GeofenceEvent;
import pt.isec.mei.ai.geofence.model.SensorLocation;
import pt.isec.mei.ai.geofence.operators.GeofenceOperator;
import pt.isec.mei.ai.geofence.serializer.GeofenceEventSerializer;

import java.io.IOException;


public class DataStreamJob {

	public static void main(String[] args) throws Exception {
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setParallelism(2);

		KafkaSource<SensorLocation> kafkaSource = KafkaSource.<SensorLocation>builder()
				.setBootstrapServers("broker:9092")
				.setTopics("sensor-location-events")
				.setGroupId("processor-group")
				.setStartingOffsets(OffsetsInitializer.latest())
				.setDeserializer(new SensorLocationDeserializer())
				.build();

		KafkaSink<GeofenceEvent> sink = KafkaSink.<GeofenceEvent>builder()
				.setBootstrapServers("broker:9092")
				.setRecordSerializer(KafkaRecordSerializationSchema.builder()
						.setTopic("geofencing-events")
						.setValueSerializationSchema(new GeofenceEventSerializer())
						.build())
				.setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
				.build();


		DataStream<GeofenceEvent> dataStream =
				env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source")
				.keyBy(SensorLocation::getSensorId)
				.process(new GeofenceOperator())
				.uid("geofence_operator_v1")
				.name("geofence_operator_v1");

		dataStream.sinkTo(sink)
				.uid("geofence_event_sink_v1")
				.name("geofence_event_sink_v1");

		dataStream.sinkTo(new Sink<GeofenceEvent>() {
			@Override
			public SinkWriter<GeofenceEvent> createWriter(InitContext context) throws IOException {
				return new SinkWriter<GeofenceEvent>() {
					@Override
					public void write(GeofenceEvent element, Context context) throws IOException, InterruptedException {
						System.out.println(element);
					}

					@Override
					public void flush(boolean endOfInput) throws IOException, InterruptedException {

					}

					@Override
					public void close() throws Exception {

					}
				};
			}
		});

 		env.execute("geofencing");
	}
}
