const { Kafka } = require('kafkajs')

const kafka = new Kafka({
  clientId: 'producer-1',
  brokers: ['broker1:9092', 'broker2:9092', 'broker3:9092'],
})

console.log('todo implement consumer');