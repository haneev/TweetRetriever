package tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapUtil
{
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map ) {
    	
        List<Map.Entry<K, V>> list = new ArrayList<Map.Entry<K, V>>( map.entrySet() );
        
        Collections.sort( list, new Comparator<Map.Entry<K, V>>() {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new HashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put( entry.getKey(), entry.getValue() );
        }
        
        return result;
    }
}