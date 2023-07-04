package pt.isec.mei.ai.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {
        System.out.println("start at " + Instant.now());

        Properties props = new Properties();

        props.put("bootstrap.servers", "localhost:29092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");

       final Producer<String, String> producer = new KafkaProducer<String, String>(props);

       var id = new AtomicInteger(1);

       final var coords = getCoords();

       Runnable runnable = () -> {
           var sensorId = id.getAndIncrement();

           var sensorKey = "s" + sensorId;

           System.out.println("starting " + sensorId);

           int i = 0;
           Random random = new Random();
           while (i< 300) {
               i++;

               if(sensorId == 1)
                   System.out.println("i " + i);

               var lat = "1.1";
               var lng = "1.1";

                if(random.nextInt(1,101) > 90) {
                    int index = random.nextInt(0, coords.size());
                    var gps = coords.get(index);
                    lat = gps.getLat();
                    lng = gps.getLng();
                }

                var payload = """
                                  {
                                       "sensorId"  : "%s",
                                       "latitude"  : %s,
                                       "longitude" : %s
                                  }
                               """.formatted(sensorId, lat, lng);

                var record = new ProducerRecord<String, String>(
                       "sensor-location-events",
                        sensorKey,
                       payload);

               producer.send(record);

               try {
                   Thread.sleep(1000);
               } catch (InterruptedException e) {
               }

           }
           System.out.println("finishing " + sensorId);
       };

        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

        IntStream.range(0, 100000).forEach(i -> executorService.submit(runnable));

           executorService.shutdown();
            try {

                executorService.awaitTermination(10, TimeUnit.MINUTES);
            }catch (InterruptedException e) {
            }

        System.out.println("Message sent successfully");
        producer.close();

        System.out.println("Stop at " + Instant.now());
    }

    private static List<Gps> getCoords() throws IOException {

        Path path = Paths.get("src/main/resources/valid_gps.csv");

        return Files.readAllLines(path)
                .stream()
                .skip(1)
                .map(line -> line.split(","))
                .map(line -> new Gps(line[0], line[1]))
                .collect(toList());
    }
}
