package com.performizeit.mjstack.model;

/**
 * Created by life on 6/5/14.
 */
public interface  ProfileNodeFilter {
    boolean accept(String stackFrame, int level,Object context);

}
