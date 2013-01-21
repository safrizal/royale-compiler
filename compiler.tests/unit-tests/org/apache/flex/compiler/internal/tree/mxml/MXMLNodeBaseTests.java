/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.flex.compiler.internal.tree.mxml;

import static org.junit.Assert.assertNotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.flex.compiler.internal.projects.FlexProject;
import org.apache.flex.compiler.internal.projects.FlexProjectConfigurator;
import org.apache.flex.compiler.internal.units.SourceCompilationUnitFactory;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.mxml.IMXMLNamespaceMapping;
import org.apache.flex.compiler.mxml.MXMLNamespaceMapping;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.utils.EnvProperties;
import org.apache.flex.utils.FilenameNormalization;
import org.apache.flex.utils.StringUtils;
import org.junit.Ignore;

/**
 * JUnit tests for {@link MXMLNodeBase}.
 * 
 * @author Gordon Smith
 */
@Ignore
public class MXMLNodeBaseTests 
{
	private static EnvProperties env = EnvProperties.initiate();
	
	protected static Workspace workspace = new Workspace();
	
	protected FlexProject project;
	
 	protected String[] getTemplate()
	{
 		// Tests of nodes for class-definition-level tags like <Declarations>,
 		// <Library>,  <Metadata>, <Script>, and <Style> use this document template.
 		// Tests for nodes produced by tags that appear at other locations
 		// override getTemplate() and getMXML().
		return new String[] 
		{
		    "<d:Sprite xmlns:fx='http://ns.adobe.com/mxml/2009'",
		    "          xmlns:d='flash.display.*'",
		    "          xmlns:s='library://ns.adobe.com/flex/spark'",
		    "          xmlns:mx='library://ns.adobe.com/flex/mx'>",
			"    %1",
		    "</d:Sprite>"
		};
    };
	
	protected String getMXML(String[] code)
    {
        String mxml = StringUtils.join(getTemplate(), "\n");
        mxml = mxml.replace("%1", StringUtils.join(code, "\n    "));
        return mxml;
    }
	    
    protected IMXMLFileNode getMXMLFileNode(String[] code)
    {
    	String mxml = getMXML(code);
    	return getMXMLFileNode(mxml);
    }

    protected IMXMLFileNode getMXMLFileNode(String mxml)
	{
		assertNotNull("Environment variable FLEX_HOME is not set", env.SDK);
		assertNotNull("Environment variable PLAYERGLOBAL_HOME is not set", env.FPSDK);
		
		project = new FlexProject(workspace);
		FlexProjectConfigurator.configure(project);		
		
		String tempDir = FilenameNormalization.normalize("temp"); // ensure this exists
				
		File tempMXMLFile = null;
		try
		{
			tempMXMLFile = File.createTempFile(getClass().getSimpleName(), ".mxml", new File(tempDir));
			tempMXMLFile.deleteOnExit();

			BufferedWriter out = new BufferedWriter(new FileWriter(tempMXMLFile));
		    out.write(mxml);
		    out.close();
		}
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}
		
		List<File> sourcePath = new ArrayList<File>();
		sourcePath.add(new File(tempDir));
		project.setSourcePath(sourcePath);

		// Compile the code against playerglobal.swc.
		List<File> libraries = new ArrayList<File>();
		libraries.add(new File(FilenameNormalization.normalize(env.FPSDK + "\\11.1\\playerglobal.swc")));
		libraries.add(new File(FilenameNormalization.normalize(env.SDK + "\\frameworks\\libs\\framework.swc")));
		libraries.add(new File(FilenameNormalization.normalize(env.SDK + "\\frameworks\\libs\\rpc.swc")));
		libraries.add(new File(FilenameNormalization.normalize(env.SDK + "\\frameworks\\libs\\spark.swc")));
		project.setLibraries(libraries);
		
		// Use the MXML 2009 manifest.
		List<IMXMLNamespaceMapping> namespaceMappings = new ArrayList<IMXMLNamespaceMapping>();
		IMXMLNamespaceMapping mxml2009 = new MXMLNamespaceMapping(
		    "http://ns.adobe.com/mxml/2009", env.SDK + "\\frameworks\\mxml-2009-manifest.xml");
		namespaceMappings.add(mxml2009);
		project.setNamespaceMappings(namespaceMappings);
				
		ICompilationUnit cu = null;
        String normalizedMainFileName = FilenameNormalization.normalize(tempMXMLFile.getAbsolutePath());
		//String normalizedMainFileName = FilenameNormalization.normalize("code.mxml");
        SourceCompilationUnitFactory compilationUnitFactory = project.getSourceCompilationUnitFactory();
        File normalizedMainFile = new File(normalizedMainFileName);
        if (compilationUnitFactory.canCreateCompilationUnit(normalizedMainFile))
        {
            Collection<ICompilationUnit> mainFileCompilationUnits = workspace.getCompilationUnits(normalizedMainFileName, project);
            for (ICompilationUnit cu2 : mainFileCompilationUnits)
            {
            	if (cu2 != null)
            		cu = cu2;
            }
        }
		
        // Build the AST.
		IMXMLFileNode fileNode = null;
		try
		{
			fileNode = (IMXMLFileNode)cu.getSyntaxTreeRequest().get().getAST();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		return fileNode;
	}
    
	protected IASNode findFirstDescendantOfType(IASNode node, Class<? extends IASNode> nodeType)
	{
		int n = node.getChildCount();
		for (int i = 0; i < n; i++)
		{
			IASNode child = node.getChild(i);
			if (nodeType.isInstance(child))
				return child;
			
			IASNode found = findFirstDescendantOfType(child, nodeType);
			if (found != null)
				return found;
		}
		
		return null;
	}
}
