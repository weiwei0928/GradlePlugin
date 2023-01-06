package com.ww.gradle.publish

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Description:
 * Date: 2022/12/30
 * Author: weiwei
 */
class PublishPlugin :Plugin<Project>{

    override fun apply(project: Project) {
        val mavenPublish = mutableMapOf("plugin" to "maven-publish")
        project.apply(mavenPublish)
    }
}