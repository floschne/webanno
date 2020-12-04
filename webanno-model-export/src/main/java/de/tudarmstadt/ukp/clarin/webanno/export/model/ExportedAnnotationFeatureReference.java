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
package de.tudarmstadt.ukp.clarin.webanno.export.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.tudarmstadt.ukp.clarin.webanno.model.AnnotationFeature;

/**
 * A by-name reference to a feature.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExportedAnnotationFeatureReference
{
    @JsonProperty("name")
    private String name;

    @JsonProperty("layer")
    private String layer;

    public ExportedAnnotationFeatureReference()
    {
        // Needed for deserialization
    }

    public ExportedAnnotationFeatureReference(AnnotationFeature aFeature)
    {
        super();
        name = aFeature.getName();
        layer = aFeature.getLayer().getName();
    }

    public ExportedAnnotationFeatureReference(String aLayerName, String aFeatureName)
    {
        super();
        name = aFeatureName;
        layer = aLayerName;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String aName)
    {
        name = aName;
    }

    public String getLayer()
    {
        return layer;
    }

    public void setLayer(String aLayer)
    {
        layer = aLayer;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((layer == null) ? 0 : layer.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ExportedAnnotationFeatureReference other = (ExportedAnnotationFeatureReference) obj;
        if (layer == null) {
            if (other.layer != null) {
                return false;
            }
        }
        else if (!layer.equals(other.layer)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
