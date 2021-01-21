package com.flink

import org.apache.flink.api.scala.{DataSet, ExecutionEnvironment}

/**
  * Created by $maoshuwu on 2020/11/30.
  */
object catchexaple {

  def main(args: Array[String]): Unit = {
    val env = ExecutionEnvironment.getExecutionEnvironment

    // register a file from HDFS
    env.registerCachedFile("hdfs:///path/to/your/file", "hdfsFile")

    // register a local executable file (script, executable, ...)
    env.registerCachedFile("file:///path/to/exec/file", "localExecFile", true)

    // define your program and execute
//    ...
//    val input: DataSet[String] = ...
//    val result: DataSet[Integer] = input.map(new MyMapper())
//    ...
    env.execute()
  }

}
