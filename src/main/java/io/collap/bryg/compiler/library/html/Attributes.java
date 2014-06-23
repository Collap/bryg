package io.collap.bryg.compiler.library.html;

public class Attributes {

    // TODO: Some of these arguments do not make sense in every attribute, according to the spec.
    // See: http://www.w3.org/TR/html5/dom.html#global-attributes
    // The validator might be extended with warnings that are thrown when one uses a supposedly valid
    // tag on an element where it makes no sense.

    /**
     * Notes:
     *  - The data-* attributes are also global attributes, but already accounted for in the attribute compiler.
     *  - The xmlns "attribute" is not supported in this version of bryg!
     */
    public static final String[] validGlobalAttributes = new String[] {
        "accesskey", "class", "contenteditable", "dir", "draggable",
        "dropzone", "hidden", "id", "lang",
        "onabort", "onblur", "oncancel", "oncanplay", "oncanplaythrough",
        "onchange", "onclick", "oncuechange", "ondblclick", "ondrag",
        "ondragend", "ondragenter", "ondragexit", "ondragleave", "ondragover",
        "ondragstart", "ondrop", "ondurationchange", "onemptied", "onended",
        "onerror", "onfocus", "oninput", "oninvalid", "onkeydown", "onkeypress",
        "onkeyup", "onload", "onloadeddata", "onloadedmetadata", "onloadstart",
        "onmousedown", "onmouseenter", "onmouseleave", "onmousemove",
        "onmouseout", "onmouseover", "onmouseup", "onmousewheel",
        "onpause", "onplay", "onplaying", "onprogress", "onratechange",
        "onreset", "onresize", "onscroll", "onseeked", "onseeking",
        "onselect", "onshow", "onstalled", "onsubmit", "onsuspend",
        "ontimeupdate", "ontoggle", "onvolumechange", "onwaiting",
        "spellcheck", "style", "tabindex", "title", "translate"
    };

}
