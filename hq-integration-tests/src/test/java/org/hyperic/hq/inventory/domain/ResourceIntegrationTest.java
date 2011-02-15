package org.hyperic.hq.inventory.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hyperic.hq.common.ApplicationException;
import org.hyperic.hq.common.NotFoundException;
import org.hyperic.hq.inventory.InvalidRelationshipException;
import org.hyperic.hq.inventory.NotUniqueException;
import org.hyperic.hq.inventory.dao.ResourceDao;
import org.hyperic.hq.inventory.dao.ResourceTypeDao;
import org.hyperic.hq.reference.ConfigTypes;
import org.hyperic.hq.reference.RelationshipTypes;
import org.hyperic.hq.test.BaseInfrastructureTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.graph.core.Direction;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
public class ResourceIntegrationTest
    extends BaseInfrastructureTest {

    private Resource iceberg;

    private Resource produce;

    @Autowired
    private ResourceDao resourceDao;

    @Autowired
    private ResourceTypeDao resourceTypeDao;

    private Resource traderJoes;

    @Before
    public void initializeTestData() throws ApplicationException, NotFoundException {
        ResourceType store = new ResourceType("Grocery Store");
        resourceTypeDao.persist(store);
        ResourceType produceDept = new ResourceType("Produce Dept");
        resourceTypeDao.persist(produceDept);
        store.relateTo(produceDept, RelationshipTypes.CONTAINS);
        store.addPropertyType(new PropertyType("Address", String.class));
        ResourceType lettuce = new ResourceType("Lettuce");
        resourceTypeDao.persist(lettuce);
        produceDept.relateTo(lettuce, RelationshipTypes.CONTAINS);
        this.traderJoes = new Resource("Trader Joes", store);
        resourceDao.persist(traderJoes);
        this.produce = new Resource("Produce", produceDept);
        resourceDao.persist(produce);
        this.iceberg = new Resource("Iceberg", lettuce);
        resourceDao.persist(iceberg);
        traderJoes.relateTo(produce, RelationshipTypes.CONTAINS);
        produce.relateTo(iceberg, RelationshipTypes.CONTAINS);
    }

    @Test
    public void testGetChildren() {
        Set<Resource> children = traderJoes.getChildren(false);
        final Set<Resource> expected = new HashSet<Resource>();
        expected.add(produce);
        assertEquals(expected, children);
    }

    @Test
    public void testGetChildrenIds() {
        Set<Integer> children = traderJoes.getChildrenIds(false);
        final Set<Integer> expected = new HashSet<Integer>();
        expected.add(produce.getId());
        assertEquals(expected, children);
    }

    @Test
    public void testGetChildrenIdsRecursive() {
        Set<Integer> children = traderJoes.getChildrenIds(true);
        final Set<Integer> expected = new HashSet<Integer>();
        expected.add(produce.getId());
        expected.add(iceberg.getId());
        assertEquals(expected, children);
    }

    @Test
    public void testGetChildrenRecursive() {
        Set<Resource> children = traderJoes.getChildren(true);
        final Set<Resource> expected = new HashSet<Resource>();
        expected.add(produce);
        expected.add(iceberg);
        assertEquals(expected, children);
    }

    @Test
    public void testGetProperties() {
        traderJoes.setProperty("Address", "123 My Street");
        Map<String, Object> expected = new HashMap<String, Object>(1);
        expected.put("Address", "123 My Street");
        assertEquals(expected, traderJoes.getProperties());
    }

    @Test
    public void testGetPropertiesNoneSet() {
        assertTrue(traderJoes.getProperties().isEmpty());
    }

    @Test
    public void testGetProperty() {
        traderJoes.setProperty("Address", "123 My Street");
        assertEquals("123 My Street", traderJoes.getProperty("Address"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPropertyNotDefined() {
        traderJoes.getProperty("Hours");
    }

    @Test
    public void testGetPropertyNotSet() {
        assertNull(traderJoes.getProperty("Address"));
    }

    @Test
    public void testGetRelationships() {
        assertEquals(2, produce.getRelationships().size());
    }

    @Test
    public void testGetRelationshipsBoth() {
        produce.relateTo(traderJoes, RelationshipTypes.CONTAINS);
        Set<ResourceRelationship> relationships = produce.getRelationships(traderJoes,
            RelationshipTypes.CONTAINS, Direction.BOTH);
        assertEquals(2, relationships.size());
    }

    @Test
    public void testGetRelationshipsFrom() {
        Set<ResourceRelationship> relationships = produce
            .getRelationshipsFrom(RelationshipTypes.CONTAINS);
        assertEquals(1, relationships.size());
        ResourceRelationship relationship = relationships.iterator().next();
        assertEquals(iceberg, relationship.getTo());
        assertEquals(produce, relationship.getFrom());
    }

    @Test
    public void testGetRelationshipsIncoming() {
        Set<ResourceRelationship> relationships = produce.getRelationships(traderJoes,
            RelationshipTypes.CONTAINS, Direction.INCOMING);
        assertEquals(1, relationships.size());
        ResourceRelationship relationship = relationships.iterator().next();
        assertEquals(traderJoes, relationship.getFrom());
        assertEquals(produce, relationship.getTo());
    }

    @Test
    public void testGetRelationshipsOutgoing() {
        Set<ResourceRelationship> relationships = traderJoes.getRelationships(produce,
            RelationshipTypes.CONTAINS, Direction.OUTGOING);
        assertEquals(1, relationships.size());
        ResourceRelationship relationship = relationships.iterator().next();
        assertEquals(traderJoes, relationship.getFrom());
        assertEquals(produce, relationship.getTo());
    }

    @Test
    public void testGetRelationshipsTo() {
        Set<ResourceRelationship> relationships = produce
            .getRelationshipsTo(RelationshipTypes.CONTAINS);
        assertEquals(1, relationships.size());
        ResourceRelationship relationship = relationships.iterator().next();
        assertEquals(traderJoes, relationship.getFrom());
        assertEquals(produce, relationship.getTo());
    }

    @Test
    public void testGetRelationshipTo() {
        ResourceRelationship relationship = produce.getRelationshipTo(traderJoes,
            RelationshipTypes.CONTAINS);
        assertEquals(traderJoes, relationship.getFrom());
        assertEquals(produce, relationship.getTo());
    }

    @Test
    public void testGetResourceFrom() {
        assertEquals(iceberg, produce.getResourceFrom(RelationshipTypes.CONTAINS));
    }

    @Test(expected = NotUniqueException.class)
    public void testGetResourceFromMultiple() {
        traderJoes.relateTo(iceberg, RelationshipTypes.CONTAINS);
        traderJoes.getResourceFrom(RelationshipTypes.CONTAINS);
    }

    @Test
    public void testGetResourceFromNotRelated() {
        assertNull(iceberg.getResourceFrom(RelationshipTypes.CONTAINS));
    }

    @Test
    public void testGetResourcesFrom() {
        Set<Resource> expected = new HashSet<Resource>();
        expected.add(iceberg);
        assertEquals(expected, produce.getResourcesFrom(RelationshipTypes.CONTAINS));
    }

    @Test
    public void testGetResourcesTo() {
        Set<Resource> expected = new HashSet<Resource>();
        expected.add(traderJoes);
        assertEquals(expected, produce.getResourcesTo(RelationshipTypes.CONTAINS));
    }

    @Test
    public void testGetResourceTo() {
        assertEquals(traderJoes, produce.getResourceTo(RelationshipTypes.CONTAINS));
    }

    @Test(expected = NotUniqueException.class)
    public void testGetResourceToMultiple() {
        traderJoes.relateTo(iceberg, RelationshipTypes.CONTAINS);
        iceberg.getResourceTo(RelationshipTypes.CONTAINS);
    }

    @Test
    public void testGetResourceToNotRelated() {
        assertNull(traderJoes.getResourceTo(RelationshipTypes.CONTAINS));
    }

    @Test
    public void testHasChildDoesnt() {
        assertFalse(produce.hasChild(traderJoes, true));
    }

    @Test
    public void testHasChildNotRecursive() {
        assertTrue(traderJoes.hasChild(produce, false));
        assertFalse(traderJoes.hasChild(iceberg, false));
    }

    @Test
    public void testHasChildRecursive() {
        assertTrue(traderJoes.hasChild(produce, true));
        assertTrue(traderJoes.hasChild(iceberg, true));
    }

    @Test
    public void testIsOwner() {
        traderJoes.setOwner(authzSubjectManager.getOverlordPojo());
        assertTrue(traderJoes.isOwner(authzSubjectManager.getOverlordPojo().getId()));
    }

    @Test
    public void testIsOwnerNoOwner() {
        assertFalse(traderJoes.isOwner(authzSubjectManager.getOverlordPojo().getId()));
    }

    @Test
    public void testIsOwnerNotOwner() {
        traderJoes.setOwner(authzSubjectManager.getOverlordPojo());
        assertFalse(traderJoes.isOwner(967));
    }

    @Test
    public void testIsRelatedToIncoming() {
        assertFalse(produce.isRelatedTo(traderJoes, RelationshipTypes.CONTAINS));
    }

    @Test
    public void testIsRelatedToOutgoing() {
        assertTrue(traderJoes.isRelatedTo(produce, RelationshipTypes.CONTAINS));
    }

    @Test
    public void testRelateTo() {
        produce.getType().relateTo(traderJoes.getType(), "ManagedBy");
        ResourceRelationship relationship = produce.relateTo(traderJoes, "ManagedBy");
        assertEquals(traderJoes, relationship.getTo());
        assertEquals(produce, relationship.getFrom());
    }
    
    @Test(expected = InvalidRelationshipException.class)
    public void testRelateToInvalidRelationship() {
        produce.relateTo(traderJoes, "ManagedBy");
    }

    @Test
    public void testRemove() {
        traderJoes.remove();
        //verify relationship removed
        assertEquals(1,produce.getRelationships().size());
        assertNull(resourceDao.findById(traderJoes.getId()));
    }

    @Test
    public void testRemoveProperties() {
        traderJoes.setProperty("Address", "123 My Street");
        traderJoes.removeProperties();
        assertTrue(traderJoes.getProperties().isEmpty());
    }
    
    @Test
    public void testRemoveRelationships() {
        produce.removeRelationships();
        assertTrue(produce.getRelationships().isEmpty());
    }
    
    @Test
    public void testRemoveRelationshipsBothDirections() {
        produce.relateTo(traderJoes, RelationshipTypes.CONTAINS);
        produce.removeRelationships(traderJoes, RelationshipTypes.CONTAINS);
        assertFalse(produce.isRelatedTo(traderJoes, RelationshipTypes.CONTAINS));
        assertFalse(traderJoes.isRelatedTo(produce, RelationshipTypes.CONTAINS));
    }
    
    @Test
    public void testRemoveRelationshipsByName() {
        traderJoes.getType().relateTo(produce.getType(), "Manages");
        traderJoes.relateTo(produce, "Manages");
        produce.removeRelationships("Manages");
        assertTrue(traderJoes.isRelatedTo(produce, RelationshipTypes.CONTAINS));
        assertFalse(traderJoes.isRelatedTo(produce, "Manages"));
    }
    
    @Test
    public void testRemoveRelationshipsIncoming() {
        produce.relateTo(traderJoes, RelationshipTypes.CONTAINS);
        produce.removeRelationships(traderJoes, RelationshipTypes.CONTAINS, Direction.INCOMING);
        assertTrue(produce.isRelatedTo(traderJoes, RelationshipTypes.CONTAINS));
        assertFalse(traderJoes.isRelatedTo(produce, RelationshipTypes.CONTAINS));
    }
    
    @Test
    public void testRemoveRelationshipsOutgoing() {
        produce.relateTo(traderJoes, RelationshipTypes.CONTAINS);
        produce.removeRelationships(traderJoes, RelationshipTypes.CONTAINS, Direction.OUTGOING);
        assertFalse(produce.isRelatedTo(traderJoes, RelationshipTypes.CONTAINS));
        assertTrue(traderJoes.isRelatedTo(produce, RelationshipTypes.CONTAINS));
    }
    
    @Test
    public void testSetProperty() {
        traderJoes.setProperty("Address", "123 My Street");
        assertEquals("123 My Street",traderJoes.getProperty("Address"));
        Object oldValue = traderJoes.setProperty("Address", "123 Some Other Street");
        assertEquals("123 My Street",oldValue);
        assertEquals("123 Some Other Street",traderJoes.getProperty("Address"));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSetPropertyNotDefined() {
        traderJoes.setProperty("Hours", "9-5");
    }
    
    @Test
    public void testSetPropertyToNull() {
        traderJoes.setProperty("Address", "123 My Street");
        assertEquals("123 My Street",traderJoes.getProperty("Address"));
        Object oldValue = traderJoes.setProperty("Address", null);
        assertEquals("123 My Street",oldValue);
        assertNull(traderJoes.getProperty("Address"));
    }
    
    @Test
    public void testSetPropertyToNullFirstTime() {
        traderJoes.setProperty("Address",null);
        assertNull(traderJoes.getProperty("Address"));
    }
    
    @Test
    public void testSetAndGetConfig() {
        Config product = new Config();
        product.setValue("user", "bob");
        traderJoes.setConfig(ConfigTypes.PRODUCT,product);
        Config config = traderJoes.getConfig(ConfigTypes.PRODUCT);
        assertEquals("bob",config.getValue("user"));
    }
    
    @Test
    public void testSetConfigNotYetPersisted() {
        Config product = new Config();
        traderJoes.setConfig(ConfigTypes.PRODUCT,product);
        assertEquals(product,traderJoes.getConfig(ConfigTypes.PRODUCT));
    }
    
    @Test
    public void testGetConfigNoConfig() {
        assertNull(traderJoes.getConfig(ConfigTypes.PRODUCT));
    }
}
