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

package org.apache.flex.compiler.internal.codegen.externals.reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.flex.compiler.clients.ExternCConfiguration;
import org.apache.flex.compiler.clients.ExternCConfiguration.ExcludedMemeber;
import org.apache.flex.compiler.internal.codegen.externals.utils.DebugLogUtils;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.NodeUtil;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;

public class ReferenceModel
{
    private ExternCConfiguration configuration;
    private Compiler jscompiler;

    private List<String> namespaces = new ArrayList<String>();

    private HashMap<String, ClassReference> typedefs = new HashMap<String, ClassReference>();
    private HashMap<String, ClassReference> classes = new HashMap<String, ClassReference>();
    private HashMap<String, FunctionReference> functions = new HashMap<String, FunctionReference>();
    private HashMap<String, ConstantReference> constants = new HashMap<String, ConstantReference>();

    //    public Compiler getJSCompiler()
    //    {
    //        return jscompiler;
    //    }

    public void setJSCompiler(Compiler compiler)
    {
        this.jscompiler = compiler;
    }

    public ExternCConfiguration getConfiguration()
    {
        return configuration;
    }

    public ClassReference getObjectReference()
    {
        return classes.get("Object");
    }

    public Collection<String> getNamespaces()
    {
        return namespaces;
    }

    public Collection<ClassReference> getTypedefs()
    {
        return typedefs.values();
    }

    public Collection<ClassReference> getClasses()
    {
        return classes.values();
    }

    public Collection<FunctionReference> getFunctions()
    {
        return functions.values();
    }

    public Collection<ConstantReference> getConstants()
    {
        return constants.values();
    }

    public ReferenceModel(ExternCConfiguration config)
    {
        this.configuration = config;
    }

    public ClassReference getClassReference(String qualifiedName)
    {
        return classes.get(qualifiedName);
    }

    public ClassReference getInterfaceReference(String qualifiedName)
    {
        ClassReference reference = classes.get(qualifiedName);
        if (reference != null && reference.isInterface())
            return reference;
        return null;
    }

    public void addNamespace(Node node, String qualifiedName)
    {
        if (namespaces.contains(qualifiedName))
        {
            err("Duplicate namesapce [" + qualifiedName + "]");
            return;
        }

        log("Model.addNamespace(" + qualifiedName + ")");

        namespaces.add(qualifiedName);
    }

    public void addClass(Node node, String qualifiedName)
    {
        if (classes.containsKey(qualifiedName))
        {
            err("Duplicate class [" + qualifiedName + "]");
            return;
        }

        log("Model.addClass(" + qualifiedName + ")");
        ClassReference reference = new ClassReference(this, node, qualifiedName);
        classes.put(qualifiedName, reference);
    }

    public void addTypeDef(Node node, String qualifiedName)
    {
        if (typedefs.containsKey(qualifiedName))
        {
            err("Duplicate @typedef [" + qualifiedName + "]");
            return;
        }

        log("Model.addTypeDef(" + qualifiedName + ")");

        ClassReference reference = new ClassReference(this, node, qualifiedName);
        typedefs.put(qualifiedName, reference);
    }

    public void addInterface(Node node, String qualifiedName)
    {
        if (classes.containsKey(qualifiedName))
        {
            err("Duplicate @interface [" + qualifiedName + "]");
            return;
        }

        log("Model.addInterface(" + qualifiedName + ")");

        ClassReference reference = new ClassReference(this, node, qualifiedName);
        classes.put(qualifiedName, reference);
    }

    public void addFinalClass(Node node, String qualifiedName)
    {
        if (classes.containsKey(qualifiedName))
        {
            err("Duplicate final class [" + qualifiedName + "]");
            return;
        }

        log("Model.addFinalClass(" + qualifiedName + ")");

        ClassReference reference = new ClassReference(this, node, qualifiedName);
        reference.setFinal(true);
        classes.put(qualifiedName, reference);
    }

