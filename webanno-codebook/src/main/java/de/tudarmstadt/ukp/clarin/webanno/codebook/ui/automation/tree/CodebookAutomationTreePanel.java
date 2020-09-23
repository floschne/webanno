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
package de.tudarmstadt.ukp.clarin.webanno.codebook.ui.automation.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import de.tudarmstadt.ukp.clarin.webanno.codebook.ui.automation.CodebookAutomationSuggestion;
import de.tudarmstadt.ukp.clarin.webanno.codebook.ui.automation.CodebookAutomationPage;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.googlecode.wicket.kendo.ui.markup.html.link.Link;

import de.tudarmstadt.ukp.clarin.webanno.codebook.model.Codebook;
import de.tudarmstadt.ukp.clarin.webanno.codebook.model.CodebookNode;
import de.tudarmstadt.ukp.clarin.webanno.codebook.ui.tree.CodebookNodeExpansion;
import de.tudarmstadt.ukp.clarin.webanno.codebook.ui.tree.CodebookTreePanel;
import de.tudarmstadt.ukp.clarin.webanno.codebook.ui.tree.CodebookTreeProvider;

public class CodebookAutomationTreePanel
    extends CodebookTreePanel
{
    private static final long serialVersionUID = -8329270688665288003L;

    private CodebookAutomationPage parentPage;
    private Map<CodebookNode, CodebookAutomationNodePanel> nodePanels;
    private transient Map<Codebook, List<CodebookAutomationSuggestion>> automationSuggestions;

    public CodebookAutomationTreePanel(String aId, CodebookAutomationPage parentPage)
    {
        super(aId, new Model<>(null));

        // create and add expand and collapse links
        this.add(new Link<Void>("expandAll")
        {
            private static final long serialVersionUID = -2081711094768955973L;

            public void onClick()
            {
                CodebookNodeExpansion.get().expandAll();
            }
        });
        this.add(new Link<Void>("collapseAll")
        {
            private static final long serialVersionUID = -4576757597732733009L;

            public void onClick()
            {
                CodebookNodeExpansion.get().collapseAll();
            }
        });

        this.nodePanels = new HashMap<>();
        this.parentPage = parentPage;
    }

    private Map<Codebook, List<CodebookAutomationSuggestion>> createDummySuggestions() {
        Map<Codebook, List<CodebookAutomationSuggestion>> suggestions = new HashMap<>();

        List<Codebook> codebooks = this.codebookService
                .listCodebook(parentPage.getModelObject().getProject());

        for (Codebook cb : codebooks) {
            suggestions.put(cb, Collections.singletonList(new CodebookAutomationSuggestion(
                    cb,
                    parentPage.getModelObject().getDocument(),
                    "Dummy1312",
                    1.0
            )));
        }

        return suggestions;
    }

    @Override
    public void initCodebookTreeProvider()
    {
        // get all codebooks and init the provider
        List<Codebook> codebooks = this.codebookService
                .listCodebook(parentPage.getModelObject().getProject());
        this.provider = new CodebookTreeProvider(codebooks);
    }

    @Override
    public void initTree()
    {
        this.initCodebookTreeProvider();

        tree = new NestedTree<CodebookNode>("codebookCurationTree", this.provider,
                new CodebookNodeExpansionModel())
        {
            private static final long serialVersionUID = 2285250157811357702L;

            @Override
            protected Component newContentComponent(String id, IModel<CodebookNode> model)
            {
                // we save the nodes and their panels to get 'easy' access to the panels since
                // we need them later
                CodebookAutomationNodePanel nodePanel = new CodebookAutomationNodePanel(id, model,
                        automationSuggestions.get(provider.getCodebook(model.getObject())),
                        CodebookAutomationTreePanel.this);
                CodebookAutomationTreePanel.this.nodePanels.put(model.getObject(), nodePanel);
                return nodePanel;
            }
        };

        this.applyTheme();

        this.automationSuggestions = createDummySuggestions();

        tree.setOutputMarkupId(true);
        this.addOrReplace(tree);
    }

    public Map<CodebookNode, CodebookAutomationNodePanel> getNodePanels()
    {
        return nodePanels;
    }

    public CodebookAutomationPage getParentPage()
    {
        return parentPage;
    }

    public void expandNode(CodebookNode n)
    {
        tree.expand(n);
    }
}
