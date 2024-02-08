# dlq-bug-reproducer

This is a reproducer project for a bug introduced in Quarkus 3.7 that affects Reactive Messaging consumers that
connect to a Confluent Cloud Kafka broker.

# Structure

This project has 2 sub-modules. The only difference between the two is the version of Quarkus:

- `quarkus-3.6` uses Quarkus 3.6.9. This was made to show that the example works in this version.
- `quarkus-3.7` uses Quarkus 3.7.1. This is the actual reproducer.

# Requirements

- A Confluent Cloud cluster
- JDK 17+
- Any tool that can produce/consume to/from kafka topics

# How to run

1. On your Confluent Cloud cluster:
    1. Create the topics `test-quarkus-bug` and `test-quarkus-bug-dlq` (or choose the names you prefer)
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