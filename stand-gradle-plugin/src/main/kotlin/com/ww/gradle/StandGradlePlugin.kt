package com.ww.gradle

import com.android.build.gradle.AppExtension
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

//      在AGP7.0中Transform已经被标记为废弃了，并且将在AGP8.0中移除。是时候了解一下，在Transform被废弃之后，该怎么适配了
        findByType?.registerTransform(ActivityTransform(project))

    }
}
