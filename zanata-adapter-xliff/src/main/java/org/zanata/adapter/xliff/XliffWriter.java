package org.zanata.adapter.xliff;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.zanata.common.ContentState;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.extensions.gettext.TextFlowExtension;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.util.PathUtil;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

public class XliffWriter extends XliffCommon
{
   /**
    * Write document header with XML, xliff, file and body tag
    * @param writer
    * @param doc
    * @param targetLocale (use hyphen, not underscore)
    * @throws XMLStreamException
    */
   private static void writeHeader(IndentingXMLStreamWriter writer, Resource doc, String targetLocale) throws XMLStreamException
   {
      // XML tag
      writer.writeStartDocument("utf-8", "1.0");
      writer.writeComment("XLIFF document generated by Zanata. Visit http://zanata.org for more infomation.");
      writer.writeCharacters("\n");
      // XLiff tag
      writer.writeStartElement("xliff");
      writer.writeNamespace("", "urn:oasis:names:tc:xliff:document:1.1");
      writer.writeNamespace("xyz", "urn:appInfo:Items");
      writer.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
      writer.writeAttribute("xsi:schemaLocation", "urn:oasis:names:tc:xliff:document:1.1 http://www.oasis-open.org/committees/xliff/documents/xliff-core-1.1.xsd");
      writer.writeAttribute("version", "1.1");

      // file tag
      writer.writeStartElement(ELE_FILE);
      writer.writeAttribute(ATTRI_SOURCE_LANGUAGE, doc.getLang().getId());
      writer.writeAttribute(ATTRI_DATATYPE, "plaintext");
      writer.writeAttribute(ATTRI_ORIGINAL, "");
      if (targetLocale != null)
      {
         writer.writeAttribute(ATTRI_TARGET_LANGUAGE, targetLocale);
      }

      // body tag
      writer.writeStartElement(ELE_BODY);
   }

   private static void writeTransUnits(IndentingXMLStreamWriter writer, Resource doc, TranslationsResource targetDoc, boolean createSkeletons) throws XMLStreamException
   {
      Map<String, TextFlowTarget> targets = Collections.emptyMap();
      if (targetDoc != null)
      {
         targets = new HashMap<String, TextFlowTarget>();
         for (TextFlowTarget target : targetDoc.getTextFlowTargets())
         {
            targets.put(target.getResId(), target);
         }
      }
      for (TextFlow textFlow : doc.getTextFlows())
      {
         TextFlowTarget target = targets.get(textFlow.getId());
         if (target == null && !createSkeletons)
         {
            continue;
         }

         writer.writeStartElement(ELE_TRANS_UNIT);
         writer.writeAttribute(ATTRI_ID, textFlow.getId());
         writeTransUnitSource(writer, textFlow);
         if (target != null && target.getState() == ContentState.Approved)
         {
            writeTransUnitTarget(writer, target);
         }
         writeTransUnitContext(writer, textFlow);
         writer.writeEndElement();// end trans-unit tag
      }
   }

   private static void writeTransUnitSource(IndentingXMLStreamWriter writer, TextFlow textFlow) throws XMLStreamException
   {
      writer.writeStartElement(ELE_SOURCE);
      List<String> contents = textFlow.getContents();
      if (contents.size() != 1)
      {
         throw new RuntimeException("file format does not support plural forms: resId=" + textFlow.getId());
      }
      writer.writeCharacters(contents.get(0));
      writer.writeEndElement();// end source tag
   }

   private static void writeTransUnitTarget(IndentingXMLStreamWriter writer, TextFlowTarget target) throws XMLStreamException
   {
      writer.writeStartElement(ELE_TARGET);
      List<String> contents = target.getContents();
      if (contents.size() != 1)
      {
         throw new RuntimeException("file format does not support plural forms: resId=" + target.getResId());
      }
      writer.writeCharacters(contents.get(0));
      writer.writeEndElement();// end target tag
   }

   private static void writeTransUnitContext(IndentingXMLStreamWriter writer, TextFlow textFlow) throws XMLStreamException
   {
      if (!textFlow.getExtensions(true).isEmpty())
      {
         Map<String, ArrayList<String[]>> contextGroupMap = new HashMap<String, ArrayList<String[]>>();

         for (TextFlowExtension textFlowExtension : textFlow.getExtensions())
         {
            SimpleComment comment = (SimpleComment) textFlowExtension;
            String[] contextValues = comment.getValue().split(DELIMITER, 3);
            if (!contextGroupMap.containsKey(contextValues[0]))
            {
               ArrayList<String[]> list = new ArrayList<String[]>();
               list.add(new String[] { contextValues[1], contextValues[2] });
               contextGroupMap.put(contextValues[0], list);
            }
            else
            {
               ArrayList<String[]> list = contextGroupMap.get(contextValues[0]);
               list.add(new String[] { contextValues[1], contextValues[2] });
            }

         }

         for (String key : contextGroupMap.keySet())
         {
            ArrayList<String[]> values = contextGroupMap.get(key);

            writer.writeStartElement(ELE_CONTEXT_GROUP);
            writer.writeAttribute(ATTRI_NAME, key);

            for (String[] val : values)
            {
               writer.writeStartElement(ELE_CONTEXT);
               writer.writeAttribute(ATTRI_CONTEXT_TYPE, val[0]);
               writer.writeCharacters(val[1]);
               writer.writeEndElement();// end context
            }

            writer.writeEndElement();// end context-group
         }
      }
   }

   /**
    * Used for writing target file
    * 
    * @param baseDir
    * @param doc
    * @param locale (use hyphen, not underscore)
    * @param targetDoc may be null
    */
   public static void write(File baseDir, Resource doc, String locale, TranslationsResource targetDoc, boolean createSkeletons)
   {
      try
      {
         XMLOutputFactory output = XMLOutputFactory.newInstance();
         File outFile = new File(baseDir, doc.getName() + "_" + locale.replace('-', '_') + ".xml");
         PathUtil.makeParents(outFile);
         XMLStreamWriter xmlStreamWriter = output.createXMLStreamWriter(new FileOutputStream(outFile), "utf-8");
         IndentingXMLStreamWriter writer = new IndentingXMLStreamWriter(xmlStreamWriter);

         if (targetDoc != null)
            writeHeader(writer, doc, locale);
         else
            writeHeader(writer, doc, null);
         writeTransUnits(writer, doc, targetDoc, createSkeletons);

         writer.writeEndElement(); // end body tag
         writer.writeEndElement(); // end file tag
         writer.writeEndDocument(); // end Xliff tag
         writer.flush();
         writer.close();
      }
      catch (XMLStreamException e)
      {
         throw new RuntimeException("Error generating XLIFF file format   ", e);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Error writing XLIFF file  ", e);
      }
   }

   /**
    * Used for writing source file
    * 
    * @param baseDir
    * @param doc
    * @param locale (use hyphen, not underscore)
    */
   public static void write(File baseDir, Resource doc, String locale)
   {
      write(baseDir, doc, locale, null, true);
   }

}
