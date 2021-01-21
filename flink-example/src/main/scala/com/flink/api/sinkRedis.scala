package com.flink.api

import com.flink.tableapi.SensorReading
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.{FlinkJedisPoolConfig, FlinkJedisSentinelConfig}
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}

/**
  * Created by $maoshuwu on 2020/11/20.
  */
object sinkRedis {
  def main(args: Array[String]): Unit = {
    val env=StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    //读取数据
    val path="D:\\IdeaProjects\\flinkmsw\\flink-example\\src\\main\\resources\\sense"
    val inputStream: DataStream[String] = env.readTextFile(path)
    val dataSteam: DataStream[SensorReading] = inputStream.map( data => {
      val arr = data.split(",")
      SensorReading(arr(0), arr(1).toLong, arr(2).toDouble)
    })
    //定义flinkJedisConfigBase
    val conf =new FlinkJedisPoolConfig.Builder()
         .setHost("localhost")
         .setPort(6379)
            .setPassword("123456")
      .build()

    dataSteam.addSink(new RedisSink[SensorReading](conf,new MyRedisMapper) )
    env.execute("redis")

  }

}

//定义一个redis  mapper
class MyRedisMapper extends RedisMapper[SensorReading]{

  //定义保存当前redis的命令
  override def getCommandDescription: RedisCommandDescription = {
   new RedisCommandDescription(RedisCommand.HSET, "sens_temps")
  }
 //将温度值指定为value
  override def getValueFromData(t: SensorReading): String =t.temps.toString
 //将ID指定为key
  override def getKeyFromData(t: SensorReading): String = t.id
}
