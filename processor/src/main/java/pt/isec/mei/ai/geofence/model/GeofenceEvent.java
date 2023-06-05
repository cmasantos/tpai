package pt.isec.mei.ai.geofence.model;

public class GeofenceEvent {

    public static final long serialVersionUID = 1L;


    private String sensorId;

    private Long timestamp;

    private String type;

    private String zoneId;

    public GeofenceEvent() {
    }

    public GeofenceEvent(String sensorId, Long timestamp, String type, String zoneId) {
        this.sensorId = sensorId;
        this.timestamp = timestamp;
        this.type = type;
        this.zoneId = zoneId;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    @Override
    public String toString() {
        return "GeofenceEvent{" +
                "sensorId='" + sensorId + '\'' +
                ", timestamp=" + timestamp +
                ", type='" + type + '\'' +
                ", zoneId='" + zoneId + '\'' +
                '}';
    }
}
