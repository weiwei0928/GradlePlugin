package com.ww.gradle.pluginmeta

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.json.simple.JSONObject
import java.io.FileOutputStream


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
        val pluginMeta = project.extensions.getByName("pluginMeta") as PluginMeta

        val hasAppPlugin = project.plugins.hasPlugin(AppPlugin::class.java)
        val hasLibPlugin = project.plugins.hasPlugin(LibraryPlugin::class.java)
        if (hasAppPlugin || hasLibPlugin) {
            android = project.extensions.getByName("android") as BaseExtension
        } else {
            throw GradleException("非法使用")
        }

        /**
         * val appExtension = project.extensions.findByType(AppExtension::class.java)
         * //transform 替换插件activity
         * appExtension?.registerTransform(ActivityTransform(project))
         * */

        project.afterEvaluate {
            android.variantFilter {
                project.logger.info(TAG + "afterEvaluate :" + it.name)
            }
            writePluginMeta(project, pluginMeta)
        }
    }

    private fun writePluginMeta(project: Project, pluginMeta: PluginMeta) {
        project.logger.info("$TAG writePluginMeta enter")

        val file = project.file("src/main/assets/plugin.meta")
        file.parentFile.mkdirs()
        val json = buildJson(pluginMeta)
        project.logger.info(json)
        try {
            val fileOutputStream = FileOutputStream(file)

            fileOutputStream.write(json?.toByteArray())
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildJson(pluginMeta: PluginMeta): String? {
        val jsonObject = JSONObject()
        jsonObject["version"] = pluginMeta.version
        jsonObject["name"] = pluginMeta.name
        jsonObject["mainClass"] = pluginMeta.mainClass
        jsonObject["innerPlugin"] = pluginMeta.innerPlugin
        print(jsonObject.toString())
        return jsonObject.toString()
    }


}