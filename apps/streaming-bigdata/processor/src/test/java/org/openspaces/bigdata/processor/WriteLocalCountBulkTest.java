package org.openspaces.bigdata.processor;

import static org.fest.assertions.Assertions.assertThat;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.bigdata.processor.events.TokenCounter;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.j_spaces.core.IJSpace;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/pu.xml")
public class WriteLocalCountBulkTest {
    Logger log = Logger.getLogger(WriteLocalCountBulkTest.class);

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
