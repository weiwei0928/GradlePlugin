package com.ww.gradle

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.gradle.api.Project

/**
 * @Author weiwei
 * @Date 2022/7/15 17:51
 */
class MyTransform(private val project: Project) : Transform() {

    companion object {
        private const val TAG = "MyTransform"
    }

    override fun getName(): String? {
        return "TestTransform"
    }

    override fun getInputTypes(): Set<QualifiedContent.ContentType>? {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope>? {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return false
    }

    override fun transform(
        context: Context?,
        inputs: MutableCollection<TransformInput>?,
        referencedInputs: MutableCollection<TransformInput>?,
        outputProvider: TransformOutputProvider?,
        isIncremental: Boolean
    ) {
        println("$TAG--------------------transform 开始  HHH -------------------")
//        super.transform(context, inputs, referencedInputs, outputProvider, isIncremental)

        inputs?.forEach {
            //文件夹里面包含的是我们手写的类以及R.class、BuildConfig.class以及R$XXX.class等
            it.directoryInputs.forEach { directoryInput ->

//                MyInjectByJavassist.injectToast(project,directoryInput.file.absolutePath)
                MyInjectByJavassist.injectToast(project,directoryInput.file.absolutePath)

                var dest = outputProvider?.getContentLocation(directoryInput.name,directoryInput.contentTypes,directoryInput.scopes,Format.DIRECTORY)
                println("directory output dest: $dest.absolutePath")
                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
        }

        println("$TAG--------------------transform 结束-------------------")

    }

}