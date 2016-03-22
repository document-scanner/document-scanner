/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

