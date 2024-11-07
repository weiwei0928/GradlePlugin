package com.ww.gradle.other

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @Author weiwei
 * @Date 2022/7/27 21:37
 */
class TestPlugin : Plugin<Project> {

    companion object {
        private const val TAG = "WritePluginMetaPlugin"
    }

    override fun apply(project: Project) {

        project.afterEvaluate {
            var android = project.extensions.getByName("android") as BaseExtension

//            project.plugins.withId("com.android.library") {
//                println("com.android.library")
//            }
//            project.plugins.withId("com.android.application") {
//                println("com.android.application")
//            }

            val hasAppPlugin = project.plugins.hasPlugin(AppPlugin::class.java)
            val hasLibPlugin = project.plugins.hasPlugin(LibraryPlugin::class.java)
            if (hasAppPlugin || hasLibPlugin) {
                writePluginMeta(project)
            } else {
                throw GradleException("非法使用")
            }

        }


    }

    private fun writePluginMeta(project: Project) {

    }


}