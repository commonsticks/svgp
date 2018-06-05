package com.keldee.svgp4.BasicSettings;

public abstract class Settings implements SettingsBase {
    protected static final String __EMPTY = "__SETTINGS_NOT_INITIALIZED_PARAMETERS";
    protected transient static int _id = 0;
    protected transient int id = _id;
    private boolean emptySettings = false;

    public Settings () { _id++; }

    public Settings (boolean empty) {
        _id++;
        if (empty)
            emptySettings = true;
    }

    public boolean isEmpty () {
        //it's alright, really
        return false;
    }

    protected void setNotEmpty () {
        emptySettings = false;
    }

    public <T> void importSettings (T settings) {
        resetFields(settings);
    }

    protected abstract <T> void resetFields (T a);

    public int getId() {
        return id;
    }
}