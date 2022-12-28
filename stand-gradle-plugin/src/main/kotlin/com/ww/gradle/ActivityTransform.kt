package com.ww.gradle

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import javassist.ClassPool
import org.gradle.api.Project

/**
 * @Author weiwei
 * @Date 2022/7/25 20:19
 */
class ActivityTransform(private val project: Project) : Transform() {


    companion object {
        private const val TAG = "ActivityTransform"
    }

    // 设置我们自定义的Transform对应的Task名称
    override fun getName(): String {
        return this::class.simpleName!!
    }

    // 指定输入的类型，通过这里的设定，可以指定我们要处理的文件类型这样确保其他类型的文件不会传入
    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    // 指定Transform的作用范围
    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return false
    }

    override fun transform(transformInvocation: TransformInvocation) {
        super.transform(transformInvocation)
        transformInvocation.outputProvider.deleteAll()

        val outputDir = transformInvocation.outputProvider.getContentLocation(
            name,
            outputTypes,
            scopes,
            Format.DIRECTORY
        )

        val classPool = ClassPool.getDefault()
        val systemPath = classPool.appendSystemPath()
        println("$TAG systemPath: $systemPath")
        project.extensions.findByType(BaseExtension::class.java)?.bootClasspath?.forEach {
            println("$TAG bootClasspath: ${it.absolutePath}")
            classPool.appendClassPath(it.absolutePath)
        }
        transformInvocation.inputs.forEach { input ->
            input.directoryInputs.forEach {
                println("$TAG directoryInputs: ${it.file.absolutePath}")
                classPool.insertClassPath(it.file.absolutePath)
            }
            input.jarInputs.forEach {
                println("$TAG jarInputs: ${it.file.absolutePath}")
//                classPool.insertClassPath(it.file.absolutePath)
                var jarName = it.name
//                var md5Name = DigestUtils.md5Hex(it.file.absolutePath)
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length - 4)
                }
                var dest = transformInvocation.outputProvider.getContentLocation(
                        jarName , it.contentTypes, it.scopes, Format.JAR)
                FileUtils.copyFile(it.file, dest)
            }
        }
        val classNames = transformInvocation.collectClassNamesForFullBuild()
        println("${TAG}classNames:$classNames")
        val ctClasses = classNames.map { className ->
            println("${TAG}className: $className")
            classPool.get(className)
        }
        //该类所引用的所有类型
        ctClasses.map {
            println("$TAG :${it.refClasses}")
        }

        val activities = ctClasses.filter {
            it.refClasses.contains("android.app.Activity")
        }
        val appCompatActivities = ctClasses.filter {
            it.refClasses.contains("androidx.appcompat.app.AppCompatActivity")
        }
        appCompatActivities.forEach {
            it.replaceClassName(
                "androidx.appcompat.app.AppCompatActivity",
                "zeus.plugin.ZeusBaseActivity"
            )
        }
        activities.forEach {
            it.replaceClassName(
                "android.app.Activity",
                "zeus.plugin.ZeusBaseActivity"
            )
        }

        ctClasses.forEach { it.writeFile(outputDir.canonicalPath) }
//        ctClasses.forEach { it.writeFile(jarOutputDir.canonicalPath) }

    }


}