package com.ww.gradle.pluginmeta

import com.android.build.gradle.AppExtension
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
class WritePluginMetaPlugin : Plugin<Project> {

    companion object {
        private const val TAG = "WritePluginMetaPlugin"
    }

    private lateinit var android: BaseExtension

    override fun apply(project: Project) {
        project.extensions.create("pluginMeta", PluginMeta::class.java)
        val appExtension: AppExtension = project.extensions.getByType(AppExtension::class.java)
        val pluginVariants = appExtension.applicationVariants.forEach {
            it.mergeAssetsProvider.get().doLast {

            }
        }

        val pluginMeta = project.extensions.getByName("pluginMeta") as PluginMeta

        val hasAppPlugin = project.plugins.hasPlugin(AppPlugin::class.java)
        val hasLibPlugin = project.plugins.hasPlugin(LibraryPlugin::class.java)
        if (hasAppPlugin || hasLibPlugin) {
            android = project.extensions.getByName("android") as BaseExtension
            android.variantFilter{
                println(TAG+it)
                println(TAG+it.name)
            }

        } else {
            throw GradleException("非法使用")
        }
        project.afterEvaluate {
            android.variantFilter{
                println(TAG+it.name)
            }
            writePluginMeta(project, pluginMeta)
        }


    }

    private fun writePluginMeta(project: Project, pluginMeta: PluginMeta) {
//        val pluginMeta = project.extensions.findByType(PluginMeta::class.java)

    }


}