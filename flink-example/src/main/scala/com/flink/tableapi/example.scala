package com.flink.tableapi

import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.datastream.DataStreamSink
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api._
import org.apache.flink.table.api.scala._
/**
  * Created by $maoshuwu on 2020/11/18.
  */
object example {



  def main(args: Array[String]): Unit = {
    val env=StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    //读取数据
     val path="D:\\IdeaProjects\\flinkmsw\\flink-example\\src\\main\\resources\\sense"
     val inputStream: DataStream[String] = env.readTextFile(path)
    val dataSteam: DataStream[SensorReading] = inputStream.map(data => {
      val arr = data.split(",")
      SensorReading(arr(0), arr(1).toLong, arr(2).toDouble)
    })
//    dataSteam.print("re")
//    创建环境Table
    val tabeEnv = StreamTableEnvironment.create(env)
//    //基于流创建一张表
    val datatable: Table = tabeEnv.fromDataStream(dataSteam)
//    //调用table api 进行转换
//    val resulttable: Table = datatable
//        .select("id,temps")
//      .filter("id='sens_1'")

////
//    resulttable.toAppendStream[(String,Double)].print("result")
     tabeEnv.createTemporaryView("datatable",datatable)
    val sql = "select id,temps from datatable where id='sens_1'"
    val resultsqltable: Table = tabeEnv.sqlQuery(sql)
    resultsqltable.toAppendStream[(String,Double)].print("result")

//    //1,1基于老版本planner的流处理
//    val settings=EnvironmentSettings.newInstance().useOldPlanner()
//      .inStreamingMode()
//      .build()
//
//    val oldstreamTableEnvironment: StreamTableEnvironment = StreamTableEnvironment.create(env,settings)
//    //1,1基于老版本planner的批处理
//      val batchenv: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
//    val oldbatchenv=BatchTableEnvironment.create(batchenv)
//    //1.3基于blink planner的流处理
//    val blinkstreamsettings=EnvironmentSettings.newInstance().useBlinkPlanner()
//      .inStreamingMode()
//      .build()
//    val blinkstreamTableEnvironment: StreamTableEnvironment = StreamTableEnvironment.create(env,blinkstreamsettings)
//
//    //1.4基于blink 批的流处理
//    val blinkstreamsettingsbatch=EnvironmentSettings.newInstance().useBlinkPlanner()
//        .inBatchMode()
//      .build()
//    val blinkbatchstreamTableEnvironment: TableEnvironment = TableEnvironment.create(blinkstreamsettingsbatch)

    env.execute("table api")
  }
}