    public void addFunction(Node node, String qualifiedName)
    {
        if (functions.containsKey(qualifiedName))
        {
            err("Duplicate global function [" + qualifiedName + "]");
            return;
        }

        log("Model.addFunction(" + qualifiedName + ")");

        FunctionReference reference = new FunctionReference(this, node,
                qualifiedName, node.getJSDocInfo());
        functions.put(qualifiedName, reference);
    }

    public boolean hasClass(String className)
    {
        return classes.containsKey(className);
    }

    public boolean hasConstant(String qualifiedName)
    {
        return constants.containsKey(qualifiedName);
    }

    public void addConstant(Node node, String qualifiedName)
    {
        if (constants.containsKey(qualifiedName))
        {
            // XXX Record warning;
            return;
        }

        log("Model.addConstant(" + qualifiedName + ")");

        ConstantReference reference = new ConstantReference(this, node,
                qualifiedName, node.getJSDocInfo());
        constants.put(qualifiedName, reference);
    }

    public void addConstantType(Node node, String qualifiedName, JSType type)
    {
        if (constants.containsKey(qualifiedName))
        {
            // XXX Record warning;
            return;
        }

        log("Model.addConstantType(" + qualifiedName + ")");

        ConstantReference reference = new ConstantReference(this, node,
                qualifiedName, node.getJSDocInfo(), type);
        constants.put(qualifiedName, reference);
    }

    public void addField(Node node, String className, String memberName)
    {
        ClassReference classReference = getClassReference(className);
        if (classReference != null)
            classReference.addField(node, memberName, node.getJSDocInfo(),
                    false);
    }

    public void addStaticField(Node node, String className, String memberName)
    {
        ClassReference classReference = getClassReference(className);
        // XXX this is here because for now, the doc might be on the parent ASSIGN node
        // if it's a static property with a value
        JSDocInfo comment = NodeUtil.getBestJSDocInfo(node);
        if (classReference != null)
        {
            classReference.addField(node, memberName, comment, true);
        }
        else
        {
            err(">>>> {ReferenceModel} Class [" + className + "] not found in "
                    + node.getSourceFileName());
        }
    }

    public void addMethod(Node node, String className, String memberName)
    {
        JSDocInfo comment = NodeUtil.getBestJSDocInfo(node);
        ClassReference classReference = getClassReference(className);
        if (classReference != null)
            classReference.addMethod(node, memberName, comment, false);
    }

    public void addStaticMethod(Node node, String className, String memberName)
    {
        ClassReference classReference = getClassReference(className);
        // XXX this is here because for now, the doc might be on the parent ASSIGN node
        // if it's a static property with a value
        JSDocInfo comment = NodeUtil.getBestJSDocInfo(node);
        if (classReference != null)
        {
            classReference.addMethod(node, memberName, comment, true);
        }
        else
        {
            err(">>>> {ReferenceModel} Class [" + className + "] not found in "
                    + node.getSourceFileName());
        }
    }

    public final JSType evaluate(JSTypeExpression expression)
    {
        JSType jsType = expression.evaluate(null, jscompiler.getTypeRegistry());
        return jsType;
    }

    //--------------------------------------------------------------------------

    public ExcludedMemeber isExcludedClass(ClassReference classReference)
    {
        return getConfiguration().isExcludedClass(classReference);
    }

    public ExcludedMemeber isExcludedMember(ClassReference classReference,
            MemberReference memberReference)
    {
        return getConfiguration().isExcludedMember(classReference,
                memberReference);
    }

    //--------------------------------------------------------------------------

    protected void log(Node n)
    {
        DebugLogUtils.err(n);
    }

    protected void err(Node n)
    {
        DebugLogUtils.err(n);
    }

    protected void log(String message)
    {
        DebugLogUtils.log(message);
    }

    protected void err(String message)
    {
        DebugLogUtils.err(message);
    }

}
