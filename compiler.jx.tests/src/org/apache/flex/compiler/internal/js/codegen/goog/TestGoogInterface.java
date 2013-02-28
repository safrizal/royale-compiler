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

package org.apache.flex.compiler.internal.js.codegen.goog;

import org.apache.flex.compiler.common.driver.IBackend;
import org.apache.flex.compiler.internal.as.codegen.TestInterface;
import org.apache.flex.compiler.internal.js.driver.goog.GoogBackend;
import org.apache.flex.compiler.tree.as.IInterfaceNode;
import org.junit.Test;

/**
 * This class tests the production of valid 'goog' JS code for Interface production.
 * 
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class TestGoogInterface extends TestInterface
{
	@Override
    @Test
    public void testSimple()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA{}");
        asBlockWalker.visitInterface(node);
        assertOut("/**\n * @interface\n */\npublic interface IA {\n}");
    }

	@Override
    @Test
    public void testSimpleExtends()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA extends IB{}");
        asBlockWalker.visitInterface(node);
        assertOut("/**\n * @interface\n * @extends {IB}\n */\npublic interface IA {\n}");
    }

	@Override
    @Test
    public void testSimpleExtendsMultiple()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA extends IB, IC, ID {}");
        asBlockWalker.visitInterface(node);
        assertOut("/**\n * @interface\n * @extends {IB}\n * @extends {IC}\n * @extends {ID}\n */\npublic interface IA {\n}");
    }

	@Override
    @Test
    public void testQualifiedExtendsMultiple()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA extends foo.bar.IB, baz.goo.IC, foo.ID {}");
        asBlockWalker.visitInterface(node);
        assertOut("/**\n * @interface\n * @extends {foo.bar.IB}\n * @extends {baz.goo.IC}\n * @extends {foo.ID}\n */\npublic interface IA {\n}");
    }

	@Override
    @Test
    public void testAccessors()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA {"
                + "function get foo1():Object;"
                + "function set foo1(value:Object):void;}");
        asBlockWalker.visitInterface(node);
        assertOut("/**\n * @interface\n */\npublic interface IA {\n}\nIA.prototype.foo1;");
    }

	@Override
    @Test
    public void testMethods()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA {"
                + "function baz1():Object;"
                + "function baz2(value:Object):void;}");
        asBlockWalker.visitInterface(node);
        assertOut("/**\n * @interface\n */\npublic interface IA {\n}\nIA.prototype.baz1 = function();\nIA.prototype.baz2 = function(value);");
    }

	@Override
    @Test
    public void testAccessorsMethods()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA {"
                + "function get foo1():Object;"
                + "function set foo1(value:Object):void;"
                + "function baz1():Object;"
                + "function baz2(value:Object):void;}");
        asBlockWalker.visitInterface(node);
        assertOut("/**\n * @interface\n */\npublic interface IA {\n}\nIA.prototype.foo1;\nIA.prototype.baz1 = function();\nIA.prototype.baz2 = function(value);");
    }

    protected IBackend createBackend()
    {
        return new GoogBackend();
    }

}
