package library

fun Double.format(decimals:Int =2):String = String.format("%.${decimals}f",this)