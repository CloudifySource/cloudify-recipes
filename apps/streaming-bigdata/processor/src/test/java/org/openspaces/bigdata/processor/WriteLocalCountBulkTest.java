package org.openspaces.bigdata.processor;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openspaces.bigdata.processor.events.TokenCounter;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.springframework.beans.factory.annotation.Autowired;

import com.j_spaces.core.IJSpace;

public class WriteLocalCountBulkTest {
    
    @Autowired
    GigaSpace gigaSpace;

    @Before
    public void setupSpace() {
        IJSpace space = new UrlSpaceConfigurer("/./testSpace").space();
        gigaSpace = new GigaSpaceConfigurer(space).gigaSpace();
    }

    @After
    public void clearSpace() {
        gigaSpace.clear(null);
    }

    @Test
    public void saveTwoCountersAndRequestAll() {
        TokenCounter foo = new TokenCounter("foo", 3);
        TokenCounter bar = new TokenCounter("bar", 8);

        gigaSpace.write(foo);
        gigaSpace.write(bar);

        TokenCounter requestAll = new TokenCounter();

        assertThat(gigaSpace.readMultiple(requestAll)) //
                .isNotNull() //
                .hasSize(2) //
                .contains(foo) //
                .contains(bar);
    }

    @Test
    public void saveTwoCountersButRequestOnlyFoo() {
        TokenCounter foo = new TokenCounter("foo", 3);
        TokenCounter bar = new TokenCounter("bar", 8);

        gigaSpace.write(foo);
        gigaSpace.write(bar);

        TokenCounter requestFoo = new TokenCounter("foo");

        assertThat(gigaSpace.readMultiple(requestFoo)) //
                .isNotNull() //
                .hasSize(1) //
                .containsOnly(foo);
    }
}
