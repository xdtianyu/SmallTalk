package org.xdty.smalltalk.wrapper;

import android.content.Context;
import android.content.SharedPreferences;

import org.xdty.smalltalk.model.Config;
import org.xdty.smalltalk.model.Error;

import java.util.HashMap;

/**
 * Created by ty on 15-1-17.
 */
public class ConfigWrapper {
    
    private static ConfigWrapper mInstance;
    
    private static SharedPreferences prefs;
    
    private SharedPreferences.Editor editor;
    
    private final static String PREFS_NAME = "config";
    
    private final static HashMap<String, String> STRING_PREFS = new HashMap<String, String>() {
        {
            put(Config.USERNAME, Config.USERNAME_DEFAULT);
            put(Config.PASSWORD, Config.PASSWORD_DEFAULT);
            put(Config.SERVER_URI, Config.SERVER_URI_DEFAULT);
        }
    };
    
    private static void throwPrefsError(String error) {
        throw new RuntimeException(error);
    }
    
    private SharedPreferences getPrefs() {
        if (prefs==null) {
            throwPrefsError(Error.PREFS_IS_NULL);
        }
        return prefs;
    }
    
    public static void init(Context context) {
        if (prefs!=null) {
            throwPrefsError(Error.PREFS_IS_INITIALIZED);
        }
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public ConfigWrapper Instance() {

        if (prefs==null) {
            throwPrefsError(Error.PREFS_IS_NULL);
        }
        
        if (mInstance==null) {
            mInstance = new ConfigWrapper();
        }
        return mInstance;
    }
    
    public String getString(String key) {
        String result = "";

        if (prefs.contains(key)) {
            result = prefs.getString(key, STRING_PREFS.get(key));
        } else if (STRING_PREFS.containsKey(key)){
            result = STRING_PREFS.get(key);
        }
        return result;
    }
    
    public void startEditing() {
        if (editor==null) {
            editor = prefs.edit();
        } else {
            throwPrefsError(Error.PREFS_IS_EDITING);
        }
    }
    
    public synchronized void setString(String key, String value) {
        if (editor!=null) {
            editor.putString(key, value);
        } else {
            throwPrefsError(Error.PREFS_IS_NOT_EDITING);
        }
    }
    
    public void endEditing() {
        if (editor!=null) {
            editor.commit();
            editor = null;
        } else {
            throwPrefsError(Error.PREFS_IS_NOT_EDITING);
        }
    }
    
}
