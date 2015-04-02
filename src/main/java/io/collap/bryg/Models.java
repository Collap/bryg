package io.collap.bryg;

public class Models {

    private static final Model EMPTY_MODEL = new EmptyModel();

    /**
     * A model that has no variables and can't save any variables.
     */
    private static class EmptyModel implements Model {

        /**
         * This particular implementation does <b>not</b> throw an exception,
         * because the fragment that is called may only consist of
         * @return Always null.
         */
        @Override
        public Object getVariable(String name) {
            return null;
        }

        @Override
        public void setVariable(String name, Object value) {
            throw new RuntimeException("The empty model must not be assigned a variable.");
        }

    }

    /**
     * <p>Returns an empty and <b>immutable</b> model.</p>
     *
     * <p>Use it to pass a model to a fragment which does not expect any arguments
     *    without creating a new object and cluttering the garbage collector.</p>
     */
    public static Model empty() {
        return EMPTY_MODEL;
    }

}
