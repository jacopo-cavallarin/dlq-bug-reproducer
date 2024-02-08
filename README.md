# dlq-bug-reproducer

This is a reproducer project for a bug introduced in Quarkus 3.7 that affects Reactive Messaging consumers that
connect to a Confluent Cloud Kafka broker using a Dead Letter Queue (DLQ) as failure strategy.

# Structure

This project has 2 submodules. Both contain exactly the same code and configuration properties.
The only difference between the two is the version of Quarkus:

- `quarkus-3.6` uses Quarkus 3.6.9. This was made to show that the example works in this version.
- `quarkus-3.7` uses Quarkus 3.7.1. This is the actual reproducer.

# Requirements

- A Confluent Cloud cluster
- JDK 17+
- Any tool that can produce/consume to/from kafka topics

# How to run

1. On your Confluent Cloud cluster:
    1. Create the topic `test-quarkus-bug` and the DLQ topic `test-quarkus-bug-dlq` (or choose the names you prefer)
    2. Create an API key that can access those topics
2. Set the following properties in the main [POM](pom.xml):
    1. `kafka.bootstrap.server`: replace `TODO` with your Confluent Cloud Kafka server address (including port)
    2. `confluent.api.key`: replace `TODO` with a valid API Key that can access your Confluent Cloud cluster.
    3. `confluent.api.secret`: replace `TODO` with the API Secret associated to the key set above.
    4. If you chose different topic names in the previous step, can change `topic.name` and `dlq.topic.name`
       to the names you used.
3. Run a submodule with one of the following commands:
   ```shell
     ./mvnw compile quarkus:dev --pl quarkus-3.6
   ```
   ```shell
     ./mvnw compile quarkus:dev --pl quarkus-3.7
   ```

# How to reproduce

The following steps can be followed on both submodules. In `quarkus-3.6` everything works fine,
in `quarkus-3.7` the application won't be able to connect to the DLQ.

1. Run the submodule and wait for the application to be ready.
2. Start a consumer on the DLQ topic with your kafka tool.
3. Send a message with the following payload to the main topic:
   ```json
   { "shouldAck" : true }
   ```
4. The application should successfully ack the message (both in `quarkus-3.6` and `quarkus-3.7`).
5. Now send the following payload to the same topic:
   ```json
   { "shouldAck" : false }
   ```
6. Depending on the submodule, this is what should happen:
    1. On `quarkus-3.6`, the message is nacked and sent to the DLQ. Your DLQ consumer should receive the nacked message.
    2. On `quarkus-3.7`, nack fails and no message is sent to the DLQ. After about 60 seconds, the following error
       message will be printed on the console:
       ```
       Topic test-quarkus-bug-dlq not present in metadata after 60000 ms.
       ```

An additional difference between the two submodules is that on `quarkus-3.7` the following warning will be printed
every couple of seconds:

```
WARN  [org.apa.kaf.cli.NetworkClient] (kafka-producer-network-thread | kafka-dead-letter-topic-producer-kafka-consumer-test-consumer) [Producer clientId=kafka-dead-letter-topic-producer-kafka-consumer-test-consumer] Bootstrap broker <your kafka bootstrap server> (id: -1 rack: null) disconnected
```
