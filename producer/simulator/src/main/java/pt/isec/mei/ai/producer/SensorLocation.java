package pt.isec.mei.ai.producer;

import java.io.Serializable;

public class SensorLocation implements Serializable {

    public static final long serialVersionUID = 1L;

    private String sensorId;

    private Double latitude;

    private Double longitude;

    public SensorLocation() {
        //Empty by design
    }

    public SensorLocation(String sensorId, Double latitude, Double longitude) {
        this.sensorId = sensorId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "SensorLocation{" +
                "sensorId='" + sensorId + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                '}';
    }
}
