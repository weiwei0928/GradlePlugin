package com.ww.gradle

import com.android.SdkConstants
import com.android.build.api.transform.*
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import javassist.ClassPool
import javassist.Modifier
import org.gradle.api.Project

/**
 * Javassist 使用练习
 *
 * @Author weiwei
 * @Date 2022/7/11 17:36
 */
class JavassistTransform(private val project: Project) : Transform() {


    companion object {
        private const val TAG = "JavassistTransform"
        private const val AndroidInstrumentationClassname = "android.app.Instrumentation"
        private const val ShadowInstrumentationClassname =
            "com.tencent.shadow.core.runtime.ShadowInstrumentation"
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
//            input.jarInputs.forEach {
//                println("$TAG jarInputs: ${it.file.absolutePath}")
//                classPool.insertClassPath(it.file.absolutePath)
//            }
        }
        val classNames = collectClassNamesForFullBuild(transformInvocation)
        println("${TAG}classNames:$classNames")
        val ctClasses = classNames.map { className ->
            println("${TAG}className: $className")
            classPool.get(className)
        }
        //该类所引用的所有类型
        ctClasses.map {
            println("$TAG :${it.refClasses}")
        }
        val filterClasses = ctClasses.filter {
            it.refClasses.contains("android.app.Activity") ||
                    it.refClasses.contains("androidx.appcompat.app.AppCompatActivity")
        }
        println("$TAG filterClasses:$filterClasses")
        ctClasses.flatMap { it.declaredMethods.toList() }
            .filter { !Modifier.isAbstract(it.modifiers) && !Modifier.isNative(it.modifiers) }
            .forEach {
                println("methodName: ${it.name}")

            }
        filterClasses.forEach {
            it.replaceClassName("androidx.appcompat.app.AppCompatActivity","com.eyepetizer.android.ui.common.ui.BaseActivity")
        }

        ctClasses.forEach { it.writeFile(outputDir.canonicalPath) }

    }

    private fun collectClassNamesForIncrementalBuild(invocation: TransformInvocation): List<String> =
        invocation.inputs
            .flatMap { it.directoryInputs }
            .flatMap {
                it.changedFiles
                    .filter { (_, status) -> status != Status.NOTCHANGED && status != Status.REMOVED }
                    .map { (file, _) -> file.relativeTo(it.file) }
            }
            .map { it.path }
            .filter { it.endsWith(SdkConstants.DOT_CLASS) }
            .map { pathToClassName(it) }

    private fun collectClassNamesForFullBuild(transformInvocation: TransformInvocation): List<String> =
        transformInvocation.inputs
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

    private fun pathToClassName(path: String): String {
        return path.substring(0, path.length - SdkConstants.DOT_CLASS.length)
            .replace("/", ".")
            .replace("\\", ".")
    }

}