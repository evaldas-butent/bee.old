package com.butent.bee.shared.testutils;

import com.butent.bee.shared.Resource;
import com.butent.bee.shared.communication.ContentType;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link com.butent.bee.shared.Resource}.
 */
public class TestResource {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public final void testDeserialize() {
    Resource resource = new Resource("13URI13ZIP15false17Content");
    assertEquals("URI", resource.getUri());
    assertEquals(ContentType.ZIP, resource.getType());
    assertEquals(false, resource.isReadOnly());
    assertEquals("Content", resource.getContent());
  }

  @Test
  public final void testSerialize() {

    Resource resource =
        new Resource("http://localhost/image.jpg", "This will be an image", ContentType.IMAGE,
            true);
    assertEquals("226http://localhost/image.jpg15IMAGE14true221This will be an image", resource
        .serialize());

    Resource resource1 = new Resource();
    assertEquals("0015false0", resource1.serialize());

    Resource resource2 = new Resource("", ContentType.UNKNOWN);
    assertEquals("017UNKNOWN15false0", resource2.serialize());

    Resource resource3 = new Resource("URI", "Content");
    assertEquals("13URI015false17Content", resource3.serialize());

    Resource resource4 = new Resource("URI", "Content", true);
    assertEquals("13URI014true17Content", resource4.serialize());

    Resource resource5 = new Resource("URI", "Content", ContentType.ZIP);
    assertEquals("13URI13ZIP15false17Content", resource5.serialize());
  }
}
