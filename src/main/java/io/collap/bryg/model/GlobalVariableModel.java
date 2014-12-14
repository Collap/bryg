package io.collap.bryg.model;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class GlobalVariableModel implements Model {

    public class GlobalVariable {

        private Class<?> javaType;
        private Object value;

        private GlobalVariable (Class<?> javaType, Object value) {
            this.javaType = javaType;
            this.value = value;
        }

        public Class<?> getJavaType () {
            return javaType;
        }

        public Object getValue () {
            return value;
        }

        public void setValue (Object value) {
            this.value = value;
        }

    }

    private Map<String, GlobalVariable> variables = new HashMap<> ();

    public void declareVariable (String name, Class<?> type, Object value) {
        variables.put (name, new GlobalVariable (type, value));
    }

    @Nullable
    public GlobalVariable getDeclaredVariable (String name) {
        return variables.get (name);
    }

    @Override
    public Object getVariable (String name) {
        GlobalVariable variable = getDeclaredVariable (name);
        if (variable == null) {
            return null;
        }

        return variable.getValue ();
    }

    @Override
    public void setVariable (String name, Object value) {
        GlobalVariable variable = variables.get (name);
        if (variable == null) {
            throw new RuntimeException ("The variable '" + name + "' in the GlobalVariableModel has to be declared before " +
                    "it can be set.");
        }

        variable.setValue (value);
    }

}
