package com.ww.gradle

import com.android.SdkConstants
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.QualifiedContent.DefaultContentType
import com.android.build.api.transform.QualifiedContent.Scope
import com.android.build.api.transform.Status
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.BaseExtension
import javassist.ClassPool
import javassist.Modifier
import org.gradle.api.Project
import java.util.EnumSet
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
        println("哈哈哈哈哈")
        val log = project.logger
        val outputDir = transformInvocation.outputProvider.getContentLocation(
            name,
            outputTypes,
            scopes,
            Format.DIRECTORY
        )

        val classPool = createClassPool(transformInvocation)
        val isIncremental = transformInvocation.isIncremental
        val classNames =
            if (isIncremental) collectClassNamesForIncrementalBuild(transformInvocation)
            else collectClassNamesForFullBuild(transformInvocation)
        val ctClasses = classNames.map { className ->
            log.debug("className: $className")
            classPool.get(className)
        }

        ctClasses.flatMap { it.declaredMethods.toList() }
            .filter { !Modifier.isAbstract(it.modifiers) && !Modifier.isNative(it.modifiers) }
            .forEach {
                log.debug("methodName: ${it.name}")

                it.insertBefore("android.util.Log.e(\"LoggerTransformer\", \"before: ${it.name}\");")
                it.insertAfter("android.util.Log.e(\"LoggerTransformer\", \"after: ${it.name}\");")
            }

        ctClasses.forEach { it.writeFile(outputDir.canonicalPath) }
    }

    private fun createClassPool(invocation: TransformInvocation): ClassPool {
        val classPool = ClassPool(null)
        classPool.appendSystemPath()
        project.extensions.findByType(BaseExtension::class.java)?.bootClasspath?.forEach {
            classPool.appendClassPath(it.absolutePath)
        }

        invocation.inputs.forEach { input ->
            input.directoryInputs.forEach { classPool.appendClassPath(it.file.absolutePath) }
            input.jarInputs.forEach { classPool.appendClassPath(it.file.absolutePath) }
        }
        invocation.referencedInputs.forEach { input ->
            input.directoryInputs.forEach { classPool.appendClassPath(it.file.absolutePath) }
            input.jarInputs.forEach { classPool.appendClassPath(it.file.absolutePath) }
        }

        return classPool
    }

    private fun collectClassNamesForFullBuild(invocation: TransformInvocation): List<String> =
        invocation.inputs
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