package io.collap.bryg.module;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class GenericModule implements Module {

    protected String name;
    protected Visibility visibility;
    private Map<String, Member> members = new HashMap<>();

    public GenericModule(String name, Visibility visibility) {
        this.name = name;
        this.visibility = visibility;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Visibility getVisibility() {
        return visibility;
    }

    @Override
    public @Nullable Member getMember(String name) {
        return members.get(name);
    }

    public void setMember(String name, Member member) {
        members.put(name, member);
    }

}
