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

package org.apache.flex.swf.tags;

import org.apache.flex.swf.TagType;

/**
 * Base class for all SWF tags.
 */
public abstract class Tag implements ITag
{
    /**
     * Constructor.
     * 
     * @param tagType A {@link TagType} value, such as <code>DoABC</code>,
     * specifying what kind of tag this is.
     */
    public Tag(TagType tagType)
    {
        this.tagType = tagType;
    }
    
    /**
     * Specifies what kind of tag this is.
     */
    private final TagType tagType;

    /**
     * Get tag type code.
     * 
     * @return tag type code
     */
    @Override
    public TagType getTagType()
    {
        return tagType;
    }

    /**
     * Print a base tag by its tag type name.
     */
    @Override
    public String toString()
    {
        return String.format("[%s] %s", tagType.name(), description());
    }

    /**
     * Sub-class can override this method to provide more information when the
     * tag object is printed out.
     * 
     * @return description text
     */
    protected String description()
    {
        return "";
    }
}