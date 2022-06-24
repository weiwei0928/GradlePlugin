package com.ww.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 *
 * @author ww
 */
class StandGradlePlugin : Plugin<Project> {
    override fun apply(p0: Project) {
        println("使用独立项目创建的插件")
    }
}
