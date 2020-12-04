/*
 * Copyright 2017
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
package de.tudarmstadt.ukp.clarin.webanno.ui.core.settings;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import de.tudarmstadt.ukp.clarin.webanno.model.Project;

public abstract class ProjectSettingsPanelBase
    extends Panel
{
    private static final long serialVersionUID = -6844742938608503193L;

    public ProjectSettingsPanelBase(String id)
    {
        super(id);
    }

    public ProjectSettingsPanelBase(String id, IModel<Project> aProjectModel)
    {
        super(id, aProjectModel);
    }

    public void setModel(IModel<Project> aModel)
    {
        setDefaultModel(aModel);
    }

    @SuppressWarnings("unchecked")
    public IModel<Project> getModel()
    {
        return (IModel<Project>) getDefaultModel();
    }

    public void setModelObject(Project aModel)
    {
        setDefaultModelObject(aModel);
    }

    public Project getModelObject()
    {
        return (Project) getDefaultModelObject();
    }

}
