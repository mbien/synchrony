/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.synchrony.examples.mina.sumup.message;


/**
 * <code>ADD</code> message in SumUp protocol.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class AddMessage extends AbstractMessage {
    private static final long serialVersionUID = -940833727168119141L;

    private int value;

    public AddMessage() {
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        // it is a good practice to create toString() method on message classes.
        return getSequence() + ":ADD(" + value + ')';
    }
}



