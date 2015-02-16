package org.xdty.smalltalk.model;

/**
 * Created by ty on 15-1-17.
 */
public class Error {
    
    public final static String PREFS_IS_NULL = "prefs is null, did you call \"ConfigWrapper.init()\" first?";
    public final static String PREFS_IS_INITIALIZED = "prefs have been initialized";
    public final static String PREFS_IS_EDITING = "prefs is editing";
    public final static String PREFS_IS_NOT_EDITING = "prefs is not editing, did you call \"startEditing()\" first?";
    
    public final static String BIND_SERVICE_TIMEOUT = "Bind service timeout!";
}
