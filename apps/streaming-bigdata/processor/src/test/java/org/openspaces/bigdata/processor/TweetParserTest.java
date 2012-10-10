package org.openspaces.bigdata.processor;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

public class TweetParserTest {
    @Test
    public void tokenize() {
        String text = "Twitter is an online social networking service and microblogging service" //
                + " that enables its users to send and read text-based posts of up to 140 characters," //
                + " known as \"tweets\". You can signup and test it yourself.";
        Map<String, Integer> tokenMap = new TweetParser().tokenize(text);
        
        assertThat(tokenMap).hasSize(25);
        assertThat(tokenMap.get("microblogging")).isEqualTo(1);
        assertThat(tokenMap.get("and")).isEqualTo(3);
        assertThat(tokenMap.get("unknown")).isNull();

        for (Entry<String, Integer> e : tokenMap.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue());
        }
    }
}