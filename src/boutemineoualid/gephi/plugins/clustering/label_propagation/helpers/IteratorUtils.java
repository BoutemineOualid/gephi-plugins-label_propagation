/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package boutemineoualid.gephi.plugins.clustering.label_propagation.helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Oualid
 */
public class IteratorUtils {
    public static <T> List<T> toList(Iterator<T> iterator){
        List<T> result = new ArrayList<T>();
        while(iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;

    }
    
    public static <T> Set<T> toSet(Iterator<T> iterator){
        Set<T> result = new HashSet<T>();
        while(iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;

    }
}
