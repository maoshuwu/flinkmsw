package com.flink

import java.util

import com.flink.tableapi.SensorReading
import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.elasticsearch.{ElasticsearchSinkFunction, RequestIndexer}
import org.apache.flink.streaming.connectors.elasticsearch6.ElasticsearchSink
import org.apache.http.HttpHost
import org.elasticsearch.ElasticsearchException
import org.elasticsearch.client.Requests

/**
  * Created by $maoshuwu on 2020/11/20.
  */
package object sinkES {
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
  //定义
    val httpHosts=new util.ArrayList[HttpHost]()
    httpHosts.add(new HttpHost("",9200))
    //定义写入es的sink
    val myEsSinkFunc =new ElasticsearchSinkFunction[SensorReading]{
      override def process(t: SensorReading, runtimeContext: RuntimeContext, requestIndexer: RequestIndexer): Unit = {
        //包装一个map作为data source
        val dataSource =new util.HashMap[String,String]()
        dataSource.put("id",t.id)
        dataSource.put("timestamps",t.timestamps.toString)
        dataSource.put("temps",t.temps.toString)
        //创建index requst，用于发送http请求
        val insexRequest=Requests.indexRequest()
          .index("sensor")
          .`type`("redaingdata")
          .source(dataSource)
        //用indexer发送请求
        requestIndexer.add(insexRequest)

      }
    }
    dataSteam.addSink(new ElasticsearchSink
    .Builder[SensorReading](httpHosts,myEsSinkFunc)
        .build())


  }

}
