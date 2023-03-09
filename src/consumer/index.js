const { Kafka } = require('kafkajs')

const kafka = new Kafka({
  clientId: 'producer-1',
  brokers: ['broker1:9092', 'broker2:9092', 'broker3:9092'],
})

const consumer = kafka.consumer({ groupId: 'test-group' })




const listen = async () => { 
  await consumer.connect()
  await consumer.subscribe({ topic: 'ejemplo', fromBeginning: true })

  await consumer.run({
    eachMessage: async ({ topic, partition, message }) => {
      console.log(topic, partition, message.value.toString())
    },
  })
}


listen().then();