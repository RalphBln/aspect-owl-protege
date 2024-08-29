/*
 * This file is part of the OWL API.
 *
 * The contents of this file are subject to the LGPL License, Version 3.0.
 *
 * Copyright (C) 2011, The University of Queensland
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see http://www.gnu.org/licenses/.
 *
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License,
 * Version 2.0 in which case, the provisions of the Apache License Version 2.0 are applicable
 * instead of those above.
 *
 * Copyright 2011, The University of Queensland
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package xyz.aspectowl.rdf.renderer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.RioSetting;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.rio.helpers.JSONLDSettings;
import org.eclipse.rdf4j.rio.helpers.NTriplesWriterSettings;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.semanticweb.owlapi.formats.RioRDFDocumentFormat;
import org.semanticweb.owlapi.formats.RioRDFDocumentFormatFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLDocumentFormatFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLStorer;
import org.semanticweb.owlapi.util.AbstractOWLStorer;

import com.google.common.base.Optional;
import xyz.aspectowl.owlapi.model.OWLAspectManager;
import xyz.aspectowl.renderer.AspectOWLStorer;

/**
 * An implementation of {@link OWLStorer} that writes statements to Sesame {@link RDFHandler}s,
 * including {@link RDFWriter} implementations based on the given
 * {@link RioRDFDocumentFormatFactory}.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 * @since 4.0.0
 */
public class AspectOWLTrigStorer extends AbstractOWLStorer implements AspectOWLStorer {
  
  private transient RDFHandler rioHandler;
  private final OWLDocumentFormatFactory ontFormat;
//  private final Resource[] contexts;
  
  private OWLAspectManager aspectManager;
  
  /**
   * @param ontologyFormat format
   * @param aspectManager the aspect manager
   */
  public AspectOWLTrigStorer(OWLDocumentFormatFactory ontologyFormat, OWLAspectManager aspectManager /*, Resource... contexts */) {
//    OpenRDFUtil.verifyContextNotNull(contexts);
    ontFormat = ontologyFormat;
    this.aspectManager = aspectManager;
//    this.contexts = contexts;
  }
  
  @Override
  public OWLAspectManager getAspectManager() {
    return aspectManager;
  }
  
  @Override
  public boolean canStoreOntology(OWLDocumentFormat ontologyFormat) {
    return ontFormat.createFormat().equals(ontologyFormat);
  }
  
  /**
   * If the {@link RDFFormat} is null, then it is acceptable to return an in memory
   * {@link StatementCollector}. This method will only be called from {@code storeOntology} if
   * {@link #setRioHandler(RDFHandler)} is not called with a non-null argument.
   *
   * @param format The {@link RDFFormat} for the resulting {@link RDFHandler}, if the writer
   *        parameter is not null.
   * @param writer The {@link Writer} for the resulting RDFHandler, or null to create an in-memory
   *        collection.
   * @param baseIRI base IRI
   * @return An implementation of the {@link RDFHandler} interface, based on the parameters given
   *         to this method.
   * @throws OWLOntologyStorageException If the format does not have an {@link RDFWriter}
   *         implementation available on the classpath.
   */
  protected RDFHandler getRDFHandlerForWriter(@Nullable RDFFormat format, Writer writer,
                                              @Nullable String baseIRI) throws OWLOntologyStorageException {
    // by default return a StatementCollector if they did not specify a
    // format
    if (format == null) {
      return new StatementCollector();
    } else {
      try {
        return getWriter(format, writer, baseIRI);
      } catch (final UnsupportedRDFormatException | URISyntaxException e) {
        throw new OWLOntologyStorageException(e);
      }
    }
  }
  
  protected RDFWriter getWriter(RDFFormat format, Writer writer, @Nullable String baseIRI)
          throws URISyntaxException {
    if (baseIRI == null || format.equals(RDFFormat.RDFXML)) {
      // do not set a base iri for RDFXML, it causes the output IRIs to be relativised and the
      // parser code does not handle resolution properly.
      return Rio.createWriter(format, writer);
    }
    return Rio.createWriter(format, writer, baseIRI);
  }
  
