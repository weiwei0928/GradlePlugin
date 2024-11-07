package com.ww.gradle.other

import com.android.SdkConstants
import com.android.build.gradle.BaseExtension
import com.android.utils.FileUtils
import javassist.ClassPool
import org.gradle.api.Project
import java.io.File

/**
 * @Author weiwei
 * @Date 2022/7/15 17:57
 */
object MyInjectByJavassist {

    private const val TAG = "MyInjectByJavassist"

    fun injectToast(project: Project, path: String) {

        val baseExtension = project.extensions.getByName("android") as BaseExtension


        val classPool = ClassPool.getDefault()
        // 加入当前路径
        classPool.appendClassPath(path)
        // project.android.bootClasspath 加入android.jar，不然找不到android相关的所有类
//        classPool.appendClassPath(project.android.bootClasspath[0].toString())
        val sdkDirectory = baseExtension.sdkDirectory
        val compileSdkVersion =
            baseExtension.compileSdkVersion ?: throw IllegalStateException("compileSdkVersion获取失败")
        val androidJarPath = "platforms/${compileSdkVersion}/android.jar"
        val androidJar = File(sdkDirectory, androidJarPath)
        classPool.appendClassPath(androidJar.absolutePath)
        // 引入android.os.Bundle包，因为onCreate方法参数有Bundle
        classPool.importPackage("android.os.Bundle")
        val allFiles = FileUtils.getAllFiles(File(path))
        allFiles.filter{
            it?.name?.endsWith(SdkConstants.DOT_CLASS) ?: false
        }.forEach {
            println("$TAG----->${it.name}")
        }
//        val file = File(path)
//        if (file.isDirectory){
//            //递归遍历
//        }
        val fileTreeWalk = File(path).walk()
    }

}