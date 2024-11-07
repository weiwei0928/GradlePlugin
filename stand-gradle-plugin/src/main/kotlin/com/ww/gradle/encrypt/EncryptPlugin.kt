package com.ww.gradle.encrypt

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.tasks.MergeSourceSetFolders
import com.ww.gradle.utils.Utils
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @Author weiwei
 * @Date 2024/1/29 17:38
 */
class EncryptPlugin : Plugin<Project> {

    companion object {
        private const val TAG = "EncryptPlugin"
    }

    private lateinit var android: AppExtension
    override fun apply(project: Project) {
//        project.extensions.create("assets_encrypt_model", AssetsEncryptModel::class.java)
//        val model = project.extensions.getByName("assets_encrypt_model") as AssetsEncryptModel
        android = project.extensions.getByName("android") as AppExtension
//        android.applicationVariants.all {
//            it.
//            val mergeAssets = getMergeAssets(it)
//            mergeAssets.doLast {
//                mergeAssets.outputDir
//            }
//        }
        android.applicationVariants.all {
            it.mergeAssetsProvider.get().outputDir
        }

//        project.tasks

    }


    fun getMergeAssets(variant: BaseVariant): MergeSourceSetFolders {
        if (Utils.compareVersion(Utils.getAGPVersion(), "3.3.0") >= 0) {
            return variant.mergeAssetsProvider.get()
        } else {
            return variant.getMergeAssets()
        }
    }
}