  /**
   * If the {@link RDFFormat} is null, then it is acceptable to return an in memory
   * {@link StatementCollector}. This method will only be called from {@code storeOntology} if
   * {@link #setRioHandler(RDFHandler)} is not called with a non-null argument.
   *
   * @param format The {@link RDFFormat} for the resulting {@link RDFHandler}, if the writer
   *        parameter is not null.
   * @param outputStream The {@link java.io.OutputStream} for the resulting RDFHandler, or null to
   *        create an in-memory collection.
   * @return An implementation of the {@link RDFHandler} interface, based on the parameters given
   *         to this method.
   * @throws OWLOntologyStorageException If the format does not have an {@link RDFWriter}
   *         implementation available on the classpath.
   */
  protected static RDFHandler getRDFHandlerForOutputStream(final RDFFormat format,
                                                           final OutputStream outputStream) throws OWLOntologyStorageException {
    // by default return a StatementCollector if they did not specify a
    // format
    if (format == null) {
      return new StatementCollector();
    } else {
      try {
        return Rio.createWriter(format, outputStream);
      } catch (final UnsupportedRDFormatException e) {
        throw new OWLOntologyStorageException(e);
      }
    }
  }
  
  /**
   * @return the RDF handler
   */
  public RDFHandler getRioHandler() {
    return rioHandler;
  }
  
  /**
   * @param rioHandler the RDF handler to set
   */
  public void setRioHandler(final RDFHandler rioHandler) {
    this.rioHandler = rioHandler;
  }
  
  @Override
  protected void storeOntology(@Nonnull OWLOntology ontology, Writer writer,
                               OWLDocumentFormat format) throws OWLOntologyStorageException {
    // This check is performed to allow any Rio RDFHandler to be used to
    // render the output, even if it does not render to a writer. For
    // example, it could store the triples in memory without serialising
    // them to any particular format.
    if (rioHandler == null) {
      if (!(format instanceof RioRDFDocumentFormat)) {
        throw new OWLOntologyStorageException(
                "Unable to use RioOntologyStorer to store this format as it is not recognised as a RioRDFOntologyFormat: "
                        + format);
      }
      final RioRDFDocumentFormat rioFormat = (RioRDFDocumentFormat) format;
      if (format.isTextual()) {
        rioHandler = getRDFHandlerForWriter(rioFormat.getRioFormat(), writer,
                format.isPrefixOWLOntologyFormat()
                        ? format.asPrefixOWLOntologyFormat().getDefaultPrefix()
                        : null);
      } else {
        throw new OWLOntologyStorageException(
                "Unable to use storeOntology with a Writer as the desired format is not textual. Format was "
                        + format);
      }
    }
    try {
      // if this is a writer rather than a statement collector, set its config from the format
      // parameters, if any
      addSettingsIfPresent(format);
      final AspectOWLTrigRenderer ren =
              new AspectOWLTrigRenderer(ontology, rioHandler, format /*, contexts(ontology, format)*/);
      ren.render();
    } catch (final IOException e) {
      throw new OWLOntologyStorageException(e);
    }
  }
  
  @Override
  protected void storeOntology(@Nonnull OWLOntology ontology, OutputStream outputStream,
                               OWLDocumentFormat format) throws OWLOntologyStorageException {
    // This check is performed to allow any Rio RDFHandler to be used to
    // render the output, even if it does not render to a writer. For
    // example, it could store the triples in memory without serialising
    // them to any particular format.
    if (rioHandler == null) {
      if (!(format instanceof RioRDFDocumentFormat)) {
        throw new OWLOntologyStorageException(
                "Unable to use RioOntologyStorer to store this format as it is not recognised as a RioRDFOntologyFormat: "
                        + format);
      }
      final RioRDFDocumentFormat rioFormat = (RioRDFDocumentFormat) format;
      if (format.isTextual()) {
        try {
          Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, UTF_8));
          rioHandler = getRDFHandlerForWriter(rioFormat.getRioFormat(), writer,
                  format.isPrefixOWLOntologyFormat()
                          ? format.asPrefixOWLOntologyFormat().getDefaultPrefix()
                          : null);
        } catch (IOException e) {
          throw new OWLOntologyStorageException(e);
        }
      } else {
        rioHandler = getRDFHandlerForOutputStream(rioFormat.getRioFormat(), outputStream);
      }
    }
    try {
      // if this is a writer rather than a statement collector, set its config from the format
      // parameters, if any
      addSettingsIfPresent(format);
      final AspectOWLTrigRenderer ren =
              new AspectOWLTrigRenderer(ontology, rioHandler, format/*, contexts(ontology, format)*/);
      ren.render();
    } catch (final IOException e) {
      throw new OWLOntologyStorageException(e);
    }
  }
  
