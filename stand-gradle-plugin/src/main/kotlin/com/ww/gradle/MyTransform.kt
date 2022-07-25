package com.ww.gradle

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.gradle.internal.pipeline.TransformManager
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


}