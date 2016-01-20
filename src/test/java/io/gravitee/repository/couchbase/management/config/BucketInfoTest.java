package io.gravitee.repository.couchbase.management.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.couchbase.client.java.bucket.BucketInfo;
import com.couchbase.client.java.bucket.BucketType;

import io.gravitee.repository.couchbase.management.config.util.ClusterDependentTest;

public class BucketInfoTest extends ClusterDependentTest  {

    @Test
    public void shouldLoadBucketInfo() {
        BucketInfo info = bucket().bucketManager().info();

        assertEquals(BucketType.COUCHBASE, info.type());
        assertEquals(bucketName(), info.name());
        assertTrue(info.nodeCount() > 0);
        assertEquals(info.nodeCount(), info.nodeList().size());
        assertNotNull(info.raw());
    }

}
