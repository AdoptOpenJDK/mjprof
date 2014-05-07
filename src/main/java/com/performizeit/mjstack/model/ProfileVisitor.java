package com.performizeit.mjstack.model;

/**
 * Created by life on 6/5/14.
 */
public interface ProfileVisitor {
    String visit(String stackframe,int level);
}
