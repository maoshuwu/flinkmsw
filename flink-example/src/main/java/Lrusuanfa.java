import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by $maoshuwu on 2021/1/7.
 */
public class Lrusuanfa<K,V> extends LinkedHashMap<K,V> {
    private int capacity; //缓存坑位
    //map负责查找，构建一个虚拟的双向链表，它里面安装一个Node节点，作为数据载体
    //1.一个Node节点，作为数据载体
   class  Node<K,V>{
       K key;
       V value;
        Node<K,V> prev;
        Node<K,V> next;
        public Node(){
            this.prev=this.next=null;
        }
        public Node(K key,V value){
            this.key=key;
            this.value=value;
            this.prev=this.next=null;
        }
    }
   //构建一个双向队列，里面放Node
    class DoubleLinkedLise<K,V>{
       Node<K,V> head;
       Node<K,V> tail;
       //构造方法
       public DoubleLinkedLise(){
           head = new Node<>();
           tail = new Node<>();
           head.next=tail;
           tail.next=head;
       }
       //添加到头
       public void  addHead(Node<K,V> node ){
           node.next=head.next;
           node.prev=head;
           head.next.prev=node;
           head.next=node;

       }
       //删除节点
       public void  removeNode(Node<K,V> node ){
           node.next.prev=node.prev;
           node.prev.next=node.next;
           node.prev=null;
           node.next=null;

       }
       //获取最后一个节点
       public Node  getLast(){
          return tail.prev;

       }
    }

    private int catchSize;
   Map<Integer,Node<Integer,Integer>> map;
   DoubleLinkedLise<Integer,Integer> doubleLinkedLise;
   public Lrusuanfa(int catchSize){
       this.catchSize=catchSize;//坑位
       map=new HashMap<>();//查找
       doubleLinkedLise=new DoubleLinkedLise<>();
   }

   public int get(int key){
       if (!map.containsKey(key)){
           return -1;
       }
       Node<Integer, Integer> node = map.get(key);
       doubleLinkedLise.removeNode(node);
       doubleLinkedLise.addHead(node);
       return node.value;
   }
  //saveOrupdate method
   public void put(int key,int value){
       if(map.containsKey(key)){//update
           Node<Integer, Integer> node = map.get(key);
           node.value=value;
           map.put(key,node);
           doubleLinkedLise.removeNode(node);
           doubleLinkedLise.addHead(node);
       }else{
           if (catchSize==map.size()){//坑位满了
               Node<Integer,Integer> lastNode = doubleLinkedLise.getLast();
               map.remove(lastNode.key);
               doubleLinkedLise.removeNode(lastNode);
           }
           //新增
           Node<Integer, Integer> newNode = new Node<>(key, value);
           map.put(key,newNode);
           doubleLinkedLise.addHead(newNode);
       }

   }


    public static void main(String[] args) {
        Lrusuanfa lruLinkHashmap = new Lrusuanfa(3);
        lruLinkHashmap.put(1,"a");
        lruLinkHashmap.put(2,"b");
        lruLinkHashmap.put(3,"c");
        System.out.println(lruLinkHashmap.map.keySet());
        lruLinkHashmap.put(4,"f");
        System.out.println(lruLinkHashmap.map.keySet());
        lruLinkHashmap.put(3,"f");
        System.out.println(lruLinkHashmap.map.keySet());
        lruLinkHashmap.put(3,"f");
        System.out.println(lruLinkHashmap.map.keySet());
        lruLinkHashmap.put(3,"f");
        System.out.println(lruLinkHashmap.map.keySet());
        lruLinkHashmap.put(5,"f");
        System.out.println(lruLinkHashmap.map.keySet());

    }
}
