package com.ww.gradle;

import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import org.gradle.api.Project

import java.util.Set;

/**
 * @Author weiwei
 * @Date 2022/7/11 18:00
 */
public class MyTransform extends Transform {

    Project project;

    public MyTransform(Project project){
        this.project = project;
    }
    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return null;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return null;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }
}
