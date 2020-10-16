/*
 * Copyright 2019
 * Ubiquitous Knowledge Processing (UKP) Lab Technische Universität Darmstadt
 * and Language Technology lab Universität Hamburg
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
package de.tudarmstadt.ukp.clarin.webanno.codebook.ui.suggestion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.cas.CAS;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.CollectionModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import de.tudarmstadt.ukp.clarin.webanno.api.AnnotationSchemaService;
import de.tudarmstadt.ukp.clarin.webanno.api.DocumentService;
import de.tudarmstadt.ukp.clarin.webanno.api.ProjectService;
import de.tudarmstadt.ukp.clarin.webanno.api.WebAnnoConst;
import de.tudarmstadt.ukp.clarin.webanno.api.annotation.AnnotationEditorExtensionRegistry;
import de.tudarmstadt.ukp.clarin.webanno.codebook.adapter.CodebookAdapter;
import de.tudarmstadt.ukp.clarin.webanno.codebook.api.coloring.ColoringStrategy;
import de.tudarmstadt.ukp.clarin.webanno.codebook.model.Codebook;
import de.tudarmstadt.ukp.clarin.webanno.codebook.model.CodebookFeature;
import de.tudarmstadt.ukp.clarin.webanno.codebook.model.CodebookTag;
import de.tudarmstadt.ukp.clarin.webanno.codebook.service.CodebookDiff;
import de.tudarmstadt.ukp.clarin.webanno.codebook.service.CodebookSchemaService;
import de.tudarmstadt.ukp.clarin.webanno.codebook.ui.curation.CodebookCurationModel;
import de.tudarmstadt.ukp.clarin.webanno.curation.casdiff.CasDiff.DiffResult;
import de.tudarmstadt.ukp.clarin.webanno.curation.storage.CurationDocumentService;
import de.tudarmstadt.ukp.clarin.webanno.model.AnnotationDocument;
import de.tudarmstadt.ukp.clarin.webanno.model.AnnotationDocumentState;

public class CodebookSuggestionPanel
    extends Panel
{
    private static final long serialVersionUID = -9151455840010092452L;

    private @SpringBean ProjectService projectRepository;

    private @SpringBean DocumentService documentService;
    private @SpringBean CurationDocumentService curationDocumentService;
    private @SpringBean AnnotationSchemaService annotationService;
    private @SpringBean CodebookSchemaService codebookService;
    private @SpringBean AnnotationEditorExtensionRegistry extensionRegistry;

    private CodebookCurationModel cModel;
    private final WebMarkupContainer codebooksGroup;
    private final PageableListView<CodebookSuggestion> suggestions;

    public CodebookSuggestionPanel(String id, IModel<CodebookCurationModel> aModel)
    {
        super(id, aModel);

        setOutputMarkupId(true);

        cModel = aModel.getObject();
        suggestions = new PageableListView<CodebookSuggestion>("suggestions",
                (IModel<? extends List<CodebookSuggestion>>) cModel.getCodebookSuggestions(),
                cModel.getCodebookSuggestions().size())
        {

            private static final long serialVersionUID = -3591948738133097041L;

            @Override
            protected void populateItem(final ListItem<CodebookSuggestion> item)
            {
                final CodebookSuggestion suggestion = item.getModelObject();
                item.add(new Label("codebook", suggestion.getCodebook().getUiName()));
                item.add(new Label("username", suggestion.getUsername()));
                item.add(new Label("annotation", suggestion.getAnnotation())
                        .add(new AttributeModifier("style",
                                ColoringStrategy.getCodebookDiffColor(suggestion.isHasDiff()))));

                AjaxLink<String> alink = new AjaxLink<String>("merge",
                        Model.of(suggestion.getAnnotation()))
                {

                    private static final long serialVersionUID = -4235258547224541472L;

                    @Override
                    public void onClick(AjaxRequestTarget aTarget)
                    {
                        onMerge(aTarget, suggestion.getFeature(), suggestion.getAnnotation());
                        onShowSuggestions(aTarget, suggestion.getFeature());

                    }
                };
                // alink.add(new AttributeModifier("style",
                // ColoringStrategy.getCodebookDiffColor(suggestion.isHasDiff())));
                item.add(alink);
            }
        };
        suggestions.setOutputMarkupId(true);
        codebooksGroup = new WebMarkupContainer("codebooksGroup");
        codebooksGroup.setOutputMarkupId(true);

        codebooksGroup.add(suggestions);

        IModel<Collection<Codebook>> codebooksToAdeModel = new CollectionModel<>(new ArrayList<>());
        Form<Collection<Codebook>> form = new Form<>("form", codebooksToAdeModel);
        add(form);
        form.add(codebooksGroup);
    }

    public CodebookCurationModel getModelObject()
    {
        return (CodebookCurationModel) getDefaultModelObject();
    }

    public void setSuggestionModel(AjaxRequestTarget aTarget, CodebookFeature aFeature)
    {
        List<CodebookSuggestion> suggestionsModel = getSuggestions(aFeature);
        suggestions.setModelObject(suggestionsModel);
        suggestions.setItemsPerPage(suggestionsModel.size());
    }

    public void setModel(AjaxRequestTarget aTarget, CodebookCurationModel aModel)
    {
        cModel = aModel;
        setDefaultModelObject(cModel);
    }

    List<String> getTags(Codebook aCodebook)
    {
        /*
         * TODO I dont see the point of the code below and would suggest to remove it if
         * (codebookService.listCodebookFeature(aCodebook) == null ||
         * codebookService.listCodebookFeature(aCodebook).size() == 0) { return new ArrayList<>(); }
         * CodebookFeature codebookFeature = codebookService.listCodebookFeature(aCodebook).get(0);
         * if (codebookFeature.getCodebook() == null) { return new ArrayList<>(); }
         */
        List<String> tags = new ArrayList<>();
        for (CodebookTag tag : codebookService.listTags(aCodebook)) {
            tags.add(tag.getName());
        }
        return tags;
    }

    private List<CodebookSuggestion> getSuggestions(CodebookFeature feature)
    {
        Map<String, CAS> jCases = getSuggestionCases();
        List<Codebook> types = new ArrayList<>();
        types.add(feature.getCodebook());
        CodebookAdapter adapter = new CodebookAdapter(feature.getCodebook());
        List<CodebookSuggestion> suggestions = new ArrayList<>();

        for (String username : jCases.keySet()) {

            if (username.equals(WebAnnoConst.CURATION_USER)) {
                continue;
            }
            String existingCode = (String) adapter.getExistingCodeValue(jCases.get(username),
                    feature);
            Map<String, CAS> suggestionCas = new HashMap<>();
            suggestionCas.put(username, jCases.get(username));
            suggestionCas.put(WebAnnoConst.CURATION_USER, jCases.get(WebAnnoConst.CURATION_USER));
            CodebookSuggestion suggestion = new CodebookSuggestion(username, existingCode,
                    isDiffs(feature.getCodebook(), types, suggestionCas), feature.getCodebook(),
                    feature);
            suggestions.add(suggestion);

        }
        return suggestions;
    }

    private Map<String, CAS> getSuggestionCases()
    {
        Map<String, CAS> userCASes = new HashMap<>();
        List<AnnotationDocument> annotationDocuments = documentService
                .listAnnotationDocuments(cModel.getDocument());
        for (AnnotationDocument annotationDocument : annotationDocuments) {
            String username = annotationDocument.getUser();
            if (annotationDocument.getState().equals(AnnotationDocumentState.FINISHED)
                    || username.equals(WebAnnoConst.CURATION_USER)) {
                CAS jCas;
                try {
                    jCas = documentService.readAnnotationCas(annotationDocument);
                    userCASes.put(username, jCas);
                }
                catch (IOException e) {
                    error("Unable to load the curation CASes" + e.getMessage());
                }

            }
        }
        try {
            userCASes.put(WebAnnoConst.CURATION_USER, getCas());
        }
        catch (IOException e) {
            error("Unable to load the curation CASes" + e.getMessage());
        }

        return userCASes;
    }

    private boolean isDiffs(Codebook codebook, List<Codebook> types, Map<String, CAS> jCases)
    {
        DiffResult diff = CodebookDiff.doCodebookDiff(codebookService, codebook.getProject(),
                // CurationUtil.getCodebookTypes(jCases.get(CurationUtil.CURATION_USER), types),
                null, jCases, 0, 0);
        if (diff.getIncompleteConfigurationSets().size() > 0) {
            return true;
        }
        return diff.getDifferingConfigurationSets().size() > 0;
    }

    public CAS getCas() throws IOException
    {
        CodebookCurationModel state = getModelObject();
        return curationDocumentService.readCurationCas(state.getDocument());

    }

    protected void onMerge(AjaxRequestTarget aTarget, CodebookFeature aFeature, String aAnnotation)
    {
        // Overriden in CurationPanel
    }

    protected void onShowSuggestions(AjaxRequestTarget aTarget, CodebookFeature aFeature)
    {
        // Overriden in CurationPanel
    }

}
