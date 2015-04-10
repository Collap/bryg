package io.collap.bryg.module;

import io.collap.bryg.Visibility;
import io.collap.bryg.internal.module.html.FunctionCollection;

import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * The following HTML5 elements and attributes are valid according to this spec: http://www.w3.org/TR/html5
 */
public class HtmlModule implements Module {

    private Visibility visibility;
    private FunctionCollection functionCollection;

    public HtmlModule(Visibility visibility) {
        this.visibility = visibility;
        functionCollection = FunctionCollection.getInstance();
    }

    @Override
    public String getName() {
        return "html";
    }

    @Override
    public Visibility getVisibility() {
        return visibility;
    }

    @Override
    public @Nullable Member getMember(String name) {
        return functionCollection.getFunction(name);
    }

    @Override
    public Iterator<? extends Member<?>> getMemberIterator() {
        return functionCollection.getIterator();
    }

}
