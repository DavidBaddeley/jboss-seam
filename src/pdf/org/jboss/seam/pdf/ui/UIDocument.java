package org.jboss.seam.pdf.ui;

import org.jboss.seam.core.Manager;
import org.jboss.seam.core.Pages;
import org.jboss.seam.pdf.DocumentData;
import org.jboss.seam.pdf.ITextUtils;
import org.jboss.seam.pdf.DocumentStore;
import org.jboss.seam.pdf.DocumentData.DocType;

import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.context.*;
import java.io.*;

import com.lowagie.text.*;
import com.lowagie.text.html.HtmlWriter;
import com.lowagie.text.pdf.*;
import com.lowagie.text.rtf.RtfWriter2;

public class UIDocument 
extends ITextComponent
{
    public static final String COMPONENT_TYPE   = "org.jboss.seam.pdf.ui.UIDocument";

    DocWriter writer;
    Document document;
    ByteArrayOutputStream stream;
    String id;
    String baseName;

    DocType docType;

    String type;
    String title;
    String subject;
    String keywords;
    String author;
    String creator;
    String orientation; 

    String pageSize;
    String margins;
    Boolean marginMirroring;

    boolean sendRedirect = true;


    UISignature signatureField;



    public void setType(String type) {
        this.type = type;
    }

    public void setMargins(String margins) {
        this.margins = margins;
    }

    public void setPageSize(String pageSize) {
        this.pageSize = pageSize;
    }

    public void setMarginMirroring(Boolean marginMirroring) {
        this.marginMirroring = marginMirroring;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }


    public void setSendRedirect(boolean sendRedirect) {
        this.sendRedirect = sendRedirect;
    }

    public boolean getSendRedirect() {
        return sendRedirect;
    }


    @Override
    public Object getITextObject() {
        return document;
    }

    @Override
    public void createITextObject(FacesContext context) {
        type = (String) valueBinding(context, "type", type);        
        docType = docTypeForName(type);

        document = new Document();
        // most of this needs to be done BEFORE document.open();

        pageSize = (String) valueBinding(context, "pageSize", pageSize);
        if (pageSize != null) {
            document.setPageSize(ITextUtils.pageSizeValue(pageSize));
        }

        orientation = (String) valueBinding(context, "orientation", orientation);    
        if (orientation != null) {
            if (orientation.equalsIgnoreCase("portrait")) {
                // do nothing
            } else if (orientation.equalsIgnoreCase("landscape")) {
                Rectangle currentSize = document.getPageSize();
                document.setPageSize(new Rectangle(currentSize.height(),
                        currentSize.width()));

            } else {
                throw new RuntimeException("orientation value " + orientation + "unknown");
            }
        }

        margins = (String) valueBinding(context, "margins", margins);
        if (margins != null) {
            float[] vals = ITextUtils.stringToFloatArray(margins);
            if (vals.length != 4) {
                throw new RuntimeException("margins must contain 4 float values");
            }

            document.setMargins(vals[0], vals[1], vals[2], vals[3]);           
        }

        marginMirroring = (Boolean) valueBinding(context, "marginMirroring", marginMirroring);
        if (marginMirroring != null) {
            document.setMarginMirroring(marginMirroring);
        }        
    }

    private void initMetaData(FacesContext context) {
        title = (String) valueBinding(context, "title", title);
        if (title != null) {
            document.addTitle(title);
        }

        subject = (String) valueBinding(context, "subject", subject);
        if (subject != null) {
            document.addSubject(subject);
        }

        keywords = (String) valueBinding(context, "keywords", keywords);
        if (keywords != null) {
            document.addKeywords(keywords);
        }

        author = (String) valueBinding(context, "author", author);
        if (author != null) {
            document.addAuthor(author);
        }

        creator = (String) valueBinding(context, "creator", creator);
        if (creator != null) {
            document.addCreator(creator);
        }
    }

    @Override
    public void removeITextObject() {
        document = null;
    }

    @Override
    public void handleAdd(Object o) {
        if (o instanceof Element) {
            try {
                document.add((Element) o);
            } catch (DocumentException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalArgumentException("cannot add " + o);
        }
    }

    public void addSignature(UISignature signatureField) {
        this.signatureField = signatureField;
    }

    @Override
    public void encodeBegin(FacesContext context) 
    throws IOException
    {
        super.encodeBegin(context);

        stream = new ByteArrayOutputStream();

        try {
            switch (docType) {
            case PDF:
                writer = PdfWriter.getInstance(document, stream);
                break;
            case RTF:
                writer = RtfWriter2.getInstance(document, stream);
                break;
            case HTML:
                writer = HtmlWriter.getInstance(document, stream);
                break;
            }

            initMetaData(context);

            processHeaders();

            document.open();
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

        if (sendRedirect) {
            DocumentStore store = DocumentStore.instance();
            id = store.newId();

            ResponseWriter response = context.getResponseWriter();
            response.write("<!DOCTYPE composition PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\" >");
            response.startElement("html", this);
            response.writeAttribute("xmlns", "http://www.w3.org/1999/xhtml", null);
            response.startElement("head", this);
            response.startElement("meta", this);
            response.writeAttribute("http-equiv", "Refresh", null);

            String viewId = Pages.getViewId(context);
            baseName = baseNameForViewId(viewId); 
            String url = store.preferredUrlForContent(baseName, docType, id);

            url = Manager.instance().encodeConversationId(url, viewId);

            response.writeAttribute("content", "0; URL=" + url, null);

            response.endElement("meta");
            response.endElement("head");

            response.startElement("body",this);
        }
    }

    private void processHeaders() {
        Object facet = getFacet("header");      

        if (facet == null) {
            return;
        }

        if (facet instanceof UIComponent) {
            try {
                encode(FacesContext.getCurrentInstance(), (UIComponent) facet);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } 
        }


    }



    private String baseNameForViewId(String viewId) {
        int pos = viewId.lastIndexOf("/");
        if (pos != -1) {
            viewId = viewId.substring(pos+1);
        }

        pos = viewId.lastIndexOf(".");
        if (pos!=-1) {
            viewId = viewId.substring(0,pos);
        }

        return viewId;
    }

    @Override
    public void encodeEnd(FacesContext context) 
    throws IOException
    {
        document.close();

        byte[] bytes = stream.toByteArray();

        if (signatureField != null) 
        {
            bytes = signatureField.sign(bytes);
        }

        DocumentData documentData = new DocumentData(baseName, docType, bytes);

        if (sendRedirect) {
            DocumentStore.instance().saveData(id,documentData);

            ResponseWriter response = context.getResponseWriter();
            response.endElement("body");
            response.endElement("html");

            removeITextObject();

            Manager.instance().beforeRedirect();
        } else {
            UIComponent parent = getParent();

            if (parent instanceof ValueHolder) {
                ValueHolder holder = (ValueHolder) parent;
                holder.setValue(documentData);
            }

        }
    }



    public DocWriter getWriter() {
        return writer;
    }    

    private DocType docTypeForName(String typeName) {    
        if (typeName != null) {
            if (typeName.equalsIgnoreCase(DocType.PDF.name())) {
                return DocType.PDF;
            } else if (typeName.equalsIgnoreCase(DocType.RTF.name())) {
                return DocType.RTF;
            } else if (typeName.equalsIgnoreCase(DocType.HTML.name())) {
                return DocType.HTML;
            }
        }
        return DocType.PDF;
    }
}
