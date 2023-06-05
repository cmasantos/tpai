package pt.isec.mei.ai.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
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

       final var coords = Map.of(1.1, 1.1, 38.757438, -9.163719);

       Runnable runnable = () -> {
           System.out.println("starting");
           var id = UUID.randomUUID().toString();
           int i = 0;
           Random random = new Random();
           while (i< 1000) {
               i++;

               var lat = 1.1;
               var lng = 1.1;

                if(random.nextInt(1,101) > 70) {
                    lat = 38.757438;
                    lng = -9.163719;
                }

                var payload = """
                                  {
                                       "sensorId"  : "%s",
                                       "latitude"  : %f,
                                       "longitude" : %f
                                  }
                               """.formatted(id, lat, lng);
               var record = new ProducerRecord<String, String>(
                       "sensor-location-events",
                       "1",
                       payload);
               producer.send(record);

               try {
                   Thread.sleep(1000);
               } catch (InterruptedException e) {
               }

           }
           System.out.println("finishing");
       };

        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

        IntStream.range(0, 10000).forEach(i -> executorService.submit(runnable));

           executorService.shutdown();
            try {

                executorService.awaitTermination(1, TimeUnit.MINUTES);
            }catch (InterruptedException e) {
            }

        System.out.println("Message sent successfully");
        producer.close();
    }

}
