package io.scalajs.npm.redis

import io.scalajs.JSON
import io.scalajs.nodejs.buffer.Buffer
import io.scalajs.util.ScalaJsHelper._
import org.scalatest.FunSpec

import scala.scalajs.js

/**
  * Redis Client Test Suite
  * @author lawrence.daniels@gmail.com
  */
class RedisClientTest extends FunSpec {

  describe("RedisClient") {

    it("supports buffers and strings") {
      val client = Redis.createClient(new RedisClientOptions(detect_buffers = true))
      client.set("foo_rand000000000000", "OK")

      // This will return a JavaScript String 
      client.get("foo_rand000000000000", (err, reply) => {
        info(reply.toString()) // Will print `OK` 
      })

      // This will return a Buffer since original key is specified as a Buffer 
      client.get(Buffer.from("foo_rand000000000000"), (err, reply) => {
        info(reply.toString()) // Will print `<Buffer 4f 4b>` 
      })
      client.quit()
    }

    it("supports hashes") {
      val client = Redis.createClient(new RedisClientOptions())
      client.hmset("hosts", "mjr", "1", "another", "23", "home", "1234")
      client.hgetall("hosts", (err, obj) => {
        assert(!isDefined(err))
        info(JSON.stringify(obj)) // { mjr: '1', another: '23', home: '1234' }
      })
    }

    it("supports monitoring") {
      val client = Redis.createClient()
      client.monitor((err, res) => {
        info("Entering monitoring mode.")
      })
      client.set("foo", "bar")

      client.onMonitor((time, args, raw_reply) => {
        info(time + ": " + args) // 1458910076.446514:['set', 'foo', 'bar'] 
      })
    }

    it("supports multi-commands") {
      val client = Redis.createClient()
      val multi = client.multi()

      // start a separate multi command queue
      multi.incr("incr thing", Redis.print)
      multi.incr("incr other thing", Redis.print)

      // runs immediately
      client.mset(js.Array("incr thing", 100, "incr other thing", 1), Redis.print)

      // drains multi queue and runs atomically
      multi.exec((err, replies) => {
        info(JSON.stringify(replies)) // 101, 2
      })
    }

    it("supports publish / subscribe") {
      val sub = Redis.createClient()
      val pub = Redis.createClient()
      var msg_count = 0

      sub.onSubscribe((channel, count) => {
        pub.publish("a nice channel", "I am sending a message.")
        pub.publish("a nice channel", "I am sending a second message.")
        pub.publish("a nice channel", "I am sending my last message.")
      })

      sub.onMessage((channel, message) => {
        info("sub channel " + channel + ": " + message)
        msg_count += 1
        if (msg_count == 3) {
          sub.unsubscribe()
          sub.quit()
          pub.quit()
        }
      })

      sub.subscribe("a nice channel")
    }

  }

}
