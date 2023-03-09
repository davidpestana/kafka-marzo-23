const { Kafka } = require('kafkajs')

const kafka = new Kafka({
  clientId: 'producer-1',
  brokers: ['broker1:9092', 'broker2:9092', 'broker3:9092'],
})

const consumer = kafka.consumer({ groupId: 'test-group' })




const listen = async () => { 
  await consumer.connect()
  await consumer.subscribe({ topic: 'test-topic', fromBeginning: true })

  await consumer.run({
    eachMessage: async ({ topic, partition, message }) => {
      console.log({
        value: message.value.toString(),
      })
    },
  })
}


listen().then();