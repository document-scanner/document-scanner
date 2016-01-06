/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package richtercloud.document.scanner.gui.conf.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Represents a change of {@link TestClass} with the name of {@code a} changed
 * to {@code b}. And {@code c} added.
 *
 * @author richter
 */
@Entity
public class TestClassUpdate implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private Long id;
    private String b;
    private int c;

    protected TestClassUpdate() {
    }

    public TestClassUpdate(Long id, String b, int c) {
        this.id = id;
        this.b = b;
        this.c = c;
    }

    public int getC() {
        return c;
    }

    public String getB() {
        return b;
    }

    public Long getId() {
        return id;
    }
}

