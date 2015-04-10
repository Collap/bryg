package io.collap.bryg.internal.module.standard;

import io.collap.bryg.Visibility;
import io.collap.bryg.module.GenericModule;

public class StandardModule extends GenericModule {

    public StandardModule() {
        super("standard", Visibility.global);
        setMember("discard", new DiscardFunction());
    }

}
