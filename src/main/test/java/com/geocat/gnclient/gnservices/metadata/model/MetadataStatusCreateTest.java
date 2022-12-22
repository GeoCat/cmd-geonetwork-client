package com.geocat.gnclient.gnservices.metadata.model;

import org.junit.Assert;
import org.junit.Test;

public class MetadataStatusCreateTest {

    @Test
    public void testBuildStatus() {
        MetadataStatus st = new MetadataStatus();
        Assert.assertEquals(Integer.valueOf(0), MetadataStatusCreate.build(st).getStatus());

        st.setName("unknown");
        Assert.assertEquals(Integer.valueOf(0), MetadataStatusCreate.build(st).getStatus());

        // Non valid value mapped to unknown status
        st.setName("non-valid-value");
        Assert.assertEquals(Integer.valueOf(0), MetadataStatusCreate.build(st).getStatus());

        st.setName("draft");
        Assert.assertEquals(Integer.valueOf(1), MetadataStatusCreate.build(st).getStatus());

        st.setName("approved");
        Assert.assertEquals(Integer.valueOf(2), MetadataStatusCreate.build(st).getStatus());

        st.setName("retired");
        Assert.assertEquals(Integer.valueOf(3), MetadataStatusCreate.build(st).getStatus());

        st.setName("submitted");
        Assert.assertEquals(Integer.valueOf(4), MetadataStatusCreate.build(st).getStatus());

        st.setName("rejected");
        Assert.assertEquals(Integer.valueOf(5), MetadataStatusCreate.build(st).getStatus());
    }
}