//  private Resource[] contexts(OWLOntology o, OWLDocumentFormat d) {
//    boolean shouldUseOntologyIRI =
//            o.getOWLOntologyManager().getOntologyLoaderConfiguration().shouldOutputNamedGraphIRI();
//    String namedGraph = null;
//    if (shouldUseOntologyIRI) {
//      // Only use the ontology IRI if the configuration option OUTPUT_NAMED_GRAPH_IRI is set
//      // to true.
//      // If the configuration option is false, only use the value of namedGraphOverride.
//      Optional<IRI> ontologyIRI = o.getOntologyID().getOntologyIRI();
//      if (ontologyIRI.isPresent()) {
//        namedGraph = ontologyIRI.get().toString();
//      }
//    }
//    Object namedGraphOverride = d.getParameter("namedGraphOverride", namedGraph);
//    if (namedGraphOverride != null) {
//      Resource context =
//              SimpleValueFactory.getInstance().createIRI(namedGraphOverride.toString());
//      if (contexts.length == 0) {
//        return new Resource[] {context};
//      }
//      Resource[] toReturn = new Resource[contexts.length + 1];
//      System.arraycopy(contexts, 0, toReturn, 0, contexts.length);
//      toReturn[contexts.length] = context;
//      return toReturn;
//    }
//    return contexts;
//  }
  
  // These warnings are suppressed because the types cannot be easily determined here without
  // forcing constraints that might need to be updated when Rio introduces new settings.
  @SuppressWarnings({"rawtypes", "null", "unchecked"})
  protected void addSettingsIfPresent(OWLDocumentFormat format) {
    if (rioHandler instanceof RDFWriter) {
      RDFWriter w = (RDFWriter) rioHandler;
      Collection<RioSetting<?>> supportedSettings = knownSettings(w);
      for (RioSetting r : supportedSettings) {
        Serializable v = format.getParameter(r, null);
        if (v != null) {
          w.getWriterConfig().set(r, v);
        }
      }
    }
  }
  
  protected Collection<RioSetting<?>> knownSettings(RDFWriter w) {
    try {
      return w.getSupportedSettings();
    } catch (UnsupportedOperationException e) {
      LOGGER.debug(
              "Bug in RIO means this exception is thrown in some formats where an unmodifiable class is modified.  As a workaround, OWLAPI will try all the known settings - relying on the caller to only use supported settings.",
              e);
      return Arrays.asList(JSONLDSettings.COMPACT_ARRAYS, JSONLDSettings.HIERARCHICAL_VIEW,
              JSONLDSettings.JSONLD_MODE, JSONLDSettings.OPTIMIZE,
              JSONLDSettings.USE_NATIVE_TYPES, JSONLDSettings.USE_RDF_TYPE,
              BasicWriterSettings.BASE_DIRECTIVE, BasicWriterSettings.INLINE_BLANK_NODES,
              BasicWriterSettings.PRETTY_PRINT,
              BasicWriterSettings.RDF_LANGSTRING_TO_LANG_LITERAL,
              BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL,
              NTriplesWriterSettings.ESCAPE_UNICODE);
      // parsing only settings, not used here
      // NTriplesParserSettings
      // RDFJSONParserSettings
      // XMLParserSettings
      // TriXParserSettings
      // TurtleParserSettings
    }
  }
}
