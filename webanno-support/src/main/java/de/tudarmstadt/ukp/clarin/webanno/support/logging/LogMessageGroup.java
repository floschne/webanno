/*
 * Copyright 2019
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.clarin.webanno.support.logging;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class LogMessageGroup
    implements Serializable
{
    private static final long serialVersionUID = 997324549494420840L;

    private String name;
    private List<LogMessage> messages;

    public LogMessageGroup(String aName)
    {
        name = aName;
    }

    public LogMessageGroup(String aName, List<LogMessage> aMessages)
    {
        name = aName;
        messages = aMessages;
    }

    public String getName()
    {
        return name;
    }

    public void setMessages(List<LogMessage> aMessages)
    {
        messages = aMessages;
    }

    public List<LogMessage> getMessages()
    {
        if (messages == null) {
            return Collections.emptyList();
        }

        return messages;
    }
}
