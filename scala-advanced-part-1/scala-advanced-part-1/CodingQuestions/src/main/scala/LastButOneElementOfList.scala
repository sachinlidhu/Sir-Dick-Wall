object LastButOneElementOfList extends App{

  val myList = List(3,2,5,1,4,8,6) //--1,3251486,251486,--0,3251486,51486--,-1,3251486,1486
  //println(myList.takeRight(2))

  def recursive[A](ls :List[A]):A = ls match {
    case h::_::Nil => h
    case _ :: tail => recursive(tail)
    case _ => throw new NoSuchElementException
  }

  println(recursive(myList))

  //using builtin
  def builtin[A](ls:List[A]): A = {
    if(ls.isEmpty) throw new NoSuchElementException
    else ls.init.last
  }
  println(s"using biltin--" + builtin(myList))

  //finding nth element from the last
  def nthFromLast[A](n:Int,ls:List[A]): A = {
    if(n < 0) throw new IllegalArgumentException
    else if(ls.length < n) throw new NoSuchElementException
    else ls.takeRight(n).head
  }

 /* def nthFromLlastRecursive[A](n:Int,ls:List[A]): A = ls match {

    case _::tail if n ==0 => ls.head
    case _::tail if n > 0 => nthFromLlastRecursive(n-1,tail)
  }
  println(nthFromLlastRecursive(0,myList))*/
 def lastNthRecursive[A](n: Int, ls: List[A]): A = {
   def lastNthR(count: Int, resultList: List[A], curList: List[A]): A =
     curList match {
       case Nil if count > 0 => throw new NoSuchElementException
       case Nil => resultList.head
       case _ :: tail =>
         lastNthR(count - 1,
           if (count > 0) resultList else resultList.tail,
           tail)
     }

   if (n <= 0) throw new IllegalArgumentException
   else lastNthR(n, ls, ls)
 }
  println(lastNthRecursive(2,myList))
}
