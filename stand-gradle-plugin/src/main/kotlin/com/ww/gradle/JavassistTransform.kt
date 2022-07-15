package com.ww.gradle

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.internal.impldep.com.amazonaws.services.s3.transfer.TransferManager

/**
 * @Author weiwei
 * @Date 2022/7/11 17:36
 */
class JavassistTransform : Transform(){

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

    override fun transform(
        context: Context?,
        inputs: MutableCollection<TransformInput>?,
        referencedInputs: MutableCollection<TransformInput>?,
        outputProvider: TransformOutputProvider?,
        isIncremental: Boolean
    ) {
        super.transform(context, inputs, referencedInputs, outputProvider, isIncremental)

        inputs?.forEach {
            it.jarInputs.forEach {

            }
            it.directoryInputs.forEach { directoryInput ->
                var dest = outputProvider?.getContentLocation(directoryInput.name,directoryInput.contentTypes,directoryInput.scopes,Format.DIRECTORY)
//                File
            }
        }
    }

}