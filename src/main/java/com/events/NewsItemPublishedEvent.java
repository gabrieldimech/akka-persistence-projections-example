package com.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewsItemPublishedEvent implements Event {
    private Long   timestamp;
    private String newsItemTopic;
    public static final String TYPE = "NEWS_ITEM_PUBLISHED";

    @Override
    public String getType() {
        return TYPE;
    }

}
