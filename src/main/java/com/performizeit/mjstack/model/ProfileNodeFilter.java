package com.performizeit.mjstack.model;

/**
 * Created by life on 6/5/14.
 */
public interface  ProfileNodeFilter {
    boolean accept(SFNode node, int level,Object context);

}
