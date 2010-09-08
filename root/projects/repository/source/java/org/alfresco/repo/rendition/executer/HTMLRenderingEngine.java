/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.rendition.executer;

import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.rendition.RenditionLocation;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.xml.sax.ContentHandler;

/**
 * This class provides a way to turn documents supported by the
 *  {@link ContentService} standard transformers into basic, clean
 *  HTML.
 * <P/>
 * The HTML that is produced probably isn't going to be suitable
 *  for direct web publishing, as it's likely going to be too
 *  basic. Instead, it should be simple and clean HTML, suitable
 *  for being the basis of some web-friendly HTML once edited
 *  / further transformed. 
 * 
 * @author Nick Burch
 * @since 3.4
 */
public class HTMLRenderingEngine extends AbstractRenderingEngine
{
    private static Log logger = LogFactory.getLog(HTMLRenderingEngine.class);

    /*
     * Action constants
     */
    public static final String NAME = "htmlRenderingEngine";
    
    protected static final QName PRIMARY_IMAGE = QName.createQName(
          "http://www.alfresco.org/model/website/1.0", "primaryImage");
    protected static final QName SECONDARY_IMAGE = QName.createQName(
          "http://www.alfresco.org/model/website/1.0", "secondaryImage");

    private DictionaryService dictionaryService;
    
    public void setDictionaryService(DictionaryService dictionaryService) {
       this.dictionaryService = dictionaryService;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.rendition.executer.AbstractRenderingEngine#render(org.alfresco.repo.rendition.executer.AbstractRenderingEngine.RenderingContext)
     */
    @Override
    protected void render(RenderingContext context)
    {
        ContentReader contentReader = context.makeContentReader();
        String sourceMimeType = contentReader.getMimetype();
        String targetMimeType = "text/html";
        
        // Check that Tika supports it
        AutoDetectParser p = new AutoDetectParser();
        MediaType sourceMediaType = MediaType.parse(sourceMimeType);
        if(! p.getParsers().containsKey(sourceMediaType))
        {
           throw new RenditionServiceException(
                 "Source mime type of " + sourceMimeType + 
                 " is not supported by Tika for HTML conversions"
           );
        }
        
        // Make the HTML Version using Tika
        generateHTML(p, context);
        
        // Extract out any images
        // TODO
        boolean hasImages = true; // TODO
        if(hasImages)
        {
           Map<QName,Serializable> properties = new HashMap<QName,Serializable>();
           NodeRef imgFolder = null;
           
           // Extract into it
           boolean donePrimary = false;
           for(String fakeContent : new String[] {"Test1","Test2"})
           {
              if(imgFolder == null)
                 imgFolder = createImagesDirectory(context);
              
              // Create the node if needed
              NodeRef img = nodeService.getChildByName(
                    imgFolder, ContentModel.ASSOC_CONTAINS, fakeContent
              );
              if(img == null)
              {
                 properties.clear();
                 properties.put(ContentModel.PROP_NAME, fakeContent); 
                 img = nodeService.createNode(
                       imgFolder,
                       ContentModel.ASSOC_CONTAINS,
                       QName.createQName(fakeContent),
                       ContentModel.TYPE_CONTENT,
                       properties
                 ).getChildRef();
              }
              
              // If we can, associate it with the rendered HTML, so
              //  that they're properly linked
              QName assocType = SECONDARY_IMAGE;
              if(!donePrimary)
              {
                 assocType = PRIMARY_IMAGE;
                 donePrimary = true;
              }
              if(dictionaryService.getAssociation(assocType) != null)
              {
                 nodeService.createAssociation(
                       context.getDestinationNode(), img, assocType
                 );
              }
              
              // Put the image into the node
              ContentWriter writer = contentService.getWriter(
                    img, ContentModel.PROP_CONTENT, true
              );
              writer.putContent(fakeContent);
           }
        }
    }
    
    /**
     * Creates a directory to store the images in.
     * The directory will be a sibling of the rendered
     *  HTML, and named similar to it.
     */
    private NodeRef createImagesDirectory(RenderingContext context)
    {
       // It should be a sibling of the HTML in it's eventual location
       //  (not it's current temporary one!)
       RenditionLocation location = resolveRenditionLocation(
             context.getSourceNode(), context.getDefinition(), context.getDestinationNode()
       );
       NodeRef parent = location.getParentRef();
       
       // Figure out what to call it, based on the HTML node
       String folderName = nodeService.getProperty( 
             context.getSourceNode(),
             ContentModel.PROP_NAME
       ).toString();
       if(folderName.lastIndexOf('.') > -1)
       {
          folderName = folderName.substring(0, folderName.lastIndexOf('.'));
       }
       folderName = folderName + "_files";
       
       // It is already there?
       // (eg from when the rendition is being re-run)
       NodeRef imgFolder = nodeService.getChildByName(
             parent, ContentModel.ASSOC_CONTAINS, folderName
       );
       if(imgFolder != null)
          return imgFolder;
       
       // Create the directory
       Map<QName,Serializable> properties = new HashMap<QName,Serializable>();
       properties.put(ContentModel.PROP_NAME, folderName);
       imgFolder = nodeService.createNode(
             parent,
             ContentModel.ASSOC_CONTAINS,
             QName.createQName(folderName),
             ContentModel.TYPE_FOLDER,
             properties
       ).getChildRef();
       
       return imgFolder;
    }
    
    /**
     * Builds a Tika-compatible SAX content handler, which will
     *  be used to generate+capture the XHTML
     */
    private ContentHandler buildContentHandler(Writer output) 
    {
       SAXTransformerFactory factory = (SAXTransformerFactory)
                SAXTransformerFactory.newInstance();
       TransformerHandler handler;
       
       try {
          handler = factory.newTransformerHandler();
       } catch (TransformerConfigurationException e) {
          throw new RenditionServiceException("SAX Processing isn't available - " + e);
       }
       
       handler.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
       handler.setResult(new StreamResult(output));
       handler.getTransformer().setOutputProperty(OutputKeys.METHOD, "xml");
       
       return handler;
    }
    
    /**
     * Asks Tika to translate the contents into HTML
     */
    private void generateHTML(Parser p, RenderingContext context)
    {
       // Setup things to parse with
       Metadata metadata = new Metadata();
       ParseContext parseContext = new ParseContext();
       StringWriter sw = new StringWriter();
       ContentHandler handler = buildContentHandler(sw);
       
       // Parse
       try {
          p.parse(
                context.makeContentReader().getContentInputStream(),
                handler, metadata, parseContext
          );
       } catch(Exception e) {
          throw new RenditionServiceException("Tika HTML Conversion Failed", e);
       }
       
       // Save it
       ContentWriter contentWriter = context.makeContentWriter();
       contentWriter.putContent( sw.toString() );
    }
}