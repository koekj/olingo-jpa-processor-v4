package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.Tuple;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;
import com.sap.olingo.jpa.processor.core.util.TupleDouble;

public class TestJPAExpandQueryResult extends TestBase {
  private JPAExpandQueryResult cut;
  private UriInfoResource uriInfo;
  private TopOption top;
  private SkipOption skip;
  private ExpandOption expand;

  private JPAODataRequestContextAccess requestContext;

  private TestHelper helper;
  private HashMap<String, List<Tuple>> queryResult = new HashMap<>(1);
  private List<Tuple> tuples = new ArrayList<>();
  private JPAEntityType et;

  @BeforeEach
  public void setup() throws ODataException {
    helper = new TestHelper(emf, PUNIT_NAME);
    et = helper.getJPAEntityType("Organizations");
    uriInfo = mock(UriInfoResource.class);
    requestContext = mock(JPAODataRequestContextAccess.class);
    top = mock(TopOption.class);
    skip = mock(SkipOption.class);
    expand = mock(ExpandOption.class);
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    queryResult.put("root", tuples);
  }

  @Test
  public void checkGetKeyBoundaryEmptyBoundaryNoTopOrSkip() throws ODataJPAModelException, ODataJPAQueryException {

    cut = new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType("Organizations"),
        Collections.emptyList());
    final Optional<JPAKeyPair> act = cut.getKeyBoundary(requestContext);
    assertFalse(act.isPresent());
  }

  @Test
  public void checkGetKeyBoundaryEmptyBoundaryNoExpand() throws ODataJPAModelException, ODataJPAQueryException {

    final Map<String, Object> key = new HashMap<>(1);
    final TupleDouble tuple = new TupleDouble(key);
    tuples.add(tuple);
    key.put("ID", Integer.valueOf(10));
    cut = new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType("Organizations"),
        Collections.emptyList());
    when(uriInfo.getTopOption()).thenReturn(top);
    when(top.getValue()).thenReturn(2);
    final Optional<JPAKeyPair> act = cut.getKeyBoundary(requestContext);
    assertFalse(act.isPresent());
  }

  @Test
  public void checkGetKeyBoundaryEmptyBoundaryNotComparable() throws ODataJPAModelException,
      NumberFormatException, ODataApplicationException {

    cut = new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType("AdministrativeDivisionDescriptions"),
        Collections.emptyList());
    final Optional<JPAKeyPair> act = cut.getKeyBoundary(requestContext);
    assertFalse(act.isPresent());
  }

  @Test
  public void checkGetKeyBoundaryOneResultWithTop() throws ODataJPAModelException, ODataJPAQueryException {

    final Map<String, Object> key = new HashMap<>(1);
    final TupleDouble tuple = new TupleDouble(key);
    tuples.add(tuple);
    key.put("ID", Integer.valueOf(10));
    cut = new JPAExpandQueryResult(queryResult, null, et, Collections.emptyList());
    when(uriInfo.getTopOption()).thenReturn(top);
    when(uriInfo.getExpandOption()).thenReturn(expand);
    when(top.getValue()).thenReturn(2);
    final Optional<JPAKeyPair> act = cut.getKeyBoundary(requestContext);
    assertTrue(act.isPresent());
    assertEquals(10, act.get().getMin().get(et.getKey().get(0)));
  }

  @Test
  public void checkGetKeyBoundaryOneResultWithSkip() throws ODataJPAModelException, ODataJPAQueryException {

    addTuple(Integer.valueOf(12));
    cut = new JPAExpandQueryResult(queryResult, null, et, Collections.emptyList());
    when(uriInfo.getSkipOption()).thenReturn(skip);
    when(uriInfo.getExpandOption()).thenReturn(expand);
    when(skip.getValue()).thenReturn(2);
    final Optional<JPAKeyPair> act = cut.getKeyBoundary(requestContext);
    assertTrue(act.isPresent());
    assertEquals(12, act.get().getMin().get(et.getKey().get(0)));
  }

  @Test
  public void checkGetKeyBoundaryTwoResultWithSkip() throws ODataJPAModelException, ODataJPAQueryException {

    addTuple(Integer.valueOf(12));
    addTuple(Integer.valueOf(15));
    cut = new JPAExpandQueryResult(queryResult, null, et, Collections.emptyList());
    when(uriInfo.getSkipOption()).thenReturn(skip);
    when(uriInfo.getExpandOption()).thenReturn(expand);
    when(skip.getValue()).thenReturn(2);
    final Optional<JPAKeyPair> act = cut.getKeyBoundary(requestContext);
    assertTrue(act.isPresent());
    assertEquals(12, act.get().getMin().get(et.getKey().get(0)));
    assertEquals(15, act.get().getMax().get(et.getKey().get(0)));
  }

  @Test
  public void checkGetKeyBoundaryOneCompoundResultWithTop() throws ODataJPAModelException, ODataJPAQueryException {

    final Map<String, Object> key = new HashMap<>(1);
    final TupleDouble tuple = new TupleDouble(key);
    tuples.add(tuple);
    key.put("codePublisher", "ISO");
    key.put("codeID", "3166-1");
    key.put("divisionCode", "BEL");
    cut = new JPAExpandQueryResult(queryResult, null, helper.getJPAEntityType("AdministrativeDivisionDescriptions"),
        Collections.emptyList());
    when(uriInfo.getTopOption()).thenReturn(top);
    when(uriInfo.getExpandOption()).thenReturn(expand);
    when(top.getValue()).thenReturn(2);
    final Optional<JPAKeyPair> act = cut.getKeyBoundary(requestContext);
    assertTrue(act.isPresent());
    assertNotNull(act.get().getMin());
    assertNull(act.get().getMax());
  }

  @Test
  public void checkGetKeyBoundaryCollectionRequested() throws ODataJPAModelException, ODataJPAQueryException {

    addTuple(Integer.valueOf(12));
    addTuple(Integer.valueOf(15));
    cut = new JPAExpandQueryResult(queryResult, null, et, Collections.emptyList());
    when(uriInfo.getSkipOption()).thenReturn(skip);
    when(uriInfo.getExpandOption()).thenReturn(expand);
    when(skip.getValue()).thenReturn(2);
    final Optional<JPAKeyPair> act = cut.getKeyBoundary(requestContext);
    assertTrue(act.isPresent());
    assertEquals(12, act.get().getMin().get(et.getKey().get(0)));
    assertEquals(15, act.get().getMax().get(et.getKey().get(0)));
  }

  private void addTuple(final Integer value) {
    final Map<String, Object> key = new HashMap<>(1);
    final TupleDouble tuple = new TupleDouble(key);
    tuples.add(tuple);
    key.put("ID", value);
  }

}
