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
 *
 * @author richter
 */
@Entity
public class TestClass implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private Long id;
    private String a;

    protected TestClass() {
    }

    public TestClass(Long id, String a) {
        this.id = id;
        this.a = a;
    }

    public String getA() {
        return a;
    }

    public Long getId() {
        return id;
    }
}
