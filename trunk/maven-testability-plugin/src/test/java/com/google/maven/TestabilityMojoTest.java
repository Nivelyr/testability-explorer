package com.google.maven;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileReader;

/**
 * Test the TE maven plugin
 *
 * @author alexeagle@google.com (Alex Eagle)
 */
public class TestabilityMojoTest extends AbstractMojoTestCase {
  private TestabilityExplorerMojo mojo;

  public void testPluginPomWorks() throws Exception {
    mojo = lookupMojoFromPom("xmlTestability.xml");
    assertNotNull(mojo);
    mojo.execute();
  }

  public void testHtmlOutput() throws Exception {
    mojo = lookupMojoFromPom("printsHtml.xml");
    File outputDir = (File) getVariableValueFromObject(mojo, "outputDirectory");
    String resultFile = (String) getVariableValueFromObject(mojo, "resultfile");

    File results = new File(outputDir, resultFile + ".html");
    assertTrue("should exist: " + results.getAbsolutePath(), results.exists());
    String content = IOUtil.toString(new FileReader(results));
    // TODO: wire Guice to allow multiple report outputs in a single run - this report is empty
    //assertTrue("HTML report content: " + content, content.contains("TestabilityExplorerMojo"));
    assertTrue(results.delete());
  }

  public void testAlsoOutputsXml() throws Exception {
    mojo = lookupMojoFromPom("printsHtml.xml");
    File outputDir = (File) getVariableValueFromObject(mojo, "targetDirectory");
    String resultFile = (String) getVariableValueFromObject(mojo, "resultfile");

    File results = new File(outputDir, resultFile + ".xml");
    assertTrue("should exist: " + results.getAbsolutePath(), results.exists());
    assertTrue(IOUtil.toString(new FileReader(results)).contains("TestabilityExplorerMojo"));
    assertTrue(results.delete());
  }

  public void testNonJarProject() throws Exception {
    mojo = lookupMojoFromPom("nonJarProject.xml");
    mojo.execute();
  }

  private TestabilityExplorerMojo lookupMojoFromPom(String pom) throws Exception {
    return (TestabilityExplorerMojo) lookupMojo("testability",
        getTestFile("src/test/resources/" + pom));
  }
}
