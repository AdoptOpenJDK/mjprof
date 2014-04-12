package com.performizeit.mjstack.parser;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by life on 8/4/14.
 */
public class Props {
    HashMap<String, Object> props;
    public Props(HashMap<String, Object> mtd) {
        props =mtd;
    }
    public Props() {
        props = new HashMap<String, Object>();
    }
    public HashMap<String, Object> cloneMetaData() {
        return (HashMap<String, Object>) props.clone();
    }
    public Object getVal(String key) {
        return props.get(key);
    }

    public Set<String> getProps() {
        return props.keySet();
    }
}
