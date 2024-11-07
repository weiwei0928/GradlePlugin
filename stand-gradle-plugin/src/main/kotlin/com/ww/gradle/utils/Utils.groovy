package com.ww.gradle.utils

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import groovy.xml.QName
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyResolveDetails
import org.gradle.api.artifacts.ModuleVersionSelector
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.internal.impldep.org.testng.log4testng.Logger
import org.gradle.util.VersionNumber

import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.function.Predicate

/**
 * Author: zeal zuo
 * Date: 18/4/26.
 * Usage:*/
class Utils {
    public static final String TMP_SUFFIX = ".tmp";
    public static final int BUFFER_SIZE = 4096;
    public static final String SONAR_TOKEN = "c2ff07aa549131aa9d83daffa7113dcc2dc37945"
    public static final String SONAR_SERVER_HOST = "sonar.zhizhangyi.com";
    public static final String SONAR_SERVER_URL = "http://sonar.zhizhangyi.com"
    public static final String SONAR_QUALITY_FILE = "Android%20Sonar%20way"
    public static final String SONAR_QUALITY_GATE = "Android-Component"
    public static final String PLUGIN_DEFAULT_DIR = "boilerplate"

    static boolean isEmpty(String txt) {
        return txt == null || txt.isEmpty();
    }

    static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Null-safe equivalent of {@code a.equals ( b )}.*/
    static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a == b;
    }

    static boolean isAndroidLibrary(Project project) {
        return project.plugins.findPlugin("com.android.library") != null\
                                                                       || project.plugins.findPlugin("android-library") != null\
                                                                       || project.plugins.hasPlugin(LibraryPlugin)
    }

    static boolean isJavaLibrary(Project project) {
        return project.plugins.findPlugin("java") != null\
                                                                       || project.plugins.findPlugin("java-library") != null
    }

    static boolean isAndroidApplication(Project project) {
        return project.plugins.findPlugin("com.android.application") != null\
                                                                       || project.plugins.findPlugin("android") != null\
                                                                       || project.plugins.hasPlugin(AppPlugin)
    }

    static String getSonarProjectKey(Project project) {
        final String repoUri = 'git config remote.origin.url'.execute([], project.rootDir).text.trim()
        String path
        if (isEmpty(repoUri)) {
            System.err.println("fail to get git origin url: " + repoUri)
            path = "android_"
        } else {
            path = URI.create(repoUri).getPath()
            if (path.startsWith("/")) {
                path = path.substring(1)
            }
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1)
            }
            path = path.replace("/", "_") + "_"
        }
        return path + project.getName()
    }


    static File[] recursiveListFile(File f, FilenameFilter filter) {
        List<File> allFiles = new ArrayList<>()
        List<File> resultFiles = new ArrayList<>()
        allFiles.add(f)
        while (!allFiles.isEmpty()) {
            File file = allFiles.remove(0)
            File[] targets = file.listFiles(filter)
            if (targets != null) {
                resultFiles.addAll(targets)
            }
            targets = file.listFiles(new FileFilter() {
                @Override
                boolean accept(File pathname) {
                    return pathname.isDirectory()
                }
            })
            if (targets != null) {
                allFiles.addAll(targets)
            }
        }
        return resultFiles.toArray()
    }

    static boolean isUnixSystem() {
        return File.separator == '/';
    }

    static String getBuildTime() {
        def df = new SimpleDateFormat("yyyyMMddHHmmss") //you can change it
        df.setTimeZone(TimeZone.getTimeZone("GMT+8"))
        return df.format(new Date())
    }

    static String getBuildTime(long milliSecond) {
        def df = new SimpleDateFormat("yyyyMMddHHmmss") //you can change it
        df.setTimeZone(TimeZone.getTimeZone("GMT+8"))
        return df.format(new Date(milliSecond))
    }

    static String getShortBuildTime() {
        def df = new SimpleDateFormat("yyyyMMddHHmm") //you can change it
        df.setTimeZone(TimeZone.getTimeZone("GMT+8"))
        return df.format(new Date())
    }

    static Integer emmVersion() {
        return emmVersion(null)
    }


    static Integer emmVersion(Project project) {
        mbsFlavorVersion(project, 20)
    }

    /**
     * 2020 + 36 = 2056
     * 公式 (((year-2022) + 2060) * 10^6 + day * (24 * 60) + hh * 60 + mm)
     * 形式为[passed_month + 2057][passed_minutes]
     * 确保版本号大于2060 * 10^6(兼容旧版本), 并且小于最大整型(2^31,2147483647)*/
    static Integer mbsFlavorVersion(Project project, int base) {
        Calendar calendar = Calendar.getInstance()
        /**
         * 设计这个逻辑是因为MBS想确保Debug和Release使用的时间戳是一致的*/
        if (project && project.hasProperty("mbsVersionSecond")) {
            long second = Long.parseLong(project."mbsVersionSecond")
            Logger.debug(project, "mbsVersionSecond ${second}")
            calendar.setTimeInMillis(second * 1000)
        } else {
            calendar.setTimeInMillis(System.currentTimeMillis())
        }

        int year = calendar.get(Calendar.YEAR)
        if (year < 2020 || year >= 2040) {
            throw new GradleException("time machine: " + year)
        }
        int day = calendar.get(Calendar.DAY_OF_YEAR)
        int hh = calendar.get(Calendar.HOUR_OF_DAY)
        int mm = calendar.get(Calendar.MINUTE)
        return Integer.parseInt(String.format("%04d%06d", (year - 2022 + 2060 + base), day * (24 * 60) + hh * 60 + mm))
    }

    static Integer mbsVersion(Project project) {
        if ("emm" == System.getenv("mbsAppType")) {
            return mbsFlavorVersion(project, 20);
        } else {
            return mbsFlavorVersion(project, 0);
        }
    }

    static Integer oldMbsVersion(Project project) {
        String time
        if (project && project.hasProperty("mbsVersionSecond")) {
            long second = Long.parseLong(project."mbsVersionSecond")
            Logger.debug(project, "mbsVersionSecond ${second}")
            time = getBuildTime(second * 1000)
        } else {
            time = buildTime
        }

        int year = Integer.parseInt(time.substring(0, 4))
        if (year < 2020 || year > 2035) {
            throw new GradleException("time machine: " + time)
        }
        int month = Integer.parseInt(time.substring(4, 6))
        int day = Integer.parseInt(time.substring(6, 8))
        int hh = Integer.parseInt(time.substring(8, 10))
        int mm = Integer.parseInt(time.substring(10, 12))
        return Integer.parseInt(String.format("%04d%02d%02d%02d", (year - 2020) * 12 + month + 2020, day, hh, mm))
    }

    /**
     * 公式 (((year-2020) * 12 + month + 2020) * 10^6 + day * 10^4 + hh * 10 ^ 2 + mm)
     * 形式为[passed_month + 2020][day][hh][mm]
     * 确保版本号大于2021 * 10^6(兼容旧版本), 并且小于最大整型(2^31,2147483647)*/
    static Integer mbsVersion() {
        mbsVersion(null)
    }


    static boolean applyPlugin(Project project, Object pluginIdOrClazz) {
        if (!hasPlugin(project, pluginIdOrClazz)) {
            project.apply('plugin': pluginIdOrClazz)
            return true
        }
        return false
    }

    static File getPluginDefaultDir(Project project) {
        return new File(project.getRootProject(), "boilerplate")
    }

    static boolean hasPlugin(Project project, Object pluginId) {
        return project.plugins.hasPlugin(pluginId)
    }

    static void resolveDependenciesConflict(Project project, String key, String version) {
        project.configurations.all { Configuration configuration ->
            resolutionStrategy {
                eachDependency { DependencyResolveDetails details ->
                    //find all dependencies
                    final ModuleVersionSelector selector = details.requested
                    final String selectorKey = selector.group + ":" + selector.name
                    if (selectorKey == key) {
                        details.useVersion(version)
                    }
                }
            }
        }
    }

    static void dump(Project project) {
        Map<String, List<String>> configurationMap = [:]
        dumpDependencies(project, configurationMap)

        project.gradle.addBuildListener(new BuildListener() {
            @Override
            void buildStarted(Gradle gradle) {
                configurationMap.clear()
            }

            @Override
            void settingsEvaluated(Settings settings) {

            }

            @Override
            void projectsLoaded(Gradle gradle) {

            }

            @Override
            void projectsEvaluated(Gradle gradle) {

            }

            @Override
            void buildFinished(BuildResult result) {
                if (!configurationMap.isEmpty()) {
                    project.getLogger().warn("-----dump ${project.name}-----")
                    for (Map.Entry<String, List<String>> entry : configurationMap.entrySet()) {
                        project.getLogger().warn("--${entry.getKey()}")
                        entry.getValue().each { String artifactValue -> project.getLogger().warn("  ${artifactValue}")
                        }
                    }
                    project.getLogger().warn("-----dump ${project.name}-----")
                }
            }
        })
    }

    static void dumpDependencies(Project project, Map<String, ArrayList<String>> configurationMap) {
        project.configurations.all { Configuration configuration ->
            def resolverMap = [:]
            resolutionStrategy {
                eachDependency { DependencyResolveDetails details ->
                    final ModuleVersionSelector selector = details.requested
                    final String selectorKey = selector.group + ":" + selector.name
                    final String oldVersion = resolverMap.get(selectorKey)
                    final String version = selector.version
                    if (oldVersion == null) {
                        resolverMap.put(selectorKey, version)
                        List<String> dependencyList = configurationMap.get(configuration.name)
                        if (dependencyList == null) {
                            dependencyList = new ArrayList<>()
                            configurationMap.put(configuration.name, dependencyList)
                        }
                        dependencyList.add("${selectorKey}:${version}")
                    } else {
                        if (VersionNumber.parse(version) > VersionNumber.parse(oldVersion)) {
                            resolverMap.put(selectorKey, version)
                            List<String> dependencies = configurationMap.get(configuration.name)
                            dependencies.removeIf(new Predicate<CharSequence>() {
                                @Override
                                boolean test(CharSequence s) {
                                    return s.toString().startsWith("${selectorKey}:${version}")
                                }
                            })
                            dependencies.add("${selectorKey}:${version}(*)")
                        }
                    }
                }
            }
        }
    }

    static def readPropertyFromLocalProperties(Project project, String key, String defaultValue) {
        Properties properties = new Properties()
        try {
            project.rootProject.file('local.properties').withReader { Reader reader -> properties.load(reader)
            }
            if (!properties.keySet().contains(key)) {
                return defaultValue
            }
            return properties.getProperty(key)
        } catch (Exception e) {
            println("load local properties failed msg:${e.message}")
            return defaultValue
        }
    }

    static def now() {
        def df = new SimpleDateFormat("MM-dd HH:mm:ss.SSS") //you can change it
        df.setTimeZone(TimeZone.getTimeZone("GMT+8"))
        return df.format(new Date())
    }

    static String getGitUrl(Project project) {
        String branch = 'git config --get remote.origin.url'.execute(null, project.rootDir).text.trim()
        if (isEmpty(branch)) {
            return ""
        }
        return new URI(branch).getPath()
    }

    static String getHeadRef(Project project) {
        String branch = 'git for-each-ref --format "%(refname:short)" --points-at HEAD'.execute(null, project.rootDir).text.trim()
        final List<String> result = branch.readLines()
        if (result.isEmpty()) {
            return null;
        }
        result.eachWithIndex { String entry, int i -> result.set(i, entry.replace('"', ''))
        }
        final String remoteRef = result.find { it -> return it.startsWith("origin") && it != "origin/HEAD"
        }
        if (!isEmpty(remoteRef)) {
            return remoteRef;
        }
        return result.get(0)
    }

    static String getAGPVersion() {
        // AGP 3.6+
        try {
            Class aClass = Class.forName("com.android.Version")
            Field version = aClass.getDeclaredField("ANDROID_GRADLE_PLUGIN_VERSION")
            return version.get(aClass)
        } catch (Throwable ignore) {
            Class aClass = Class.forName("com.android.builder.model.Version")
            Field version = aClass.getDeclaredField("ANDROID_GRADLE_PLUGIN_VERSION")
            return version.get(aClass)
        }
    }

    static int compareVersion(String v1, String v2) {
        if (v1.equals(v2)) {
            return 0
        }

        String[] version1 = v1.split("-")
        String[] version2 = v2.split("-")
        String[] version1Array = version1[0].split("[._]")
        String[] version2Array = version2[0].split("[._]")

        String preRelease1 = new String()
        String preRelease2 = new String()
        if (version1.length > 1) {
            preRelease1 = version1[1]
        }
        if (version2.length > 1) {
            preRelease2 = version2[1]
        }

        int index = 0
        int minLen = Math.min(version1Array.length, version2Array.length)
        long diff = 0

        while (index < minLen && (diff = Long.parseLong(version1Array[index]) - Long.parseLong(version2Array[index])) == 0) {
            index++
        }
        if (diff == 0) {
            for (int i = index; i < version1Array.length; i++) {
                if (Long.parseLong(version1Array[i]) > 0) {
                    return 1
                }
            }

            for (int i = index; i < version2Array.length; i++) {
                if (Long.parseLong(version2Array[i]) > 0) {
                    return -1
                }
            }
            //compare pre-release
            if (!preRelease1.isEmpty() && preRelease2.isEmpty()) {
                return -1
            } else if (preRelease1.isEmpty() && !preRelease2.isEmpty()) {
                return 1
            } else if (!preRelease1.isEmpty() && !preRelease2.isEmpty()) {
                int preReleaseDiff = preRelease1.compareTo(preRelease2);
                if (preReleaseDiff > 0) {
                    return 1
                } else if (preReleaseDiff < 0) {
                    return -1
                }
            }
            return 0
        } else {
            return diff > 0 ? 1 : -1
        }
    }

    static String upperCase(String str) {
        char[] ch = str.toCharArray()
        if (ch[0] >= 'a' && ch[0] <= 'z') {
            ch[0] -= 32
        }
        return String.valueOf(ch)
    }

    static String getNodeNameAttr(Node node) {
        Map<Object, Object> attributes = node.attributes();
        String nodeValue = ""
        for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
            if (entry instanceof QName) {
                if (entry.key.localPart == "name") {
                    nodeValue = entry.value.toString()
                }
            } else {
                if ("name" == entry.key) {
                    nodeValue = entry.value.toString();
                }
            }
        }
        return nodeValue
    }
}