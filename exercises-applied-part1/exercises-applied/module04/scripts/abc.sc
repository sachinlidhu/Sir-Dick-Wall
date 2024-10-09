/**
 * Given an array of integers, write a scala program to find the frequency of most frequent item from the list.
 */

val item = List(1,1,2,3,4,5,1,1) // 1 ->4

//item(0)
val ans = item.flatmap(x => x.split(",")).map(x => x,1).reduceByKey(_+_).collect()


//list(1 ->1, 1 ->1, 2 ->1...)
//reduceByKey(1->4, 2->1)

//print(ans)

/*
def logic(item :List[Int]): Int ={
  val i =0
  for(i <- item.length){
    if(item(i) = item(i+1))
  }
}*/

/**
GIven a list of integers with duplicate occurances of some elements.
Write a scala program to return a new list which contains only the unique elements from prev list.
Please don't use any inbuilt functions for removing duplicates
*/

val i =0

val result = List()

for(i <- item){
  val j =i
  for(j <- item){
    if(item(i) != item(j))
  }
  result = result + i
}
