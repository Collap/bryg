package io.collap.bryg.internal.module.html;

import io.collap.bryg.module.MemberFunction;
import io.collap.bryg.module.Member;

import javax.annotation.Nullable;
import java.util.*;

public class FunctionCollection {

    private static @Nullable FunctionCollection instance = null;

    public synchronized static FunctionCollection getInstance() {
        if (instance == null) {
            instance = new FunctionCollection();
        }
        return instance;
    }

    private Map<String, MemberFunction> functionMap = new HashMap<>();

    public FunctionCollection() {
        initialize();
    }

    /**
     * This method is thread-safe, since the map is only read.
     */
    public MemberFunction getFunction(String name) {
        return functionMap.get(name);
    }

    public Iterator<? extends Member<?>> getIterator() {
        return Collections.unmodifiableCollection(functionMap.values()).iterator();
    }

    private void initialize() {
        /* Document root. */
        addContentTag("html", new String[]{
                "manifest"
        });

        /* Document metadata. */
        addContentTag("head");
        addContentTag("title");
        addTag("base", new String[]{
                "href", "target"
        });
        addTag("link", new String[]{
                "crossorigin", "href", "hreflang", "media", "rel", "sizes", "type"
        });
        addTag("meta", new String[]{
                "charset", "content", "http-equiv", "name"
        });
        addContentTag("style", new String[]{
                "media", "type"
        });

        /* Sections. */
        addContentTag("body", new String[]{
                "onafterprint", "onbeforeprint", "onbeforeunload", "onhashchange",
                "onmessage", "onoffline", "ononline", "onpagehide", "onpageshow",
                "onpopstate", "onstorage", "onunload"
        });
        addContentTag("article");
        addContentTag("section");
        addContentTag("nav");
        addContentTag("aside");
        for (int i = 1; i <= 6; ++i) { // h1 to h6
            String name = "h" + i;
            addContentTag(name);
        }
        addContentTag("header");
        addContentTag("footer");
        addContentTag("address");

        /* Grouping content. */
        addContentTag("p");
        addTag("hr");
        addContentTag("pre");
        addContentTag("blockquote", new String[]{
                "cite"
        });
        addContentTag("ol", new String[]{
                "reversed", "start", "type"
        });
        addContentTag("ul");
        addContentTag("li", new String[]{
                "value"
        });
        addContentTag("dl");
        addContentTag("dt");
        addContentTag("dd");
        addContentTag("figure");
        addContentTag("figcaption");
        addContentTag("div");
        addContentTag("main");

        /* Text-level semantics. */
        addContentTag("a", new String[]{
                "download", "href", "hreflang", "rel", "target", "type"
        });
        addContentTag("em");
        addContentTag("strong");
        addContentTag("small");
        addContentTag("s");
        addContentTag("cite");
        addContentTag("q", new String[]{
                "cite"
        });
        addContentTag("dfn");
        addContentTag("abbr");
        addContentTag("data", new String[]{
                "value"
        });
        addContentTag("time", new String[]{
                "datetime"
        });
        addContentTag("var");
        addContentTag("samp");
        addContentTag("kbd");
        addContentTag("sub");
        addContentTag("sup");
        addContentTag("i");
        addContentTag("b");
        addContentTag("u");
        addContentTag("mark");
        addContentTag("ruby");
        addContentTag("rb");
        addContentTag("rt");
        addContentTag("rtc");
        addContentTag("rp");
        addContentTag("bdi");
        addContentTag("bdo");
        addContentTag("span");
        addTag("br");
        addTag("wbr");

        /* Edits. */
        addContentTag("ins", new String[]{
                "cite", "datetime"
        });
        addContentTag("del", new String[]{
                "cite", "datetime"
        });

        /* Embedded content. */
        addTag("img", new String[]{
                "alt", "crossorigin", "height", "ismap", "src", "usemap", "width"
        });
        addContentTag("iframe", new String[]{
                "height", "name", "sandbox", "src", "srcdoc", "width"
        });
        addTag("embed", new String[]{
                "height", "src", "type", "width"
        });
        addContentTag("object", new String[]{
                "data", "form", "height", "name", "type", "typemustmatch", "usemap", "width"
        });
        addTag("param", new String[]{
                "name", "value"
        });
        addContentTag("video", new String[]{
                "autoplay", "controls", "crossorigin", "height", "loop", "mediagroup",
                "muted", "poster", "preload", "src", "width"
        });
        addContentTag("audio", new String[]{
                "autoplay", "controls", "crossorigin", "loop", "mediagroup", "muted",
                "preload", "src"
        });
        addTag("source", new String[]{
                "media", "src", "type"
        });
        addTag("track", new String[]{
                "default", "kind", "label", "src", "srclang"
        });
        addContentTag("map", new String[]{
                "name"
        });
        addTag("area", new String[]{
                "alt", "coords", "download", "href", "hreflang",
                "rel", "shape", "target", "type"
        });

        /* Tabular data. */
        addContentTag("table", new String[]{
                "border"
        });
        addContentTag("caption");
        addContentTag("colgroup", new String[]{
                "span"
        });
        addTag("col", new String[]{
                "span"
        });
        addContentTag("tbody");
        addContentTag("thead");
        addContentTag("tfoot");
        addContentTag("tr");
        addContentTag("td", new String[]{
                "colspan", "headers", "rowspan"
        });
        addContentTag("th", new String[]{
                "abbr", "colspan", "headers", "rowspan", "scope"
        });

        /* Forms. */
        addContentTag("form", new String[]{
                "accept-charset", "action", "autocomplete", "enctype",
                "method", "name", "novalidate", "target"
        });
        addContentTag("label", new String[]{
                "for", "form"
        });
        addTag("input", new String[]{
                "accept", "alt", "autocomplete", "autofocus", "checked",
                "dirname", "disabled", "form", "formaction", "formenctype",
                "formmethod", "formnovalidate", "formtarget", "height",
                "list", "max", "maxlength", "min", "minlength", "multiple",
                "name", "pattern", "placeholder", "readonly", "required",
                "size", "src", "step", "type", "value", "width"
        });
        addContentTag("button", new String[]{
                "autofocus", "disabled", "form", "formaction", "formenctype",
                "formmethod", "formnovalidate", "formtarget", "name", "type",
                "value"
        });
        addContentTag("select", new String[]{
                "autofocus", "disabled", "form", "multiple", "name",
                "required", "size"
        });
        addContentTag("datalist");
        addContentTag("optgroup", new String[]{
                "disabled", "label"
        });
        addContentTag("option", new String[]{
                "disabled", "label", "selected", "value"
        });
        addContentTag("textarea", new String[]{
                "autocomplete", "autofocus", "cols", "dirname", "disabled",
                "form", "maxlength", "minlength", "name", "placeholder",
                "readonly", "required", "rows", "wrap"
        });
        addTag("keygen", new String[]{
                "autofocus", "challenge", "disabled", "form",
                "keytype", "name"
        });
        addContentTag("output", new String[]{
                "for", "form", "name"
        });
        addContentTag("progress", new String[]{
                "max", "value"
        });
        addContentTag("meter", new String[]{
                "high", "low", "max", "min", "optimum", "value"
        });
        addContentTag("fieldset", new String[]{
                "disabled", "form", "name"
        });
        addContentTag("legend");

        /* Scripting. */
        addContentTag("script", new String[]{
                "async", "charset", "crossorigin", "defer", "src", "type"
        });
        addContentTag("noscript");
        addContentTag("template");
        addContentTag("canvas", new String[]{
                "height", "width"
        });
    }

    private void addContentTag(String name) {
        addContentTag(name, null);
    }

    private void addContentTag(String name, @Nullable String[] validAttributes) {
        MemberFunction function;
        if (validAttributes != null) {
            Arrays.sort(validAttributes); // Make sure that the arrays are sorted!
            function = new HtmlFunction(name, validAttributes, true);
        } else {
            function = new HtmlFunction(name, new String[]{}, true);
        }
        functionMap.put(name, function);
    }

    private void addTag(String name) {
        addTag(name, null);
    }

    private void addTag(String name, @Nullable String[] validAttributes) {
        MemberFunction function;
        if (validAttributes != null) {
            Arrays.sort(validAttributes); // Make sure that the arrays are sorted!
            function = new HtmlFunction(name, validAttributes, false);
        } else {
            function = new HtmlFunction(name, new String[]{}, false);
        }
        functionMap.put(name, function);
    }

}
