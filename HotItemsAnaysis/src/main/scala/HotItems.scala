import java.sql.Timestamp

import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.api.java.tuple.{Tuple, Tuple1}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.WindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

import scala.collection.mutable.ListBuffer

/**
  * Created by $maoshuwu on 2020/12/2.
  */
/**
  * 定义输入数据样例类
  */
case class UserBehavior(userId:Long,itemId:Long ,categoryId:Int,behavior: String ,timestamp: Long)

//定义窗口聚合结果样列类
case class ItemViewCount(itemId:Long ,windowEnd:Long ,count: Long )
object HotItems {
  def main(args: Array[String]): Unit = {
  val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    val inputStream: DataStream[String] = env.readTextFile("D:\\IdeaProjects\\flinkmsw\\HotItemsAnaysis\\src\\main\\resources\\UserBehavior.csv")
    val dataStream: DataStream[UserBehavior] = inputStream.map(
      data => {
        val arr = data.split(",")
        UserBehavior(arr(0).toLong, arr(1).toLong, arr(2).toInt, arr(3).toString, arr(4).toLong)
      }

    ).assignAscendingTimestamps(_.timestamp * 1000L)

    // 得到窗口聚合结果
    val aggStream: DataStream[ItemViewCount] = dataStream
      .filter(_.behavior == "pv") // 过滤pv行为
      .keyBy("itemId") // 按照商品ID分组
      .timeWindow(Time.hours(1), Time.minutes(5)) // 设置滑动窗口进行统计
      .aggregate(new CountAgg(), new ItemViewWindowResult())

    val resultStream: DataStream[String] = aggStream
      .keyBy("windowEnd")    // 按照窗口分组，收集当前窗口内的商品count数据
      .process( new TopNHotItems(5) )     // 自定义处理流程

    dataStream.print("data")
    aggStream.print("agg")
    resultStream.print()
    

    env.execute("hit itens")
  }

}
//自定义预聚合函数AggregateFunction，聚合状态就是当前商品的count值
class CountAgg() extends AggregateFunction[UserBehavior,Long,Long](){
  //每来一条数据调用一次add,count就加一
  override def add(in: UserBehavior, accumulator: Long): Long = accumulator+1
  //初始化累加值
  override def createAccumulator(): Long = 0L
  //最后返回那个值，这里为accumulator
  override def getResult(accumulator: Long): Long = accumulator
  //分区处理的归并操作，这里将所有并处理的结果相加
  override def merge(a: Long, b: Long): Long = a + b
}

//自定义窗口函数windowsFunction
class ItemViewWindowResult() extends  WindowFunction[Long,ItemViewCount,Tuple,TimeWindow] {
  override def apply(key: Tuple, window: TimeWindow, input: Iterable[Long], out: Collector[ItemViewCount]): Unit = {
    //Long类型的Key为上一步的预聚合的返回值
    //Window为差给你扣类型，第一步中的没窗口类型，TimeWindow
    //input为接收的数据类型，此处为Long类型的迭代器
    //out为此方法返回的类型，此处为ItemViewCount样例类对象的集合
    val itemId = key.asInstanceOf[Tuple1[Long]].f0
    val windowEnd = window.getEnd
    val count = input.iterator.next()
    //调用ItemViewCount样例类对象的构造器，依次构造出ItemViewCount样例类并返回
    out.collect(ItemViewCount(itemId, windowEnd, count))
  }
 }

// 自定义KeyedProcessFunction
class TopNHotItems(topSize: Int) extends KeyedProcessFunction[Tuple, ItemViewCount, String]{
  // 先定义状态：ListState
  private var itemViewCountListState: ListState[ItemViewCount] = _


  override def open(parameters: Configuration): Unit = {
    itemViewCountListState = getRuntimeContext.getListState(new ListStateDescriptor[ItemViewCount]("itemViewCount-list", classOf[ItemViewCount]))
  }

  override def processElement(value: ItemViewCount, ctx: KeyedProcessFunction[Tuple, ItemViewCount, String]#Context, out: Collector[String]): Unit = {

    // 每来一条数据，直接加入ListState
    itemViewCountListState.add(value)
    // 注册一个windowEnd + 1之后触发的定时器
    ctx.timerService().registerEventTimeTimer(value.windowEnd + 1)

  }

  // 当定时器触发，可以认为所有窗口统计结果都已到齐，可以排序输出了
  override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[Tuple, ItemViewCount, String]#OnTimerContext, out: Collector[String]): Unit = {
    // 为了方便排序，另外定义一个ListBuffer，保存ListState里面的所有数据
    val allItemViewCounts: ListBuffer[ItemViewCount] = ListBuffer()
    val iter =itemViewCountListState.get().iterator()
    while(iter.hasNext){
      allItemViewCounts += iter.next()
    }

    // 清空状态
    itemViewCountListState.clear()

    // 按照count大小排序，取前n个
    val sortedItemViewCounts = allItemViewCounts.sortBy(_.count)(Ordering.Long.reverse).take(topSize)


    // 将排名信息格式化成String，便于打印输出可视化展示
    val result: StringBuilder = new StringBuilder
    result.append("窗口结束时间：").append( new Timestamp(timestamp - 1) ).append("\n")


    // 遍历结果列表中的每个ItemViewCount，输出到一行
    for( i <- sortedItemViewCounts.indices ){
      val currentItemViewCount = sortedItemViewCounts(i)
      result.append("NO").append(i + 1).append(": \t")
        .append("商品ID = ").append(currentItemViewCount.itemId).append("\t")
        .append("热门度 = ").append(currentItemViewCount.count).append("\n")
    }

    result.append("\n==================================\n\n")

    Thread.sleep(1000)
    out.collect(result.toString())



  }
}