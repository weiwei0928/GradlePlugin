package com.ww.gradle

import javassist.ClassPool
import javassist.CtClass

/**
 * @Author weiwei
 * @Date 2022/7/19 14:34
 */

fun addCode(ctClass: CtClass, body: String, fileName: String) {
    ctClass.defrost()
    var method =
        ctClass.getDeclaredMethod(
            "onClick",
            arrayOf(ClassPool.getDefault().get("android.view.View"))
        )
    method.insertAfter(body)
    ctClass.writeFile(fileName)
    ctClass.detach()
    println("write file: " + fileName + "\\" + ctClass.name)
    println("modify method: " + method.name + " succeed")
}