package org.xacml4j.v30.spi.repository;


import com.google.common.base.Strings;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.CompositeDecisionRule;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * A file system based policy source implementation
 * 
 * @author Giedrius Trumpickas
 */
public class FilesystemPolicySource extends AbstractPolicySource
{
    private final static Logger log = LoggerFactory.getLogger(FilesystemPolicySource.class);
    
    private final static String DEFAULT_POLICY_FILE_MATCH_PATTERN = "*.xml";
    
    private Path rootPath;
    private String filePatternMatch;

    private Map<Path, CompositeDecisionRule> rules;
    
    private WatchService watcher;
    private Closer closer;
    private ExecutorService notifyExecutor;
    private ExecutorService fsEventsExecutor;

    /**
     * Constructs file system policy source
     * with a given builder
     *
     * @param b a builder reference
     */
    private FilesystemPolicySource(Builder b){
        super(b);
        Verify.verifyNotNull(b.path);
        Verify.verifyNotNull(b.patternMatch);
        this.rootPath = b.path;
        this.filePatternMatch = b.patternMatch;
        this.rules = new ConcurrentHashMap<Path, CompositeDecisionRule>();
        this.notifyExecutor = Executors.newSingleThreadExecutor();
        this.fsEventsExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public Iterable<CompositeDecisionRule> getPolicies() 
            throws IOException {
        if(watcher == null){
            initialize();
        }
        return rules.values();
    }
    
    public static Builder builder(String id){
        return new Builder(id);
    }
    
    /**
     * Initializes {@link java.nio.file.WatchService} on a given
     * root folder and all sub-folders
     * *
     * @throws IOException if an error occurs while
     * initializing {@link java.nio.file.WatchService}
     */
    private void initialize() throws IOException
    {
        synchronized (this){
            FileSystem fs = rootPath.getFileSystem();
            this.watcher = fs.newWatchService();
            registerAll(watcher, rootPath);
            Collection<Path> policies = findAllPolicyFiles(rootPath, fs.getPathMatcher(filePatternMatch));
            this.rules = new ConcurrentHashMap(policies.size());
            Closer closer = Closer.create();
            for(Path p : policies){
                CompositeDecisionRule rule = parse(closer.register(Files.newInputStream(p)));
                if(log.isDebugEnabled()){
                    log.debug("Adding policy id=\"{}\" " +
                            "from path=\"{}\"", rule.getId(), p);
                }
                rules.put(p, rule);
            }
            closer.close();
            fsEventsExecutor.execute(new Runnable() {
                @Override
                public void run() {

                }
            });
        }
    }

    /**
     * Registers watcher with the given directory and all sub-directories
     * *
     * @param watcher a watcher
     * @param start a root folder path
     * @throws IOException if an error occurs
     */
    private static void registerAll(final WatchService watcher, final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Finds all files matching given matcher starting from the given folder
     * *
     * @param start a root folder path
     * @param matcher a name matcher
     * @return {@link java.util.Collection} of {@link java.nio.file.Path} instances
     * @throws IOException if an IO error occurs
     */
    private static Collection<Path> findAllPolicyFiles(final Path start, 
                                                       final PathMatcher matcher) throws IOException{
        final ImmutableSet.Builder<Path> b = ImmutableSet.builder();
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, 
                                             BasicFileAttributes attrs) throws IOException {
                if(matcher.matches(file.getFileName())){
                   if(log.isDebugEnabled()){
                       log.debug("Found policy file=\"{}\"", file.toString());
                   }
                   b.add(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, 
                                                   IOException exc) throws IOException {
                if(log.isWarnEnabled()){
                    log.warn("Failed to access file=\"{}\"", file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir,
                                                     BasicFileAttributes attrs) {
                if(log.isDebugEnabled()){
                    log.debug("Visiting directory=\"{}\"", dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return b.build();
    }

    @Override
    public void close() throws IOException {
        if(watcher != null){
            watcher.close();
        }
        shutdownExecutor(notifyExecutor, 500, TimeUnit.MILLISECONDS);
        shutdownExecutor(notifyExecutor, 500, TimeUnit.MILLISECONDS);
    }
    
    public static class Builder 
            extends AbstractPolicySource.Builder<Builder>
    {
        private Path path;
        private String patternMatch = DEFAULT_POLICY_FILE_MATCH_PATTERN;
        
        
        private Builder(String id){
            super(id);
        }

        /**
         * Sets root path for policy source
         * *
         * @param path a root folder path
         * @return reference to this builder
         */
        public Builder rootPath(Path path){
            Verify.verifyNotNull(path);
            Verify.verify(!Files.isDirectory(path), 
                    "Path=\"%s\" is NOT a directory", path);
            this.path = path.isAbsolute()?path:path.toAbsolutePath();
            return this;
        }

        /**
         * Sets root path for policy source
         * *
         * @param path a root folder path
         * @return reference to this builder
         */
        public Builder rootPath(String path){
            Verify.verify(!Strings.isNullOrEmpty(path));
            return rootPath(FileSystems.getDefault().getPath(path));
        }

        /**
         * Sets a pattern match for policy files
         *
         * @param patternMatch a pattern match expression
         * @return reference to this builder
         */
        public Builder filePatternMatch(String patternMatch){
            Verify.verify(!Strings.isNullOrEmpty(patternMatch));
            this.patternMatch = patternMatch;
            return this;
            
        }
        
        @Override
        public Builder getThis(){
            return this;
        }
        
        @Override
        public PolicySource build(){
            return new FilesystemPolicySource(this);
        }
    }
}
