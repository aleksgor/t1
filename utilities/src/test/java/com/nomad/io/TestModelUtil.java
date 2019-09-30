package com.nomad.io;


import java.util.Arrays;
import java.util.Collection;
import java.util.Map;


import com.nomad.io.model.ChildModel;
import com.nomad.io.model.ChildModelId;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.utility.ModelUtil;
import static org.junit.Assert.*;


public class TestModelUtil {



    @org.junit.Test
    public void testMap() throws Exception {
        Collection<ChildModel> childs= Arrays.asList(getChildModel(1,"name1"),getChildModel(2,"name2"),getChildModel(3,"name3"),getChildModel(4,"name4")) ;
        Map<Identifier, Model> map=ModelUtil.convertToMap(childs);
        assertNotNull(map);
        assertEquals(4, map.size());
        assertNotNull(map.get(new ChildModelId(1)));
        assertNotNull(map.get(new ChildModelId(2)));

    }

    @org.junit.Test
    public void testid() throws Exception {
        Collection<ChildModel> childs= Arrays.asList(getChildModel(1,"name1"),getChildModel(2,"name2"),getChildModel(3,"name3"),getChildModel(4,"name4")) ;
        Collection<Identifier> map=ModelUtil.getIdentifiers(childs);
        assertNotNull(map);
        assertEquals(4, map.size());
        assertTrue(map.contains(new ChildModelId(1)));
        assertTrue(map.contains(new ChildModelId(2)));
        assertTrue(map.contains(new ChildModelId(3)));
        assertTrue(map.contains(new ChildModelId(4)));

    }

    private ChildModel getChildModel(long id, String name ){
        ChildModel child = new ChildModel();
        child.setId(id);
        child.setName(name);
        child.setIdentifier(new ChildModelId(id));
        return child;
    }

}
