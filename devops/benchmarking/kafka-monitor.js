/*

This is a k6 test script that imports the xk6-kafka and
tests Kafka with a 200 JSON messages per iteration.

*/

import { check } from "k6";
// import * as kafka from "k6/x/kafka";
import {
  Reader,
  Connection,
  SchemaRegistry,
  SCHEMA_TYPE_STRING,
} from "k6/x/kafka"; // import kafka extension

// Prints module-level constants
// console.log(kafka);

const brokers = ["kafka-01:9092"];
const topic = "JeMPI-interaction-processor-controller";

const reader = new Reader({
  brokers: brokers,
  topic: topic,
});
const connection = new Connection({
  address: brokers[0],
});
const schemaRegistry = new SchemaRegistry();

if (__VU == 0) {
  connection.createTopic({ topic: topic });
}

export const options = {
  thresholds: {
    // Base thresholds to see if the writer or reader is working
    kafka_reader_error_count: ["count == 0"],
  },
};

export default function () {
  // Read 10 messages only
  let messages = reader.consume({ limit: 10 });

  check(messages, {
    "10 messages are received": (messages) => messages.length == 10,
  });

  check(messages[0], {
    "Topic equals to xk6_kafka_json_topic": (msg) => msg["topic"] == topic,
    "Key is a string and is correct": (msg) =>
      schemaRegistry.deserialize({
        data: msg.key,
        schemaType: SCHEMA_TYPE_STRING,
      }) == "test-key-string",
    "Value is a string and is correct": (msg) =>
      typeof schemaRegistry.deserialize({
        data: msg.value,
        schemaType: SCHEMA_TYPE_STRING,
      }) == "string" &&
      schemaRegistry.deserialize({
        data: msg.value,
        schemaType: SCHEMA_TYPE_STRING,
      }) == "test-value-string",
    "Header equals {'mykey': 'myvalue'}": (msg) =>
      "mykey" in msg.headers &&
      String.fromCharCode(...msg.headers["mykey"]) == "myvalue",
    "Time is past": (msg) => new Date(msg["time"]) < new Date(),
    "Partition is zero": (msg) => msg["partition"] == 0,
    "Offset is gte zero": (msg) => msg["offset"] >= 0,
    "High watermark is gte zero": (msg) => msg["highWaterMark"] >= 0,
  });
}

export function teardown(data) {
  if (__VU == 0) {
    // Delete the topic
    connection.deleteTopic(topic);
  }
  reader.close();
  connection.close();
}
