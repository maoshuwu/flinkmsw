package com.flink.tableapi

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.apache.flink.table.descriptors.Kafka

/**
  * Created by $maoshuwu on 2020/11/19.
  */
object tableApi {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
      env.setParallelism(1)
     val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(env)
    //1.1从kafak读数据
    tableEnv.connect(new Kafka()
      .version("")
        .topic("")
          .property("","")
    ).withFormat(new Csv())
      .withFormat()


  }

}
