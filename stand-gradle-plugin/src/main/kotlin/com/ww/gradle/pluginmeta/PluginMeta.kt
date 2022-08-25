package com.ww.gradle.pluginmeta

/**
 * @Author weiwei
 * @Date 2022/7/27 21:38
 */
open class PluginMeta(
    var version: String = "0",
    var name: String = "",
    var mainClass: String = "",
    var innerPlugin: Boolean = false
)