package com.ww.gradle

import com.android.SdkConstants
import com.android.build.api.transform.*
import com.android.build.api.transform.QualifiedContent.DefaultContentType
import com.android.build.api.transform.QualifiedContent.Scope
import com.android.build.gradle.BaseExtension
import javassist.ClassPool
import javassist.Modifier
import org.gradle.api.Project
import java.util.*
/**
 * @Author weiwei
 * @Date 2022/7/12 18:47
 */


class LoggerTransformer(private val project: Project) : Transform() {

    override fun getName(): String = "logger"

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> =
        mutableSetOf(DefaultContentType.CLASSES)

    override fun isIncremental(): Boolean = true

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> =
        EnumSet.of(Scope.PROJECT)

    override fun getReferencedScopes(): MutableSet<in QualifiedContent.Scope> =
        EnumSet.of(Scope.EXTERNAL_LIBRARIES, Scope.SUB_PROJECTS, Scope.TESTED_CODE)

    override fun transform(transformInvocation: TransformInvocation) {
        super.transform(transformInvocation)

        val log = project.logger
        val outputDir = transformInvocation.outputProvider.getContentLocation(
            name,
            outputTypes,
            scopes,
            Format.DIRECTORY
        )
        println("TAG:${outputDir.canonicalPath}")
        val classPool = createClassPool(transformInvocation)
        val isIncremental = transformInvocation.isIncremental
        val classNames =
            if (isIncremental) collectClassNamesForIncrementalBuild(transformInvocation)
            else collectClassNamesForFullBuild(transformInvocation)
        println("${name}classNames:$classNames")

        val ctClasses = classNames.map { className ->
            println("className: $className")
            classPool.get(className)
        }

        ctClasses.flatMap { it.declaredMethods.toList() }
            .filter { !Modifier.isAbstract(it.modifiers) && !Modifier.isNative(it.modifiers) }
            .forEach {
                println("methodName: ${it.name}")

                it.insertBefore("android.util.Log.e(\"LoggerTransformer\", \"before: ${it.name}\");")
                it.insertAfter("android.util.Log.e(\"LoggerTransformer\", \"after: ${it.name}\");")
            }

        ctClasses.forEach { it.writeFile(outputDir.canonicalPath) }
    }

    private fun createClassPool(transformInvocation: TransformInvocation): ClassPool {
//        val classPool = ClassPool(null)
        val classPool = ClassPool.getDefault()
        classPool.appendSystemPath()
        project.extensions.findByType(BaseExtension::class.java)?.bootClasspath?.forEach {
            classPool.appendClassPath(it.absolutePath)
        }

        transformInvocation.inputs.forEach { input ->
            input.directoryInputs.forEach { classPool.insertClassPath(it.file.absolutePath) }
            input.jarInputs.forEach {
                println(it.file.absolutePath)
                classPool.insertClassPath(it.file.absolutePath)
            }
        }
        transformInvocation.referencedInputs.forEach { input ->
            input.directoryInputs.forEach { classPool.insertClassPath(it.file.absolutePath) }
            input.jarInputs.forEach { classPool.insertClassPath(it.file.absolutePath) }
        }

        return classPool
    }

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

    private fun pathToClassName(path: String): String {
        return path.substring(0, path.length - SdkConstants.DOT_CLASS.length)
            .replace("/", ".")
            .replace("\\", ".")
    }
}