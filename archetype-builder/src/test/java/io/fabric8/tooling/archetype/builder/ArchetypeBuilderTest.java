/**
 *  Copyright 2005-2015 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package io.fabric8.tooling.archetype.builder;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import io.fabric8.tooling.archetype.ArchetypeUtils;
import io.fabric8.utils.Files;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class ArchetypeBuilderTest {

    public static Logger LOG = LoggerFactory.getLogger(ArchetypeBuilderTest.class);

    private String basedir = System.getProperty("basedir");
    private ArchetypeBuilder archetypeBuilder;
    private CatalogBuilder catalogBuilder;
    private File catalogFile;
    private ArchetypeUtils archetypeUtils;

    @Before
    public void init() throws IOException {
        if (basedir == null) {
            basedir = ".";
        }
        catalogFile = new File(basedir, "target/test-archetypes/archetype-catalog.xml").getCanonicalFile();
        catalogBuilder = new CatalogBuilder(catalogFile);
        catalogBuilder.setIndentSize(4);

        archetypeBuilder = new ArchetypeBuilder();
        archetypeBuilder.setIndentSize(4);

        archetypeUtils = new ArchetypeUtils();
    }

    @Test
    public void buildAllExampleArchetypes() throws Exception {
        File srcDir = new File(basedir, "src/test/examples").getCanonicalFile();

        try {
            List<String> dirs = new ArrayList<String>();
            archetypeBuilder.generateArchetypes("java", srcDir, new File(basedir, "target/test-archetypes"), true, dirs);
        } finally {
            LOG.info("Completed the generation. Closing!");
        }

        Collection<File> files = Files.recursiveList(new File("target/test-archetypes/java-hello-world-archetype"), new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return true;
            }
        });

        String[] resources = new String[] {
            "",
            "/pom.xml",
            "/.gitignore",
            "/src",
            "/src/main",
            "/src/main/resources",
            "/src/main/resources/archetype-resources",
            "/src/main/resources/archetype-resources/ReadMe.txt",
            "/src/main/resources/archetype-resources/pom.xml",
            "/src/main/resources/archetype-resources/src",
            "/src/main/resources/archetype-resources/src/test",
            "/src/main/resources/archetype-resources/src/test/java",
            "/src/main/resources/archetype-resources/src/test/java/HelloTest.java",
            "/src/main/resources/archetype-resources/src/main",
            "/src/main/resources/archetype-resources/src/main/resources",
            "/src/main/resources/archetype-resources/src/main/resources/application.properties",
            "/src/main/resources/archetype-resources/src/main/java",
            "/src/main/resources/archetype-resources/src/main/java/impl",
            "/src/main/resources/archetype-resources/src/main/java/impl/DefaultHello.java",
            "/src/main/resources/archetype-resources/src/main/java/HelloInterface.java",
            "/src/main/resources-filtered",
            "/src/main/resources-filtered/META-INF",
            "/src/main/resources-filtered/META-INF/maven",
            "/src/main/resources-filtered/META-INF/maven/archetype-metadata.xml",
        };
        for (String r : resources) {
            File file = new File("target/test-archetypes/java-hello-world-archetype" + r);
            assertTrue(r + " should exists", files.remove(file));
        }
        assertTrue("Failed to create correct Archetype project", files.isEmpty());
    }

    @Test
    public void relativePaths() throws Exception {
        Arrays.asList(File.listRoots());
        File base = new File("/tmp/x");
        File nested = new File("/tmp/x/y");
        assertThat(archetypeUtils.relativePath(base, nested), equalTo("y"));

        base = new File("/tmp/x");
        nested = new File("/tmp/x");
        assertThat(archetypeUtils.relativePath(base, nested), equalTo(""));

        base = new File("/tmp/x/..");
        nested = new File("/tmp/x");
        assertThat(archetypeUtils.relativePath(base, nested), equalTo("x"));
    }

    @Test
    public void validSourcesAndDirectories() {
        assertTrue(archetypeUtils.isValidSourceFileOrDir(new File("/tmp/main/java")));
        assertTrue(archetypeUtils.isValidSourceFileOrDir(new File("/tmp/main/java/A.java")));

        assertTrue(archetypeUtils.isValidSourceFileOrDir(new File("/tmp/.gitignore")));
        assertTrue(archetypeUtils.isValidSourceFileOrDir(new File("/tmp/.maven-dockerinclude")));
        assertFalse(archetypeUtils.isValidSourceFileOrDir(new File("/tmp/.project")));
        assertFalse(archetypeUtils.isValidSourceFileOrDir(new File("/tmp/.idea")));
        assertFalse(archetypeUtils.isValidSourceFileOrDir(new File("/tmp/.git")));

        assertFalse(archetypeUtils.isValidSourceFileOrDir(new File("/tmp/project.iml")));
    }

    @Test
    public void findingRootPackage() throws Exception {
        assertThat(archetypeUtils.findRootPackage(new File("src/test/resources/example-1/src/main/java")),
            equalTo(new File("src/test/resources/example-1/src/main/java/io/fabric8/example/root")));
        assertThat(archetypeUtils.findRootPackage(new File("src/test/resources/example-2/src/main/java/")),
            equalTo(new File("src/test/resources/example-2/src/main/java/io/fabric8/example/root/nested")));
        try {
            archetypeUtils.findRootPackage(new File("src/test/resources/example-1/io/fabric8/example/root/A.java"));
            fail("Should fail when checking root package of file");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void validArchetypeCandidates() throws Exception {
        assertTrue(archetypeUtils.isValidProjectPom(new File("src/test/resources/example-1/pom.xml")));
        assertTrue(archetypeUtils.isValidProjectPom(new File("src/test/resources/example-2/pom.xml")));
        assertFalse(archetypeUtils.isValidProjectPom(new File("src/test/resources/example-3/pom.xml")));
    }
}
