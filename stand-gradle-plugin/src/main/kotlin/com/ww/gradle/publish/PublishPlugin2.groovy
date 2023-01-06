package com.ww.gradle.publish


import org.gradle.api.Plugin
import org.gradle.api.Project
/**
 * Description:  <br>
 * Date: 2020-07-06 19:46 <br>
 * Author: zealzuo  <br>
 */
class PublishPlugin2 implements Plugin<Project> {
    @SuppressWarnings("UnstableApiUsage")
    @Override
    void apply(Project project) {
//        project.apply('plugin': 'maven-publish')
//        final ZPublication publication = project.extensions.create('zzyPublish', ZPublication, project)
//        project.afterEvaluate {
//            if (publication.checkZSonar) {
//                Utils.applyPlugin(project, 'com.zhizhangyi.platform.plugin.boilerplate.zsonar')
//            }
//            PublicationManager manager = new PublicationManager(project, publication)
//            manager.configure()
//            project.tasks.getByName(BasePlugin.CLEAN_TASK_NAME).doLast {
//                File pluginDir = PublishUtil.getPluginDir(project)
//                if (pluginDir.isDirectory()) {
//                    FileUtils.cleanDirectory(pluginDir)
//                }
//            }
//        }
//
//        project.metaClass.buildTime() {
//            return Utils.buildTime
//        }
    }
}
