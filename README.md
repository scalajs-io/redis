Redis API for Scala.js
================================
[redis](http://redis.github.io/node-redis-native/2.2/api/) - This is a complete and feature rich Redis client for node.js.

### Description

This is a complete and feature rich Redis client for node.js. It supports all Redis commands 
and focuses on high performance.

<a name="build_requirements"></a>
### Build Requirements

* [SBT v0.13.13](http://www.scala-sbt.org/download.html)

<a name="building_sdk"></a>
### Build/publish the SDK locally

```bash
 $ sbt clean publish-local
```

### Running the tests

Before running the tests the first time, you must ensure the npm packages are installed:

```bash
$ npm install
```

Then you can run the tests:

```bash
$ sbt test
```

### Examples

##### Reading and writing hashes

```scala
import io.scalajs.JSON
import io.scalajs.nodejs._
import io.scalajs.npm.redis._
import io.scalajs.util.ScalaJsHelper._
import scalajs.js

val client = Redis.createClient(new RedisClientOptions())
client.hmset("hosts", "mjr", "1", "another", "23", "home", "1234")
client.hgetall("hosts", (err, obj) => {
assert(!isDefined(err))
  console.log(JSON.stringify(obj)) // { mjr: '1', another: '23', home: '1234' }
})
```

##### Publish and Subscribe

```scala
import io.scalajs.nodejs._
import io.scalajs.npm.redis._

val sub = Redis.createClient()
val pub = Redis.createClient()
var msg_count = 0

sub.onSubscribe((channel, count) => {
    pub.publish("a nice channel", "I am sending a message.")
    pub.publish("a nice channel", "I am sending a second message.")
    pub.publish("a nice channel", "I am sending my last message.")
})

sub.onMessage((channel, message) => {
    console.log("sub channel " + channel + ": " + message)
    msg_count += 1
    if (msg_count == 3) {
      sub.unsubscribe()
      sub.quit()
      pub.quit()
    }
})

sub.subscribe("a nice channel")
```

##### Monitoring

```scala
import io.scalajs.nodejs._
import io.scalajs.npm.redis._

val client = Redis.createClient()
client.monitor((err, res) => {
    console.log("Entering monitoring mode.")
})
client.set("foo", "bar")

client.onMonitor((time, args, raw_reply) => {
    console.log(time + ": " + args) // 1458910076.446514:['set', 'foo', 'bar'] 
})
```

### Artifacts and Resolvers

To add the `Redis` binding to your project, add the following to your build.sbt:  

```sbt
libraryDependencies += "io.scalajs.npm" %%% "redis" % "0.4.0-pre5"
```

Optionally, you may add the Sonatype Repository resolver:

```sbt   
resolvers += Resolver.sonatypeRepo("releases") 
```
