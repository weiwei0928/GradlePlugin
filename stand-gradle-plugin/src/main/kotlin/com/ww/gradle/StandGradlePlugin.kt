package com.ww.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 *
 * @author ww
 */
class StandGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        println("使用独立项目创建的插件")
        val findByType = project.extensions.findByType(AppExtension::class.java)
        println(findByType)

        findByType?.registerTransform(MyTransform(project))

    }
}
