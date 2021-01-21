package com.flink.api

import com.flink.tableapi.SensorReading
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.{EventTimeSessionWindows, SlidingEventTimeWindows, TumblingEventTimeWindows}
import org.apache.flink.streaming.api.windowing.time.Time
/**
  * Created by $maoshuwu on 2020/11/23.
  */
object windows {
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

//    dataSteam.map(data =>(data.id,data.temps))
//      .keyBy(_._1)
////      .window(TumblingEventTimeWindows.of(Time.seconds(15)))//滚动
////      .window(SlidingEventTimeWindows.of(Time.seconds(15),Time.seconds(10)))//滑动
////      .window(EventTimeSessionWindows.withGap(Time.seconds(15)))//会话
////      .timeWindow(Time.seconds(15))
//      .countWindow()

  }

}
