package com.ww.gradle

import com.android.SdkConstants
import com.android.build.api.transform.TransformInvocation
import javassist.ClassPool
import javassist.CtClass

/**
 * @Author weiwei
 * @Date 2022/7/19 14:34
 */

fun addOnclickCode(ctClass: CtClass, body: String, fileName: String) {
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

fun TransformInvocation.collectClassNamesForFullBuild(): List<String> =
    inputs
        .flatMap { it.directoryInputs }
        .flatMap {
            it.file.walkTopDown()
                .filter { file -> file.isFile }
                .map { file -> file.relativeTo(it.file) }
                .toList()
        }
        .map { it.path }
        .filter { it.endsWith(SdkConstants.DOT_CLASS) }
        .map { pathToClassName(it) }


fun pathToClassName(path: String): String {
    return path.substring(0, path.length - SdkConstants.DOT_CLASS.length)
        .replace("/", ".")
        .replace("\\", ".")
}
