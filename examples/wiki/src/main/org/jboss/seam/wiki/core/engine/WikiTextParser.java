package org.jboss.seam.wiki.core.engine;

import org.jboss.seam.text.SeamTextParser;
import org.jboss.seam.text.SeamTextLexer;
import org.jboss.seam.wiki.core.model.File;
import org.jboss.seam.wiki.core.model.Document;
import org.jboss.seam.wiki.core.model.Directory;
import org.jboss.seam.wiki.util.WikiUtil;
import org.jboss.seam.Component;
import antlr.ANTLRException;

import java.util.*;
import java.io.StringReader;

/**
 * Parses SeamText markup and also resolves link and macro tags as wiki links and wiki plugins.
 * <p>
 * Don't forget to set the resolver and renderer base with <tt>setCurrentDirectory()</tt> and
 * <tt>setCurrentDocument</tt>.
 * </p><p>
 * Picks the <tt>WikiLinkResolver</tt> present in the contextual variable <tt>wikiLinkResolver</tt>. Calls
 * out to a <tt>WikiTextRender</tt> for the actual in-document rendering of wiki links and wiki plugins. Might update
 * the <tt>currentDocument</tt>'s content, this change should be flushed to the datastore after calling
 * the parser.
 * </p><p>
 * After parsing, all links to attachments and all external links are pushed onto the renderer, where they
 * can be used to render an attachment list or appendixes to the text.
 *
 * @author Christian Bauer
 */
public class WikiTextParser extends SeamTextParser {

    private WikiTextRenderer renderer;

    private WikiLinkResolver resolver;
    private Directory currentDirectory;
    private Document currentDocument;

    private Map<String, WikiLink> resolvedLinks = new HashMap<String, WikiLink>();
    private List<WikiLink> attachments = new ArrayList<WikiLink>();
    private List<WikiLink> externalLinks = new ArrayList<WikiLink>();
    private Set<String> macroNames = new HashSet<String>();
    private boolean renderDuplicateMacros;
    private boolean resolveLinks;

    public WikiTextParser(String wikiText, boolean renderDuplicateMacros, boolean resolveLinks) {
        super(new SeamTextLexer(new StringReader(wikiText)));
        this.renderDuplicateMacros = renderDuplicateMacros;
        this.resolveLinks = resolveLinks;
    }
    /**
     * Mandatory, you need to set a renderer before starting the parer.
     *
     * @param renderer an implementation of WikiTextRenderer
     */
    public void setRenderer(WikiTextRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Mandatory, you need to set a resolver before starting the parer.
     *
     * @param resolver an implementation of WikiLinkresolver
     */
    public void setResolver(WikiLinkResolver resolver) {
        this.resolver = resolver;
    }

    /*
     * The render/link resolving base
     */
    public void setCurrentDirectory(Directory currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    /*
     * The render/link resolving base
     */
    public void setCurrentDocument(Document currentDocument) {
        this.currentDocument = currentDocument;
    }

    /**
     * Start parsing the wiki text and resolve wiki links and wiki plugins.
     * <p>
     * If <tt>updateResolvedLinks</tt> is enabled, the <t>currentDocument</tt>'s content will
     * be udpated after parsing the wiki text. This only occurs if we hit a link during link
     * resolution that needs to be updated. You should flush this modification to the data store.
     *
     * @param updateResolvedLinks Set updated content on <tt>currentDocument</tt>
     */
    public void parse(boolean updateResolvedLinks) {
        if (resolver == null) throw new IllegalStateException("WikiTextParser requires setResolver() call");
        if (renderer == null) throw new IllegalStateException("WikiTextParser requires setRenderer() call");
        if (currentDocument == null) throw new IllegalStateException("WikiTextParser requires setCurrentDocument() call");
        if (currentDirectory == null) throw new IllegalStateException("WikiTextParser requires setCurrentDirectory() call");

        try {
            startRule();

            if (updateResolvedLinks) {
                for (Map.Entry<String, WikiLink> entry: resolvedLinks.entrySet()) {
                    if(entry.getValue().isRequiresUpdating()) {
                        // One of the links we parsed and resolved requires updating of the current document, run
                        // the protocol converter - which is usally only called when storing a document.
                        currentDocument.setContent(
                            resolver.convertToWikiProtocol(currentDirectory.getAreaNumber(), currentDocument.getContent())
                        );
                        // Yes, this might happen during rendering, you should lush() and UPDATE the document!

                        break; // One is enough
                    }
                }
            }

            renderer.setAttachmentLinks(attachments);
            renderer.setExternalLinks(externalLinks);

        }
        catch (ANTLRException re) {
            // TODO: Do we ever get this exception?
            System.out.println("########################## FIXME: EXCEPTION IN PARSER ################################");
            throw new RuntimeException(re);
        }
    }

    protected String linkTag(String descriptionText, String linkText) {
        if (!resolveLinks) {
            // Don't resolve links, just call back to renderer for simple inline rendering of what we have
            WikiLink unresolvedLink = new WikiLink(false, false);
            unresolvedLink.setDescription(descriptionText);
            unresolvedLink.setUrl(linkText);
            return renderer.renderInlineLink(unresolvedLink);
        }

        resolver.resolveLinkText(currentDirectory.getAreaNumber(), resolvedLinks, linkText);
        WikiLink link = resolvedLinks.get((linkText));
        if (link == null) return "";

        // Override the description of the WikiLink with description found in tag
        String finalDescriptionText =
                (descriptionText!=null && descriptionText.length() > 0 ? descriptionText : link.getDescription());
        link.setDescription(finalDescriptionText);

        // Link to file (inline or attached)
        if (WikiUtil.isFile(link.getNode())) {
            File file = (File)link.getNode();

            if (file.getImageMetaInfo() == null || 'A' == file.getImageMetaInfo().getThumbnail()) {
                // It's an attachment
                if (!attachments.contains(link)) attachments.add(link);
                return renderer.renderFileAttachmentLink((attachments.indexOf(link)+1), link);
            } else {
                // It's an embedded thumbnail
                return renderer.renderThumbnailImageInlineLink(link);
            }
        }

        // External link
        if (link.isExternal()) {
            if (!externalLinks.contains(link)) externalLinks.add(link);
            return renderer.renderExternalLink(link);
        }

        // Regular link
        return renderer.renderInlineLink(link);
    }

    protected String macroInclude(String macroName) {
        // Filter out any dangerous characters
        String filteredName = macroName.replaceAll("[^\\p{Alnum}]+", "");
        if ( (macroNames.contains(filteredName) && renderDuplicateMacros) || !macroNames.contains(filteredName)) {
            macroNames.add(filteredName);
            return renderer.renderMacro(filteredName);
        } else {
            return "[Can't use the same macro twice!]";
        }
    }


}
