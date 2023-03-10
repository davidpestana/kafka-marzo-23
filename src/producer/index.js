const { Kafka } = require('kafkajs')
const { SchemaRegistry } = require('@kafkajs/confluent-schema-registry')

const kafka = new Kafka({
  clientId: 'producer-1',
  brokers: ['broker1:9092', 'broker2:9092', 'broker3:9092'],
})

const registry = new SchemaRegistry({ host: 'http://schema-registry:8081/' })


const producer = kafka.producer()

const sender = async (number) => {
    await producer.connect()
    await producer.send({
    topic: 'test-topic',
    messages: [
        { value: 'Hello KafkaJS user! ' + number },
    ],
    })

    await producer.disconnect()  
}

let counter = 0; 
setInterval(() => sender(counter++), 100);