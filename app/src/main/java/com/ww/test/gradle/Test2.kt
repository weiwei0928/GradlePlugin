package com.ww.test.gradle

/**
 * @Author weiwei
 * @Date 2022/7/15 18:42
 */
class Test2 {

    //声明后期初始化属性的特点：
    //
    //使用lateinit关键字
    //必须是可读且可写的变量，即用var声明的变量
    //不能声明于可空变量。
    //不能声明于基本数据类型变量。例：Int、Float、Double等，注意：String类型是可以的。
    //声明后，在使用该变量前必须赋值，不然会抛出UninitializedPropertyAccessException异常。
    var a : String? = null
    lateinit var builder: ProcessBuilder

    fun b() {
        println("ahshalhd")
    }
}