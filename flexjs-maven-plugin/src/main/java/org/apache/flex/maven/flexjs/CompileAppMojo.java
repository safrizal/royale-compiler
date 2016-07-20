/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flex.maven.flexjs;

import org.apache.flex.tools.FlexTool;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.velocity.VelocityContext;

import java.io.File;
import java.util.List;

/**
 * goal which compiles a project into a flexjs sef application.
 */
@Mojo(name="compile-app",defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class CompileAppMojo
    extends BaseMojo
{

    @Parameter
    private String mainClass;

    @Parameter(defaultValue = "${project.artifactId}-${project.version}.swf")
    private String flashOutputFileName;

    @Parameter(defaultValue = "${project.artifactId}-${project.version}.war")
    private String javascriptOutputFileName;

    @Parameter(defaultValue = "namespaces")
    protected String namespaceDirectory;

    @Parameter(defaultValue = "false")
    protected boolean outputJavaScript;

    @Parameter(defaultValue = "false")
    protected boolean removeCirculars;

    @Component
    protected MavenProjectHelper mavenProjectHelper;

    @Override
    protected String getToolGroupName() {
        if(outputJavaScript) {
            return "FlexJS";
        }
        return "Falcon";
    }

    @Override
    protected String getFlexTool() {
        return FlexTool.FLEX_TOOL_MXMLC;
    }

    @Override
    protected String getConfigFileName() {
        if(outputJavaScript) {
            return "compile-app-javascript-config.xml";
        }
        return "compile-app-flash-config.xml";
    }

    @Override
    protected VelocityContext getVelocityContext() throws MojoExecutionException {
        VelocityContext context = super.getVelocityContext();
        context.put("removeCirculars", removeCirculars);
        return context;
    }

    @Override
    protected File getOutput() {
        if(outputJavaScript) {
            return new File(outputDirectory, "javascript");
        }
        return new File(outputDirectory, flashOutputFileName);
    }

    @Override
    protected List<String> getCompilerArgs(File configFile) throws MojoExecutionException {
        if(mainClass == null) {
            throw new MojoExecutionException("The mainClass has to be declared for SWF type modules.");
        }
        String mainClassPath = getSourcePath(mainClass);
        if(mainClassPath == null) {
            throw new MojoExecutionException("Could not find main class");
        }
        List<String> args = super.getCompilerArgs(configFile);
        args.add(mainClassPath);
        return args;
    }

    @Override
    public void execute() throws MojoExecutionException {
        super.execute();

        if(getOutput().exists()) {
            // If we are building JavaScript output, the war plugin will attach the war
            if(!outputJavaScript) {
                // Attach the file created by the compiler as artifact file to maven.
                project.getArtifact().setFile(getOutput());
            }
        }
    }

    /**
     * @return list of the explicitly defined as well as the automatically detected namespaces.
     */
    /*@Override
    @SuppressWarnings("unchecked")
    protected Namespace[] getNamespaces() {
        File namespaceDir = new File(outputDirectory, namespaceDirectory);
        if(namespaceDir.exists()) {
            File[] namespaceFiles = namespaceDir.listFiles();
            if(namespaceFiles != null) {
                List<Namespace> autoNamespaces = new ArrayList<Namespace>(namespaceFiles.length);
                // Read the namespace-uri attribute of each file and add them to the namespaces.
                for(File namespaceFile : namespaceFiles) {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    try {
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document namespaceDoc = builder.parse(namespaceFile);
                        String namespaceUri = namespaceDoc.getDocumentElement().getAttribute("namespace-uri");
                        Namespace namespace = new Namespace();
                        namespace.setUri(namespaceUri);
                        namespace.setManifest(namespaceFile.getPath());
                        autoNamespaces.add(namespace);
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if(!autoNamespaces.isEmpty()) {
                    List<Namespace> namespaces;
                    Namespace[] manualNamespaces = super.getNamespaces();
                    if(manualNamespaces != null) {
                        namespaces = new ArrayList<Namespace>(Arrays.asList(manualNamespaces));
                        namespaces.addAll(autoNamespaces);
                    } else {
                        namespaces = autoNamespaces;
                    }
                    return namespaces.toArray(new Namespace[0]);
                }
            }
        }
        return super.getNamespaces();
    }*/

    @Override
    protected List<Define> getDefines() {
        List<Define> defines = super.getDefines();
        defines.add(new Define("COMPILE::JS", "false"));
        defines.add(new Define("COMPILE::SWF", "true"));
        return defines;
    }

    @Override
    protected boolean includeLibrary(Artifact library) {
        // Strip out all externs except if the dependency was declared inside the pom itself.
        return !"typedefs".equalsIgnoreCase(library.getClassifier()) ||
                (outputJavaScript && library.getDependencyTrail().size() == 2);
    }

    @Override
    protected boolean isForceSwcExternalLibraryPath() {
        // The forceSwcExternalLibraryPath should only apply to Flash compilations.
        if(outputJavaScript) {
            return false;
        }
        return super.isForceSwcExternalLibraryPath();
    }

    /*private void zipDirectory(File source, File target) {
        byte[] buffer = new byte[1024];
        try {
            FileOutputStream fos = new FileOutputStream(target);
            ZipOutputStream zos = new ZipOutputStream(fos);

            FileInputStream in = null;
            Set<String> files = getFiles(source, source);
            for (String file : files) {
                ZipEntry ze = new ZipEntry(file);
                try {
                    zos.putNextEntry(ze);
                    in = new FileInputStream(source + File.separator + file);
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            // Ignore ...
                        }
                    }
                }
            }
            zos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Set<String> getFiles(File source, File curFile) {
        if(curFile.isDirectory()) {
            Set<String> files = new HashSet<String>();
            File[] children = curFile.listFiles();
            if(children != null) {
                for (File child : children) {
                    if(child.isFile()) {
                        String curFileRelativePath = child.getPath().substring(source.getPath().length() + 1);
                        files.add(curFileRelativePath);
                    } else {
                        files.addAll(getFiles(source, child));
                    }
                }
            }
            return files;
        } else {
            String curFileRelativePath = curFile.getPath().substring(source.getPath().length() + 1);
            return Collections.singleton(curFileRelativePath);
        }
    }*/

}