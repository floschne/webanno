/*
 * Copyright 2018
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
package de.tudarmstadt.ukp.clarin.webanno.project.initializers;

import static de.tudarmstadt.ukp.clarin.webanno.api.WebAnnoConst.SPAN_TYPE;
import static de.tudarmstadt.ukp.clarin.webanno.model.AnchoringMode.SINGLE_TOKEN;
import static de.tudarmstadt.ukp.clarin.webanno.model.OverlapMode.NO_OVERLAP;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.List;

import org.apache.uima.cas.CAS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import de.tudarmstadt.ukp.clarin.webanno.api.AnnotationSchemaService;
import de.tudarmstadt.ukp.clarin.webanno.api.annotation.util.JsonImportUtil;
import de.tudarmstadt.ukp.clarin.webanno.model.AnnotationFeature;
import de.tudarmstadt.ukp.clarin.webanno.model.AnnotationLayer;
import de.tudarmstadt.ukp.clarin.webanno.model.Project;
import de.tudarmstadt.ukp.clarin.webanno.model.TagSet;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

@Component
public class PartOfSpeechLayerInitializer
    implements LayerInitializer
{
    private final AnnotationSchemaService annotationSchemaService;

    @Autowired
    public PartOfSpeechLayerInitializer(AnnotationSchemaService aAnnotationSchemaService)
    {
        annotationSchemaService = aAnnotationSchemaService;
    }

    @Override
    public List<Class<? extends ProjectInitializer>> getDependencies()
    {
        // Because locks to token boundaries
        return asList(TokenLayerInitializer.class);
    }

    @Override
    public void configure(Project aProject) throws IOException
    {
        TagSet posTagSet = JsonImportUtil.importTagSetFromJson(aProject,
                new ClassPathResource("/tagsets/mul-pos-ud2.json").getInputStream(),
                annotationSchemaService);

        AnnotationLayer tokenLayer = annotationSchemaService.findLayer(aProject,
                Token.class.getName());

        AnnotationLayer posLayer = new AnnotationLayer(POS.class.getName(), "Part of speech",
                SPAN_TYPE, aProject, true, SINGLE_TOKEN, NO_OVERLAP);

        AnnotationFeature tokenPosFeature = new AnnotationFeature(aProject, tokenLayer, "pos",
                "pos", POS.class.getName());
        annotationSchemaService.createFeature(tokenPosFeature);

        posLayer.setAttachType(tokenLayer);
        posLayer.setAttachFeature(tokenPosFeature);
        annotationSchemaService.createLayer(posLayer);

        AnnotationFeature xpos = new AnnotationFeature(aProject, posLayer, "PosValue", "XPOS",
                CAS.TYPE_NAME_STRING, "XPOS", null);
        xpos.setDescription("Language-specific part-of-speech tag");
        annotationSchemaService.createFeature(xpos);

        AnnotationFeature upos = new AnnotationFeature(aProject, posLayer, "coarseValue", "UPOS",
                CAS.TYPE_NAME_STRING, "UPOS", posTagSet);
        upos.setDescription("Universal part-of-speech tag");
        annotationSchemaService.createFeature(upos);

    }
}
