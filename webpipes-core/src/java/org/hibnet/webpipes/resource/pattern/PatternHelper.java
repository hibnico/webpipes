package org.hibnet.webpipes.resource.pattern;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.hibnet.webpipes.WebpipeUtils;
import org.hibnet.webpipes.resource.FileResource;
import org.hibnet.webpipes.resource.Resource;
import org.hibnet.webpipes.resource.UrlResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatternHelper {

    private static final Logger logger = LoggerFactory.getLogger(PatternHelper.class);

    private static final String JAR_URL_SEPARATOR = "!/";

    private static Method equinoxResolveMethod;

    static {
        try {
            // Detect Equinox OSGi (e.g. on WebSphere 6.1)
            Class<?> fileLocatorClass = Class.forName("org.eclipse.core.runtime.FileLocator");
            equinoxResolveMethod = fileLocatorClass.getMethod("resolve", URL.class);
            logger.debug("Found Equinox FileLocator for OSGi bundle URL resolution");
        } catch (Throwable ex) {
            equinoxResolveMethod = null;
        }
    }

    public static String determineRootPath(PathMatcher pathMatcher, String pattern) {
        int rootDirEnd = pattern.length();
        while (rootDirEnd > 0 && pathMatcher.isPattern(pattern.substring(0, rootDirEnd))) {
            rootDirEnd = pattern.lastIndexOf('/', rootDirEnd - 2) + 1;
        }
        return pattern.substring(0, rootDirEnd);
    }

    public static Set<Resource> getFilesystemResources(PathMatcher pathMatcher, File rootDir, String subPattern)
            throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Looking for matching resources in directory tree [" + rootDir.getPath() + "]");
        }
        Set<File> matchingFiles = retrieveMatchingFiles(pathMatcher, rootDir, subPattern);
        Set<Resource> result = new LinkedHashSet<Resource>(matchingFiles.size());
        for (File file : matchingFiles) {
            result.add(new FileResource(null, file));
        }
        return result;
    }

    private static Set<File> retrieveMatchingFiles(PathMatcher pathMatcher, File rootDir, String pattern) throws IOException {
        if (!rootDir.exists()) {
            // Silently skip non-existing directories.
            if (logger.isDebugEnabled()) {
                logger.debug("Skipping [" + rootDir.getAbsolutePath() + "] because it does not exist");
            }
            return Collections.emptySet();
        }
        if (!rootDir.isDirectory()) {
            // Complain louder if it exists but is no directory.
            if (logger.isWarnEnabled()) {
                logger.warn("Skipping [" + rootDir.getAbsolutePath() + "] because it does not denote a directory");
            }
            return Collections.emptySet();
        }
        if (!rootDir.canRead()) {
            if (logger.isWarnEnabled()) {
                logger.warn("Cannot search for matching files underneath directory [" + rootDir.getAbsolutePath()
                        + "] because the application is not allowed to read the directory");
            }
            return Collections.emptySet();
        }
        String fullPattern = WebpipeUtils.replace(rootDir.getAbsolutePath(), File.separator, "/");
        if (!pattern.startsWith("/")) {
            fullPattern += "/";
        }
        fullPattern = fullPattern + WebpipeUtils.replace(pattern, File.separator, "/");
        Set<File> result = new LinkedHashSet<File>(8);
        doRetrieveMatchingFiles(pathMatcher, fullPattern, rootDir, result);
        return result;
    }

    private static void doRetrieveMatchingFiles(PathMatcher pathMatcher, String fullPattern, File dir, Set<File> result) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Searching directory [" + dir.getAbsolutePath() + "] for files matching pattern [" + fullPattern + "]");
        }
        File[] dirContents = dir.listFiles();
        if (dirContents == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("Could not retrieve contents of directory [" + dir.getAbsolutePath() + "]");
            }
            return;
        }
        for (File content : dirContents) {
            String currPath = WebpipeUtils.replace(content.getAbsolutePath(), File.separator, "/");
            if (content.isDirectory() && pathMatcher.matchStart(fullPattern, currPath + "/")) {
                if (!content.canRead()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Skipping subdirectory [" + dir.getAbsolutePath()
                                + "] because the application is not allowed to read the directory");
                    }
                } else {
                    doRetrieveMatchingFiles(pathMatcher, fullPattern, content, result);
                }
            }
            if (pathMatcher.match(fullPattern, currPath)) {
                result.add(content);
            }
        }
    }

    public static List<Resource> getClasspathResources(PathMatcher pathMatcher, ClassLoader cl, String pattern) throws IOException {
        List<Resource> resources = new ArrayList<>();

        String rootPath = determineRootPath(pathMatcher, pattern);
        String subPattern = pattern.substring(rootPath.length());

        List<URL> rootUrls = findAllClassPathUrls(cl, rootPath);
        for (URL rootUrl : rootUrls) {
            rootUrl = handleBundleUrl(rootUrl);
            if (rootUrl.getProtocol().equals("jar") || rootUrl.getProtocol().equals("zip")) {
                resources.addAll(getJarResources(pathMatcher, rootUrl, subPattern));
            } else if (rootUrl.getProtocol().equals("file")) {
                File rootDir = WebpipeUtils.urlToFile(rootUrl);
                resources.addAll(getFilesystemResources(pathMatcher, rootDir, subPattern));
            } else {
                throw new RuntimeException("Unsupported resolved classpath protocol " + rootUrl.toExternalForm());
            }
        }

        return resources;
    }

    private static List<URL> findAllClassPathUrls(ClassLoader cl, String location) throws IOException {
        String path = location;
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        Enumeration<URL> resourceUrls = (cl != null ? cl.getResources(path) : ClassLoader.getSystemResources(path));
        return Collections.list(resourceUrls);
    }

    private static URL handleBundleUrl(URL url) {
        if (equinoxResolveMethod != null) {
            if (url.getProtocol().startsWith("bundle")) {
                try {
                    return (URL) equinoxResolveMethod.invoke(null, url);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return url;
    }

    private static Set<Resource> getJarResources(PathMatcher pathMatcher, URL rootUrl, String subPattern) throws IOException {
        URLConnection con = rootUrl.openConnection();
        JarFile jarFile;
        String jarFileUrl;
        String rootEntryPath;
        boolean newJarFile = false;

        if (con instanceof JarURLConnection) {
            // Should usually be the case for traditional JAR files.
            JarURLConnection jarCon = (JarURLConnection) con;
            con.setUseCaches(con.getClass().getSimpleName().startsWith("JNLP"));
            jarFile = jarCon.getJarFile();
            jarFileUrl = jarCon.getJarFileURL().toExternalForm();
            JarEntry jarEntry = jarCon.getJarEntry();
            rootEntryPath = (jarEntry != null ? jarEntry.getName() : "");
        } else {
            // No JarURLConnection -> need to resort to URL file parsing.
            // We'll assume URLs of the format "jar:path!/entry", with the protocol
            // being arbitrary as long as following the entry format.
            // We'll also handle paths with and without leading "file:" prefix.
            String urlFile = rootUrl.getFile();
            int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
            if (separatorIndex != -1) {
                jarFileUrl = urlFile.substring(0, separatorIndex);
                rootEntryPath = urlFile.substring(separatorIndex + JAR_URL_SEPARATOR.length());
                jarFile = new JarFile(WebpipeUtils.urlToFile(new URL(jarFileUrl)));
            } else {
                jarFile = new JarFile(urlFile);
                jarFileUrl = urlFile;
                rootEntryPath = "";
            }
            newJarFile = true;
        }

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Looking for matching resources in jar file [" + jarFileUrl + "]");
            }
            if (!"".equals(rootEntryPath) && !rootEntryPath.endsWith("/")) {
                // Root entry path must end with slash to allow for proper matching.
                // The Sun JRE does not return a slash here, but BEA JRockit does.
                rootEntryPath = rootEntryPath + "/";
            }
            Set<Resource> result = new LinkedHashSet<Resource>(8);
            for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
                JarEntry entry = entries.nextElement();
                String entryPath = entry.getName();
                if (entryPath.startsWith(rootEntryPath)) {
                    String relativePath = entryPath.substring(rootEntryPath.length());
                    if (pathMatcher.match(subPattern, relativePath)) {
                        result.add(new UrlResource(null, new URL(rootUrl, relativePath)));
                    }
                }
            }
            return result;
        } finally {
            // Close jar file, but only if freshly obtained -
            // not from JarURLConnection, which might cache the file reference.
            if (newJarFile) {
                jarFile.close();
            }
        }
    }

}
