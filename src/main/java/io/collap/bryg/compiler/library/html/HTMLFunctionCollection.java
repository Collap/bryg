package io.collap.bryg.compiler.library.html;

import io.collap.bryg.compiler.library.Function;
import io.collap.bryg.compiler.library.Library;

import java.util.Arrays;

/**
 * The following HTML5 elements and attributes are valid according to this spec:
 *  http://www.w3.org/TR/html5
 */
public class HTMLFunctionCollection {

    private Library library;

    public HTMLFunctionCollection (Library library) {
        this.library = library;
    }

    public void register () {
        /* Document root. */
        addBlockFunction ("html", new String[] {
            "manifest"
        });

        /* Document metadata. */
        addBlockFunction ("head");
        addBlockFunction ("title");
        addInlineFunction ("base", new String[] {
            "href", "target"
        });
        addInlineFunction ("link", new String[] {
            "crossorigin", "href", "hreflang", "media", "rel", "sizes", "type"
        });
        addInlineFunction ("meta", new String[] {
            "charset", "content", "http-equiv", "name"
        });
        addBlockFunction ("style", new String[] {
            "media", "type"
        });

        /* Sections. */
        addBlockFunction ("body", new String[] {
            "onafterprint", "onbeforeprint", "onbeforeunload", "onhashchange",
            "onmessage", "onoffline", "ononline", "onpagehide", "onpageshow",
            "onpopstate", "onstorage", "onunload"
        });
        addBlockFunction ("article");
        addBlockFunction ("section");
        addBlockFunction ("nav");
        addBlockFunction ("aside");
        for (int i = 1; i <= 6; ++i) { // h1 to h6
            String name = "h" + i;
            addBlockFunction (name);
        }
        addBlockFunction ("header");
        addBlockFunction ("footer");
        addBlockFunction ("address");

        /* Grouping content. */
        addBlockFunction ("p");
        addInlineFunction ("hr");
        addBlockFunction ("pre");
        addBlockFunction ("blockquote", new String[] {
            "cite"
        });
        addBlockFunction ("ol", new String[] {
            "reversed", "start", "type"
        });
        addBlockFunction ("ul");
        addBlockFunction ("li", new String[] {
            "value"
        });
        addBlockFunction ("dl");
        addBlockFunction ("dt");
        addBlockFunction ("dd");
        addBlockFunction ("figure");
        addBlockFunction ("figcaption");
        addBlockFunction ("div");
        addBlockFunction ("main");

        /* Text-level semantics. */
        addBlockFunction ("a", new String[] {
            "download", "href", "hreflang", "rel", "target", "type"
        });
        addBlockFunction ("em");
        addBlockFunction ("strong");
        addBlockFunction ("small");
        addBlockFunction ("s");
        addBlockFunction ("cite");
        addBlockFunction ("q", new String[] {
            "cite"
        });
        addBlockFunction ("dfn");
        addBlockFunction ("abbr");
        addBlockFunction ("data", new String[] {
            "value"
        });
        addBlockFunction ("time", new String[] {
            "datetime"
        });
        addBlockFunction ("var");
        addBlockFunction ("samp");
        addBlockFunction ("kbd");
        addBlockFunction ("sub");
        addBlockFunction ("sup");
        addBlockFunction ("i");
        addBlockFunction ("b");
        addBlockFunction ("u");
        addBlockFunction ("mark");
        addBlockFunction ("ruby");
        addBlockFunction ("rb");
        addBlockFunction ("rt");
        addBlockFunction ("rtc");
        addBlockFunction ("rp");
        addBlockFunction ("bdi");
        addBlockFunction ("bdo");
        addBlockFunction ("span");
        addInlineFunction ("br");
        addInlineFunction ("wbr");

        /* Edits. */
        addBlockFunction ("ins", new String[] {
            "cite", "datetime"
        });
        addBlockFunction ("del", new String[] {
            "cite", "datetime"
        });

        /* Embedded content. */
        addInlineFunction ("img", new String[] {
            "alt", "crossorigin", "height", "ismap", "src", "usemap", "width"
        });
        addBlockFunction ("iframe", new String[] {
            "height", "name", "sandbox", "src", "srcdoc", "width"
        });
        addInlineFunction ("embed", new String[] {
            "height", "src", "type", "width"
        });
        addBlockFunction ("object", new String[] {
            "data", "form", "height", "name", "type", "typemustmatch", "usemap", "width"
        });
        addInlineFunction ("param", new String[] {
            "name", "value"
        });
        addBlockFunction ("video", new String[] {
            "autoplay", "controls", "crossorigin", "height", "loop", "mediagroup",
            "muted", "poster", "preload", "src", "width"
        });
        addBlockFunction ("audio", new String[] {
            "autoplay", "controls", "crossorigin", "loop", "mediagroup", "muted",
            "preload", "src"
        });
        addInlineFunction ("source", new String[] {
            "media", "src", "type"
        });
        addInlineFunction ("track", new String[] {
            "default", "kind", "label", "src", "srclang"
        });
        addBlockFunction ("map", new String[] {
            "name"
        });
        addInlineFunction ("area", new String[] {
            "alt", "coords", "download", "href", "hreflang",
            "rel", "shape", "target", "type"
        });

        /* Tabular data. */
        addBlockFunction ("table", new String[] {
            "border"
        });
        addBlockFunction ("caption");
        addBlockFunction ("colgroup", new String[] {
            "span"
        });
        addInlineFunction ("col", new String[] {
            "span"
        });
        addBlockFunction ("tbody");
        addBlockFunction ("thead");
        addBlockFunction ("tfoot");
        addBlockFunction ("tr");
        addBlockFunction ("td", new String[] {
            "colspan", "headers", "rowspan"
        });
        addBlockFunction ("th", new String[] {
            "abbr", "colspan", "headers", "rowspan", "scope"
        });

        /* Forms. */
        addBlockFunction ("form", new String[] {
            "accept-charset", "action", "autocomplete", "enctype",
            "method", "name", "novalidate", "target"
        });
        addBlockFunction ("label", new String[] {
            "for", "form"
        });
        addInlineFunction ("input", new String[] {
            "accept", "alt", "autocomplete", "autofocus", "checked",
            "dirname", "disabled", "form", "formaction", "formenctype",
            "formmethod", "formnovalidate", "formtarget", "height",
            "list", "max", "maxlength", "min", "minlength", "multiple",
            "name", "pattern", "placeholder", "readonly", "required",
            "size", "src", "step", "type", "value", "width"
        });
        addBlockFunction ("button", new String[] {
            "autofocus", "disabled", "form", "formaction", "formenctype",
            "formmethod", "formnovalidate", "formtarget", "name", "type",
            "value"
        });
        addBlockFunction ("select", new String[] {
            "autofocus", "disabled", "form", "multiple", "name",
            "required", "size"
        });
        addBlockFunction ("datalist");
        addBlockFunction ("optgroup", new String[] {
            "disabled", "label"
        });
        addBlockFunction ("option", new String[] {
            "disabled", "label", "selected", "value"
        });
        addBlockFunction ("textarea", new String[] {
            "autocomplete", "autofocus", "cols", "dirname", "disabled",
            "form", "maxlength", "minlength", "name", "placeholder",
            "readonly", "required", "rows", "wrap"
        });
        addInlineFunction ("keygen", new String[] {
            "autofocus", "challenge", "disabled", "form",
            "keytype", "name"
        });
        addBlockFunction ("output", new String[] {
            "for", "form", "name"
        });
        addBlockFunction ("progress", new String[] {
            "max", "value"
        });
        addBlockFunction ("meter", new String[] {
            "high", "low", "max", "min", "optimum", "value"
        });
        addBlockFunction ("fieldset", new String[] {
            "disabled", "form", "name"
        });
        addBlockFunction ("legend");

        /* Scripting. */
        addBlockFunction ("script", new String[] {
            "async", "charset", "crossorigin", "defer", "src", "type"
        });
        addBlockFunction ("noscript");
        addBlockFunction ("template");
        addBlockFunction ("canvas", new String[] {
            "height", "width"
        });
    }

    private void addBlockFunction (String name) {
        addBlockFunction (name, null);
    }

    private void addBlockFunction (String name, String[] validAttributes) {
        Function function;
        if (validAttributes != null) {
            Arrays.sort (validAttributes); /* Note: Make sure that the arrays are sorted! */
            function = new HTMLBlockFunction (name, validAttributes);
        }else {
            function = new HTMLBlockFunction (name);
        }
        library.setFunction (name, function);
    }

    private void addInlineFunction (String name) {
        addInlineFunction (name, null);
    }

    private void addInlineFunction (String name, String[] validAttributes) {
        Function function;
        if (validAttributes != null) {
            Arrays.sort (validAttributes); /* Note: Make sure that the arrays are sorted! */
            function = new HTMLInlineFunction (name, validAttributes);
        }else {
            function = new HTMLInlineFunction (name);
        }
        library.setFunction (name, function);
    }

}
