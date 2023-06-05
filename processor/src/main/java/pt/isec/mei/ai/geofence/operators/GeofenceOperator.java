package pt.isec.mei.ai.geofence.operators;

import com.uber.h3core.H3Core;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import pt.isec.mei.ai.geofence.enums.GeofenceEventTypes;
import pt.isec.mei.ai.geofence.model.GeofenceEvent;
import pt.isec.mei.ai.geofence.model.SensorLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;

public class GeofenceOperator extends KeyedProcessFunction<String, SensorLocation, GeofenceEvent> {

    private transient Map<String, Integer> geofenceZones;

    private transient ValueState<Tuple2<Boolean, String>> vehicleState;

    private transient H3Core h3;

    @Override
    public void open(Configuration parameters) throws Exception {
        h3 = H3Core.newInstance();

        //1. load geofence <h3 id, cluster id>
        try(var lines = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("geofencezones.csv")))).lines()) {
            geofenceZones = lines.distinct()
                    .map(line -> line.split(","))
                    .collect(
                            toMap(
                                    line -> line[0],
                                    line -> Integer.parseInt(line[1]),
                                    (zone1, zone2) -> zone1));
        }

        //2. create the value state
        ValueStateDescriptor<Tuple2<Boolean, String>> vehicleStateDescriptor
                = new ValueStateDescriptor<>(
                        "vehicleState",
                        TypeInformation.of(new TypeHint<Tuple2<Boolean, String>>() {}),
                        Tuple2.of(Boolean.FALSE, "default"));

        vehicleState = getRuntimeContext().getState(vehicleStateDescriptor);
    }

    @Override
    public void processElement(SensorLocation sensorLocation, KeyedProcessFunction<String, SensorLocation, GeofenceEvent>.Context ctx, Collector<GeofenceEvent> out) throws IOException {

        final String currentCell = h3.latLngToCellAddress(sensorLocation.getLatitude(), sensorLocation.getLongitude(), 12);

        Boolean vehicleIsInZone = Boolean.FALSE;

        if(geofenceZones.containsKey(currentCell)) {
            vehicleIsInZone = Boolean.TRUE;
        }


        final Tuple2<Boolean, String> vehicleStateInfo = vehicleState.value();

        if(vehicleWasInAZone()) {
            if(vehicleIsInZone) {
                if(!isItSameZone(currentCell)) { // vehicle has left and entered a new zone
                    out.collect(toLeftEvent(ctx.getCurrentKey(), vehicleStateInfo.f1));
                    out.collect(toEnterEvent(ctx.getCurrentKey(), currentCell));

                    vehicleState.update(Tuple2.of(Boolean.TRUE, currentCell));
                }
            } else { //vehicle has left a zone
                out.collect(toLeftEvent(ctx.getCurrentKey(), vehicleStateInfo.f1));

                vehicleState.update(Tuple2.of(Boolean.FALSE, vehicleStateInfo.f1));
            }
        } else { // vehicle was no in a zone
            if(vehicleIsInZone){ // vehicle has entered a new zone
                out.collect(toEnterEvent(ctx.getCurrentKey(), currentCell));

                vehicleState.update(Tuple2.of(Boolean.TRUE, currentCell));
            }
        }
    }

    private boolean vehicleWasInAZone() throws IOException {
        return vehicleState.value().f0;
    }

    private boolean isItSameZone(final String currentZone) throws IOException {
        final String oldZone = vehicleState.value().f1;

        return oldZone.equals(currentZone);
    }

    private GeofenceEvent toLeftEvent(final String sensorId, final String zoneId) {
        return toEvent(sensorId, zoneId, GeofenceEventTypes.LEFT);
    }

    private GeofenceEvent toEnterEvent(final String sensorId, final String zoneId) {
        return toEvent(sensorId, zoneId, GeofenceEventTypes.ENTER);
    }

    private GeofenceEvent toEvent(final String sensorId, final String zoneId, final GeofenceEventTypes eventType) {
        return new GeofenceEvent(sensorId, Instant.now().toEpochMilli(), eventType.name(), zoneId);
    }
}
