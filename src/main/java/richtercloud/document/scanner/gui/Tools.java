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
package richtercloud.document.scanner.gui;

import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import richtercloud.reflection.form.builder.ClassInfo;

/**
 *
 * @author richter
 */
public class Tools {

    public static List<Class<?>> sortEntityClasses(Set<Class<?>> entityClasses) {
        List<Class<?>> entityClassesSort = new LinkedList<>(entityClasses);
        Collections.sort(entityClassesSort, new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                String o1Value;
                ClassInfo o1ClassInfo = o1.getAnnotation(ClassInfo.class);
                if(o1ClassInfo != null) {
                    o1Value = o1ClassInfo.name();
                }else {
                    o1Value = o1.getSimpleName();
                }
                String o2Value;
                ClassInfo o2ClassInfo = o2.getAnnotation(ClassInfo.class);
                if(o2ClassInfo != null) {
                    o2Value = o2ClassInfo.name();
                }else {
                    o2Value = o2.getSimpleName();
                }
                return o1Value.compareTo(o2Value);
            }
        });
        return entityClassesSort;
    }

    public static void disableRecursively(Container container,
            boolean enable) {
        Queue<Component> queue = new LinkedList<>(Arrays.asList(container.getComponents()));
        while(!queue.isEmpty()) {
            Component head = queue.poll();
            head.setEnabled(enable);
            if(head instanceof Container) {
                Container headCast = (Container) head;
                queue.addAll(Arrays.asList(headCast.getComponents()));
            }
        }
    }

    private Tools() {
    }
}
